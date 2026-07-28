package com.tab.tab.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;

import com.tab.tab.entity.Category;
import com.tab.tab.entity.Contingent;
import com.tab.tab.entity.Criteria;
import com.tab.tab.entity.Judge;
import com.tab.tab.entity.Score;
import com.tab.tab.repository.ContingentRepository;
import com.tab.tab.repository.CriteriaRepository;
import com.tab.tab.repository.JudgeRepository;
import com.tab.tab.repository.ScoreRepository;
import com.tab.tab.service.ScoreService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("judge")
public class JudgeDataEntryView extends VerticalLayout {

    private final ScoreRepository scoreRepository;
    private final ScoreService scoreService;
    private final JudgeRepository judgeRepository;
    private final CriteriaRepository criteriaRepository;
    private final ContingentRepository contingentRepository;

    private final ComboBox<Judge> judgeComboBox = new ComboBox<>("Select Your Name");
    private final ComboBox<Category> categoryComboBox = new ComboBox<>("Dance Category");
    private final ComboBox<Contingent> contingentComboBox = new ComboBox<>("Contingent");
    
    private final FormLayout criteriaForm = new FormLayout();
    private final Button submitButton = new Button("Submit Scores");
    private final Map<Criteria, IntegerField> scoreFields = new HashMap<>();
    private Binder<Map<Criteria, Integer>> binder = new Binder<>();

    // Added summary label to show who is being scored
    private final Span scoringSummary = new Span();

    private final Div judgeSelectionSection = createSection(false);
    private final Div contingentCategorySelectionSection = createSection(false);
    private final Div criteriaReminderSection = createSection(false); 
    private final Div scoringSection = createSection(true); 

    public JudgeDataEntryView(ScoreService scoreService, ScoreRepository scoreRepository, JudgeRepository judgeRepository, 
                              CriteriaRepository criteriaRepository, ContingentRepository contingentRepository) {
        
        this.scoreService = scoreService;
        this.scoreRepository = scoreRepository;
        this.judgeRepository = judgeRepository;
        this.criteriaRepository = criteriaRepository;
        this.contingentRepository = contingentRepository;

        // --- Inject Custom CSS for Blur Effect ---
        Html blurStyle = new Html("<style>" +
                ".blurred-criteria { " +
                "  filter: blur(4px); " +
                "  opacity: 0.75; " +
                "  transition: all 0.3s ease-in-out; " +
                "  cursor: pointer; " +
                "} " +
                ".blurred-criteria:hover { " +
                "  filter: blur(0px); " +
                "  opacity: 1; " +
                "} " +
                "</style>");
        
        // --- Layout Configuration ---
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding", "20px");
        getStyle().set("background-color", "var(--lumo-contrast-5pct)"); 

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("900px");
        contentLayout.setSpacing(true);
        contentLayout.setPadding(true);
        contentLayout.setAlignItems(Alignment.STRETCH);
        
        // Add the CSS to the top of the content layout
        contentLayout.add(blurStyle);

        // --- 1. Judge Selection Section ---
        H2 mainTitle = new H2("Judge Scoring Portal");
        mainTitle.getStyle().set("margin-top", "0");
        Paragraph judgeInstruction = new Paragraph("Welcome! Please select your name from the dropdown to begin scoring.");
        judgeInstruction.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        judgeComboBox.setItems(judgeRepository.findAll());
        judgeComboBox.setItemLabelGenerator(Judge::getName);
        judgeComboBox.setWidth("100%");
        judgeSelectionSection.add(mainTitle, judgeInstruction, judgeComboBox);

        // --- 2. Contingent and Category Selection Section ---
        Paragraph selectionInstruction = new Paragraph("Next, choose the contingent and the dance category you are scoring.");
        selectionInstruction.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        categoryComboBox.setItems(EnumSet.allOf(Category.class));
        contingentComboBox.setItems(contingentRepository.findAll());
        contingentComboBox.setItemLabelGenerator(Contingent::getDisplayName);
        
        FormLayout selectionForm = new FormLayout(contingentComboBox, categoryComboBox);
        selectionForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), 
                new FormLayout.ResponsiveStep("500px", 2)
        );
        contingentCategorySelectionSection.add(selectionInstruction, selectionForm);
        contingentCategorySelectionSection.setVisible(false);

        // --- 3. Scoring Section ---
        H3 scoringTitle = new H3("Enter Scores");
        
        // Scoring Summary (Enhancement to prevent scoring the wrong person)
        scoringSummary.getStyle().set("font-weight", "bold");
        scoringSummary.getStyle().set("font-size", "var(--lumo-font-size-l)");
        scoringSummary.getStyle().set("color", "var(--lumo-primary-text-color)");
        scoringSummary.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        scoringSummary.getStyle().set("display", "block");
        
        Paragraph scoringInstruction = new Paragraph("Enter a score from 1 to 10 for each criterion.");
        scoringInstruction.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        criteriaForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), 
                new FormLayout.ResponsiveStep("500px", 2)
        );
        
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitButton.getStyle().set("margin-top", "var(--lumo-space-m)");
        
        VerticalLayout submitLayout = new VerticalLayout(submitButton);
        submitLayout.setAlignItems(Alignment.CENTER);
        
        // Add summary before the form
        scoringSection.add(scoringTitle, scoringSummary, scoringInstruction, criteriaForm, submitLayout);
        scoringSection.setVisible(false);

        // --- 4. Criteria Reminder Section (With Blur Effect) ---
        H3 reminderTitle = new H3("Grading Criteria Reference");
        reminderTitle.getStyle().set("margin-bottom", "var(--lumo-space-xs)");
        
        Paragraph hoverHint = new Paragraph("Hover over this section to reveal criteria details");
        hoverHint.getStyle().set("font-size", "var(--lumo-font-size-s)");
        hoverHint.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        hoverHint.getStyle().set("font-style", "italic");
        hoverHint.getStyle().set("text-align", "center");
        hoverHint.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        criteriaReminderSection.add(reminderTitle, hoverHint, createCriteriaTable());
        criteriaReminderSection.setVisible(false); 
        
        // Apply the blur CSS class
        criteriaReminderSection.addClassName("blurred-criteria");
        // Give it a distinct "reference card" look
        criteriaReminderSection.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
        criteriaReminderSection.getStyle().set("border", "1px dashed var(--lumo-contrast-30pct)");

        // --- Add all sections to the main view ---
        contentLayout.add(judgeSelectionSection, contingentCategorySelectionSection, scoringSection, criteriaReminderSection);
        add(contentLayout);

        // --- Event Listeners ---
        judgeComboBox.addValueChangeListener(event -> {
            boolean judgeSelected = event.getValue() != null;
            contingentCategorySelectionSection.setVisible(judgeSelected);
            criteriaReminderSection.setVisible(judgeSelected); 
            
            if (!judgeSelected) {
                clearContingentAndCategory();
                scoringSection.setVisible(false);
                criteriaReminderSection.setVisible(false);
            }
        });

        categoryComboBox.addValueChangeListener(event -> {
            updateCriteriaFields(event.getValue());
            checkAndEnableScoringSection();
        });

        contingentComboBox.addValueChangeListener(event -> checkAndEnableScoringSection());
        submitButton.addClickListener(event -> submitScores());
    }

    private Div createSection(boolean highlight) {
        Div section = new Div();
        section.getStyle().set("background-color", "var(--lumo-base-color)");
        section.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        section.getStyle().set("box-shadow", "0 2px 8px var(--lumo-shade-10pct)");
        section.getStyle().set("padding", "var(--lumo-space-l)");
        section.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        
        if (highlight) {
            section.getStyle().set("border", "2px solid var(--lumo-primary-color)");
            section.getStyle().set("background-color", "var(--lumo-primary-color-5pct)");
        }
        return section;
    }

    private VerticalLayout createCriteriaTable() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        
        Map<Category, List<Criteria>> criteriaByCategory = criteriaRepository.findAll().stream()
                .collect(Collectors.groupingBy(Criteria::getCategory));

        DecimalFormat percentFormat = new DecimalFormat("#0%");

        for (Category category : EnumSet.allOf(Category.class)) {
            H3 categoryHeader = new H3(category.name().replace("_", " "));
            categoryHeader.getStyle().set("font-size", "var(--lumo-font-size-m)");
            categoryHeader.getStyle().set("margin-bottom", "var(--lumo-space-xs)");
            layout.add(categoryHeader);

            List<Criteria> criteriaList = criteriaByCategory.getOrDefault(category, new ArrayList<>());
            criteriaList.sort(Comparator.comparing(Criteria::getDisplayName));

            for (Criteria criteria : criteriaList) {
                HorizontalLayout criteriaRow = new HorizontalLayout();
                criteriaRow.setWidthFull();
                criteriaRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                criteriaRow.getStyle().set("padding", "var(--lumo-space-xs) 0");
                
                Paragraph name = new Paragraph(criteria.getDisplayName());
                name.getStyle().set("margin", "0");
                
                Paragraph weight = new Paragraph(percentFormat.format(criteria.getWeight()));
                weight.getStyle().set("margin", "0");
                weight.getStyle().set("font-weight", "bold");
                weight.getStyle().set("color", "var(--lumo-primary-text-color)");

                criteriaRow.add(name, weight);
                layout.add(criteriaRow);
            }
            layout.add(new Hr());
        }
        return layout;
    }

    private void clearContingentAndCategory() {
        contingentComboBox.clear();
        categoryComboBox.clear();
    }

    private void checkAndEnableScoringSection() {
        boolean readyForScoring = contingentComboBox.getValue() != null && categoryComboBox.getValue() != null;
        scoringSection.setVisible(readyForScoring);
        
        // Update the scoring summary to prevent data entry errors
        if (readyForScoring) {
            Contingent c = contingentComboBox.getValue();
            Category cat = categoryComboBox.getValue();
            scoringSummary.setText("Now Scoring: " + c.getDisplayName() + "  |  Category: " + cat.name().replace("_", " "));
        } else {
            scoringSummary.setText("");
        }
    }

    private void updateCriteriaFields(Category category) {
        criteriaForm.removeAll();
        scoreFields.clear();
        binder = new Binder<>(); 
        binder.setBean(new HashMap<>());

        if (category == null) return;

        for (Criteria criteria : criteriaRepository.findAll()) {
            if (criteria.getCategory() == category) {
                IntegerField scoreField = new IntegerField(criteria.getDisplayName());
                scoreField.setMin(1);
                scoreField.setMax(10);
                scoreField.setStepButtonsVisible(true);
                scoreField.setWidthFull();
                
                // Enhance field styling for easier tapping/clicking
                scoreField.getStyle().set("font-size", "var(--lumo-font-size-l)");
                scoreField.getStyle().set("padding", "var(--lumo-space-s)");
                
                scoreFields.put(criteria, scoreField);
                criteriaForm.add(scoreField);

                binder.forField(scoreField)
                        .asRequired("Score is required")
                        .bind(map -> map.get(criteria), (map, value) -> map.put(criteria, value));
            }
        }
    }

    private void submitScores() {
        if (judgeComboBox.isEmpty() || categoryComboBox.isEmpty() || contingentComboBox.isEmpty()) {
            Notification.show("Please ensure a Judge, Contingent, and Category are selected.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (!binder.validate().isOk()) {
            Notification.show("Please fill in all required scores.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Judge judge = judgeComboBox.getValue();
        Contingent contingent = contingentComboBox.getValue();
        List<Score> scoresToSave = new ArrayList<>();

        for (Map.Entry<Criteria, IntegerField> entry : scoreFields.entrySet()) {
            scoresToSave.add(new Score(null, judge, contingent, entry.getKey(), entry.getValue().getValue()));
        }

        try {
            scoreService.saveAllScores(scoresToSave);
            Notification.show("Scores for " + contingent.getDisplayName() + " submitted successfully!", 3000, Notification.Position.MIDDLE);
            
            // Clear for next entry, but keep the judge selected.
            clearContingentAndCategory();
            scoringSection.setVisible(false);
            
            // FIX: Removed criteriaReminderSection.setVisible(false); 
            // The reminder should stay visible as long as the judge is logged in.
            
            updateCriteriaFields(null); 
        } catch (DataIntegrityViolationException e) {
            Notification.show("Error: Scores for this contingent have already been submitted by this judge.", 5000, Notification.Position.MIDDLE);
        }
    }
}