package com.tab.tab.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("") // Maps this view to the root URL (localhost:8080)
public class MainView extends VerticalLayout {

    private final Button judgeEntryButton = new Button("Judge Score");
    private final Button dashboardButton = new Button("Dashboard");

    public MainView() {

        // Title
        H1 title = new H1("Sarimanok Cultural Street Dance");
        Paragraph subtitle1 = new Paragraph("Sarimanok Stadium, Marawi City");
        Paragraph subtitle2 = new Paragraph("30 July 2026");

        VerticalLayout titleLayout = new VerticalLayout(title, subtitle1, subtitle2);
        titleLayout.setAlignItems(Alignment.CENTER);
        titleLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        titleLayout.setWidthFull();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        judgeEntryButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("judge")));
        dashboardButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));

        judgeEntryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        dashboardButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);

        // 2. Arrange Components in Layouts and add navigation buttons
        HorizontalLayout buttonLayout = new HorizontalLayout(judgeEntryButton, dashboardButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.setWidthFull();

        add(titleLayout, buttonLayout);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeight("100vh");
    }
}