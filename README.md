# Simple Tabulation 

A web application for managing and tabulating scores for competitions, built with Spring Boot and Vaadin. This system allows for the definition of judges, contingents (participants), and scoring criteria, as well as the efficient recording and updating of scores.

## Technologies Used

*   **Java:** The core programming language.
*   **Spring Boot:** Framework for building robust, stand-alone, production-grade Spring applications.
*   **Vaadin:** A Java web framework for building modern web applications with a rich user interface directly in Java.
*   **Spring Data JPA:** Simplifies data access for relational databases.
*   **H2 Database:** An embedded, in-memory relational database used for development and prototyping.
*   **Maven:** A build automation tool for Java projects.

## Features

*   **Score Management:** Efficiently record and update scores given by judges to contingents based on various criteria.
*   **Judge Management:** Define and manage a list of judges.
*   **Contingent Management:** Define and manage a list of participants (contingents).
*   **Criteria Management:** Define and manage the scoring criteria for the competition.
*   **Data Persistence:** Scores and related entities are persisted using an H2 in-memory database, which is useful for development and testing.
*   **Responsive UI:** A rich, interactive web user interface provided by Vaadin, accessible via a web browser.
*   **Custom Score Service:** Includes a custom service (`ScoreService`) to handle intelligent `save-or-update` logic for scores, ensuring data integrity by updating existing entries based on unique judge-contingent-criteria combinations or creating new ones if they don't exist.

## How to Run

To get this project up and running on your local machine, follow these steps:

### Prerequisites

*   **Java Development Kit (JDK) 17 or newer:** Ensure you have a compatible JDK installed.
*   **Apache Maven:** Make sure Maven is installed and configured in your environment.

### Steps

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd tab
    ```
    (Replace `<your-repository-url>` with the actual URL of your GitHub repository.)

2.  **Build and run the application:**
    Navigate to the root directory of the project (`tab/`) in your terminal and execute the following Maven command:
    ```bash
    mvn spring-boot:run
    ```
    This command will compile the project, download all necessary dependencies, and start the Spring Boot application.

3.  **Access the application:**
    Once the application has started (you'll see messages in the console indicating it's running), open your web browser and navigate to:
    ```
    http://localhost:8080
    ```
    You should now see the application's user interface.

## Database

This application utilizes an embedded H2 database. By default, the database files are created in the `data/` directory within the project root (e.g., `data/tabdb.mv.db`). If `src/main/resources/data.sql` is present and configured to run on startup, the database schema and initial data will be recreated each time the application starts.

