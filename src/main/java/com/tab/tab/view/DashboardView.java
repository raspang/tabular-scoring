package com.tab.tab.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.tab.tab.entity.Category;
import com.tab.tab.entity.Contingent;
import com.tab.tab.entity.Criteria;
import com.tab.tab.entity.Judge;
import com.tab.tab.entity.Score;
import com.tab.tab.entity.dto.ContingentResultDTO;
import com.tab.tab.repository.ContingentRepository;
import com.tab.tab.repository.CriteriaRepository;
import com.tab.tab.repository.JudgeRepository;
import com.tab.tab.repository.ScoreRepository;
import com.tab.tab.service.TabulationService;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("dashboard")
@PageTitle("Live Dashboard")
public class DashboardView extends VerticalLayout {

    private final TabulationService tabulationService;
    private final ScheduledExecutorService executorService;
    private final CriteriaRepository criteriaRepository;
    private final ScoreRepository scoreRepository;
    private final JudgeRepository judgeRepository;
    private final ContingentRepository contingentRepository;
    
    private final Grid<ContingentResultDTO> grid = new Grid<>(ContingentResultDTO.class, false);
    private final ComboBox<Category> categoryFilterComboBox = new ComboBox<>("Filter by Category");
    private final Div scoreTablesDiv = new Div();
    
    private FeederThread feederThread;
    
    private List<ContingentResultDTO> currentResults = new ArrayList<>();
    private Category currentSelectedCategory;

    public DashboardView(TabulationService tabulationService, ScheduledExecutorService executorService, 
                         CriteriaRepository criteriaRepository, ScoreRepository scoreRepository, 
                         JudgeRepository judgeRepository, ContingentRepository contingentRepository) {
        
        this.tabulationService = tabulationService;
        this.executorService = executorService;
        this.criteriaRepository = criteriaRepository;
        this.scoreRepository = scoreRepository;
        this.judgeRepository = judgeRepository;
        this.contingentRepository = contingentRepository;

        // --- Inject Custom CSS for Dashboard and Score Tables ---
        Html customStyle = new Html("<style>" +
                /* Dashboard Card & Header */
                ".dashboard-card { background-color: var(--lumo-base-color); border-radius: var(--lumo-border-radius-l); box-shadow: 0 4px 12px var(--lumo-shade-10pct); padding: var(--lumo-space-l); width: 100%; box-sizing: border-box; } " +
                ".dashboard-header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: var(--lumo-space-m); margin-bottom: var(--lumo-space-m); } " +
                ".dashboard-title { margin: 0; font-size: var(--lumo-font-size-xxl); color: var(--lumo-primary-text-color); } " +
                ".live-badge { background-color: var(--lumo-error-color); color: white; padding: 2px 8px; border-radius: 12px; font-size: var(--lumo-font-size-xs); font-weight: bold; margin-left: 8px; animation: pulse 2s infinite; } " +
                "@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } } " +
                ".dashboard-grid { width: 100%; font-size: var(--lumo-font-size-s); } " +
                
                /* Score Tables CSS */
                ".category-title { color: var(--lumo-primary-text-color); border-bottom: 2px solid var(--lumo-primary-color); padding-bottom: var(--lumo-space-xs); margin-top: var(--lumo-space-l); margin-bottom: var(--lumo-space-m); font-size: var(--lumo-font-size-l); } " +
                ".criteria-table-card { background: var(--lumo-base-color); border-radius: var(--lumo-border-radius-m); box-shadow: 0 2px 8px var(--lumo-shade-10pct); margin-bottom: var(--lumo-space-l); overflow: hidden; } " +
                ".criteria-title { margin: 0; padding: var(--lumo-space-m); background-color: var(--lumo-contrast-5pct); font-size: var(--lumo-font-size-m); border-bottom: 1px solid var(--lumo-contrast-10pct); color: var(--lumo-body-text-color); } " +
                ".table-wrapper { overflow-x: auto; -webkit-overflow-scrolling: touch; } " +
                ".score-table { width: 100%; border-collapse: collapse; font-size: var(--lumo-font-size-s); } " +
                ".score-table th, .score-table td { padding: var(--lumo-space-s) var(--lumo-space-m); text-align: center; border-bottom: 1px solid var(--lumo-contrast-10pct); white-space: nowrap; } " +
                ".score-table th { background-color: var(--lumo-contrast-5pct); font-weight: bold; color: var(--lumo-secondary-text-color); position: sticky; top: 0; z-index: 2; } " +
                /* Sticky First Column for Mobile Scrolling */
                ".score-table td.contingent-cell { text-align: left; font-weight: bold; background-color: var(--lumo-base-color); position: sticky; left: 0; z-index: 1; border-right: 2px solid var(--lumo-contrast-10pct); } " +
                ".score-table th:first-child { text-align: left; position: sticky; left: 0; z-index: 3; background-color: var(--lumo-contrast-5pct); border-right: 2px solid var(--lumo-contrast-10pct); } " +
                ".score-table td.score-cell { font-variant-numeric: tabular-nums; font-weight: 500; } " +
                ".score-table tbody tr:hover td:not(.contingent-cell) { background-color: var(--lumo-primary-color-10pct); } " +
                ".no-data { display: flex; align-items: center; justify-content: center; height: 150px; color: var(--lumo-secondary-text-color); font-style: italic; } " +
                
                /* Responsive Adjustments */
                "@media (max-width: 600px) { " +
                "  .dashboard-title { font-size: var(--lumo-font-size-l); } " +
                "  .dashboard-card { padding: var(--lumo-space-m); } " +
                "  .dashboard-grid { font-size: var(--lumo-font-size-xs); } " +
                "  .score-table { font-size: var(--lumo-font-size-xs); } " +
                "  .score-table th, .score-table td { padding: var(--lumo-space-xs) var(--lumo-space-s); } " +
                "} " +
                "</style>");

        // --- Layout Configuration ---
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding", "var(--lumo-space-m)");
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        add(customStyle);

        // --- Header Section ---
        Div header = new Div();
        header.addClassName("dashboard-header");
        
        H1 title = new H1("Ranaw Street Dance Showdown");
        title.addClassName("dashboard-title");
        
        Span liveBadge = new Span("LIVE");
        liveBadge.addClassName("live-badge");
        title.add(liveBadge);
        
        categoryFilterComboBox.setItems(EnumSet.allOf(Category.class));
        categoryFilterComboBox.setPlaceholder("All Categories");
        categoryFilterComboBox.setClearButtonVisible(true);
        categoryFilterComboBox.setWidth("100%");
        categoryFilterComboBox.getStyle().set("max-width", "300px");
        categoryFilterComboBox.addValueChangeListener(event -> {
            currentSelectedCategory = event.getValue();
            setupGridColumns(event.getValue());
        });

        header.add(title, categoryFilterComboBox);

        // --- Grid Section ---
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.addClassName("dashboard-grid");
        grid.setWidth("100%");
        
        Div gridCard = new Div(grid);
        gridCard.addClassName("dashboard-card");

        // --- Score Tables Section ---
        Div tablesCard = new Div(scoreTablesDiv);
        tablesCard.addClassName("dashboard-card");
        tablesCard.getStyle().set("margin-top", "var(--lumo-space-m)");

        // --- Main Content Layout ---
        VerticalLayout contentLayout = new VerticalLayout(header, gridCard, tablesCard);
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(false);
        contentLayout.setSpacing(true);
        
        add(contentLayout);
        expand(gridCard);

        setupGridColumns(null);
    }

    private void setupGridColumns(Category selectedCategory) {
        grid.removeAllColumns();

        grid.addColumn(dto -> currentResults.indexOf(dto) + 1)
                .setHeader("Rank").setSortable(false).setFlexGrow(0).setWidth("60px").setTextAlign(ColumnTextAlign.CENTER);

        grid.addColumn(ContingentResultDTO::getContingentName)
                .setHeader("Contingent").setSortable(false).setFlexGrow(2);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (selectedCategory == null) {
            grid.addColumn(new NumberRenderer<>(ContingentResultDTO::getStreetDanceTotal, decimalFormat, "N/A"))
                    .setHeader("Street").setSortable(false).setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
            grid.addColumn(new NumberRenderer<>(ContingentResultDTO::getCulturalShowdownTotal, decimalFormat, "N/A"))
                    .setHeader("Cultural").setSortable(false).setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        } else {
            criteriaRepository.findAll().stream()
                    .filter(criteria -> criteria.getCategory() == selectedCategory)
                    .forEach(criteria ->
                            grid.addColumn(new NumberRenderer<>(dto -> dto.getWeightedScores().get(criteria), decimalFormat, "N/A"))
                                    .setHeader(criteria.getDisplayName()).setSortable(false).setFlexGrow(1).setWidth("90px").setTextAlign(ColumnTextAlign.END)
                    );
            
            String totalHeader = selectedCategory == Category.STREET_DANCE ? "Street Total" : "Cultural Total";
            NumberRenderer<ContingentResultDTO> totalRenderer = selectedCategory == Category.STREET_DANCE ?
                    new NumberRenderer<ContingentResultDTO>(ContingentResultDTO::getStreetDanceTotal, decimalFormat, "N/A") :
                    new NumberRenderer<ContingentResultDTO>(ContingentResultDTO::getCulturalShowdownTotal, decimalFormat, "N/A");

            grid.addColumn(totalRenderer).setHeader(totalHeader).setSortable(false).setFlexGrow(1).setTextAlign(ColumnTextAlign.END);
        }

        grid.addColumn(new NumberRenderer<>(ContingentResultDTO::getGrandTotal, decimalFormat, "N/A"))
                .setHeader("Grand Total").setSortable(false).setFlexGrow(1).setTextAlign(ColumnTextAlign.END);

        refreshGrid();
    }

    /**
     * Fetches raw scores and builds HTML tables for each criteria.
     */
    private void updateScoreTables(Category selectedCategory) {
        List<Score> allScores = scoreRepository.findAll();
        List<Criteria> allCriteria = criteriaRepository.findAll();
        List<Contingent> allContingents = contingentRepository.findAll();
        List<Judge> allJudges = judgeRepository.findAll();

        // Filter criteria based on the selected category
        List<Criteria> criteriaToShow = allCriteria.stream()
                .filter(c -> selectedCategory == null || c.getCategory() == selectedCategory)
                .toList();

        if (criteriaToShow.isEmpty()) {
            scoreTablesDiv.getElement().setProperty("innerHTML", 
                "<div class='no-data'>No criteria found for this category.</div>");
            return;
        }

        // Group scores: Criteria -> Contingent -> Judge -> ScoreValue
        Map<Criteria, Map<Contingent, Map<Judge, Integer>>> scoreMap = new LinkedHashMap<>();
        
        // Initialize map structure to ensure all contingents/judges appear even if no score yet
        for (Criteria c : criteriaToShow) {
            scoreMap.put(c, new LinkedHashMap<>());
            for (Contingent cont : allContingents) {
                scoreMap.get(c).put(cont, new LinkedHashMap<>());
                for (Judge j : allJudges) {
                    scoreMap.get(c).get(cont).put(j, null); 
                }
            }
        }

        // Populate actual scores
        for (Score s : allScores) {
            if (scoreMap.containsKey(s.getCriteria())) {
                scoreMap.get(s.getCriteria())
                        .get(s.getContingent())
                        .put(s.getJudge(), s.getRawScore());
            }
        }

        // Build HTML
        StringBuilder html = new StringBuilder();
        
        // Group tables by Category for better organization
        Map<Category, List<Criteria>> criteriaByCategory = criteriaToShow.stream()
                .collect(Collectors.groupingBy(Criteria::getCategory, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Category, List<Criteria>> catEntry : criteriaByCategory.entrySet()) {
            html.append("<h2 class='category-title'>").append(catEntry.getKey().name().replace("_", " ")).append("</h2>");
            
            for (Criteria criteria : catEntry.getValue()) {
                html.append("<div class='criteria-table-card'>");
                html.append("<h3 class='criteria-title'>").append(criteria.getDisplayName()).append("</h3>");
                html.append("<div class='table-wrapper'>");
                html.append("<table class='score-table'>");
                
                // Header
                html.append("<thead><tr><th>Contingent</th>");
                for (Judge j : allJudges) {
                    html.append("<th>").append(j.getName()).append("</th>");
                }
                html.append("</tr></thead>");
                
                // Body
                html.append("<tbody>");
                Map<Contingent, Map<Judge, Integer>> contMap = scoreMap.get(criteria);
                for (Contingent cont : allContingents) {
                    html.append("<tr><td class='contingent-cell'>").append(cont.getDisplayName()).append("</td>");
                    Map<Judge, Integer> judgeMap = contMap.get(cont);
                    for (Judge j : allJudges) {
                        Integer score = judgeMap.get(j);
                        String displayScore = (score != null) ? String.valueOf(score) : "-";
                        html.append("<td class='score-cell'>").append(displayScore).append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</tbody></table></div></div>");
            }
        }
        
        scoreTablesDiv.getElement().setProperty("innerHTML", html.toString());
    }

    private void sortResults(List<ContingentResultDTO> results, Category category) {
        if (category == null) {
            results.sort(Comparator.comparingDouble(ContingentResultDTO::getGrandTotal).reversed());
        } else if (category == Category.STREET_DANCE) {
            results.sort(Comparator.comparingDouble(ContingentResultDTO::getStreetDanceTotal).reversed());
        } else if (category == Category.CULTURAL_SHOWDOWN) {
            results.sort(Comparator.comparingDouble(ContingentResultDTO::getCulturalShowdownTotal).reversed());
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        feederThread = new FeederThread(ui, this);
        executorService.scheduleAtFixedRate(feederThread, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (feederThread != null) {
            feederThread.stop();
        }
        super.onDetach(detachEvent);
    }

    private void refreshGrid() {
        currentResults = new ArrayList<>(tabulationService.calculateAllContingentResults());
        sortResults(currentResults, currentSelectedCategory);
        grid.setItems(currentResults);
        
        // Update the detailed score tables
        updateScoreTables(currentSelectedCategory);
    }

    private static class FeederThread implements Runnable {
        private final UI ui;
        private final DashboardView view;
        private volatile boolean running = true;

        FeederThread(UI ui, DashboardView view) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            if (running) {
                ui.access(view::refreshGrid);
            }
        }

        public void stop() {
            running = false;
        }
    }
}