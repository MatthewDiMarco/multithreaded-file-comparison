package sec.multithreadedfilecomparison;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sec.multithreadedfilecomparison.controller.FileScanner;
import sec.multithreadedfilecomparison.model.ComparisonResult;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class FileComparisonApplication extends Application {

    private TableView<ComparisonResult> resultTable = new TableView<ComparisonResult>();
    private ProgressBar progressBar = new ProgressBar();
    private FileScanner fileScanner;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("File Comparison Application");
        stage.setMinWidth(600);

        // Create toolbar
        Button compareBtn = new Button("Compare...");
        Button stopBtn = new Button("Stop");
        ToolBar toolBar = new ToolBar(compareBtn, stopBtn);

        // Set up button event handlers
        compareBtn.setOnAction(event -> crossCompare(stage));
        stopBtn.setOnAction(event -> stopComparison());

        // Initialise progressbar
        progressBar.setProgress(0.0);

        TableColumn<ComparisonResult,String> file1Col = new TableColumn<>("File 1");
        TableColumn<ComparisonResult,String> file2Col = new TableColumn<>("File 2");
        TableColumn<ComparisonResult,String> similarityCol = new TableColumn<>("Similarity");

        // The following tells JavaFX how to extract information from a ComparisonResult
        // object and put it into the three table columns.
        file1Col.setCellValueFactory(
                (cell) -> new SimpleStringProperty(cell.getValue().getComparisonPair().getFile1()) );

        file2Col.setCellValueFactory(
                (cell) -> new SimpleStringProperty(cell.getValue().getComparisonPair().getFile2()) );

        similarityCol.setCellValueFactory(
                (cell) -> new SimpleStringProperty(
                        String.format("%.1f%%", cell.getValue().getSimilarity() * 100.0)) );

        // Set and adjust table column widths.
        file1Col.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.40));
        file2Col.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.40));
        similarityCol.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.20));

        // Add the columns to the table.
        resultTable.getColumns().add(file1Col);
        resultTable.getColumns().add(file2Col);
        resultTable.getColumns().add(similarityCol);

        // Add the main parts of the UI to the window.
        BorderPane mainBox = new BorderPane();
        mainBox.setTop(toolBar);
        mainBox.setCenter(resultTable);
        mainBox.setBottom(progressBar);
        Scene scene = new Scene(mainBox);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    /**
     * todo
     * @param stage
     */
    private void crossCompare(Stage stage) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File("."));
        dc.setTitle("Choose directory");
        File directory = dc.showDialog(stage);

        if (directory != null) {
            System.out.println("Comparing files within " + directory + "...");

            Set<String> suffixes = Set.of("txt", "md", "java", "cs", "c", "cpp");
            fileScanner = new FileScanner(directory, suffixes);
            fileScanner.start();
        }

        // todo
    }

    /**
     * todo
     */
    private void stopComparison() {
        System.out.println("Stopping comparison...");

        if (fileScanner != null) {
            fileScanner.stop();
            fileScanner = null;
        }

        // todo
    }

    public static void main(String[] args) {
        launch();
    }
}