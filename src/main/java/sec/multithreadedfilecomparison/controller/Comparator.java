package sec.multithreadedfilecomparison.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import sec.multithreadedfilecomparison.helper.Helpers;
import sec.multithreadedfilecomparison.model.ComparisonPair;
import sec.multithreadedfilecomparison.model.ComparisonResult;
import sec.multithreadedfilecomparison.model.FileItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class's responsibility is to:
 * Accept files from the File Scanner and arrange them into pairs, which are
 * submitted to a thread pool for similarity processing.
 * It should do this without creating any redundant combinations, like (a,b) & (b,a)
 * (i.e. combinations that have already been submitted to the pool).
 */
public class Comparator implements Runnable {

    private static final String THREAD_NAME = "comparator-thread";
    private static final int NUM_COMPARISON_THREADS = 4;
    private Thread thread;
    private Object mutex;
    private List<FileItem> fileHistory;
    private FileScanner fileProducer;
    private ResultsLogger logger;
    private ProgressBar guiProgressBar;
    private Text guiJobText;
    private TableView<ComparisonResult> guiTable;
    private ExecutorService exService;
    private int comparisonJobsComplete;

    public Comparator(
            FileScanner producer,
            ResultsLogger logger,
            ProgressBar guiProgressBar,
            Text guiJobText,
            TableView<ComparisonResult> guiTable
    ) {
        this.fileHistory = new ArrayList<FileItem>();
        this.fileProducer = producer;
        this.logger = logger;
        this.guiProgressBar = guiProgressBar;
        this.guiJobText = guiJobText;
        this.guiTable = guiTable;
        this.mutex = new Object();
        this.exService = null;
        this.comparisonJobsComplete = 0;
    }

    /**
     * Start organising files in a separate thread.
     */
    public void start() {
        this.thread = new Thread(this, THREAD_NAME);
        this.thread.start();
    }

    /**
     * Safely bring the thread to a close.
     */
    public void stop() {
        if (this.thread == null) {
            throw new IllegalStateException(THREAD_NAME + " does not exist");
        }

        this.thread.interrupt();
        this.thread = null;
    }

    /**
     * For every file inside 'history' - pair it with the provided new file.
     * @param newFile New File
     * @return List of pairs
     */
    private List<ComparisonPair> generatePairs(FileItem newFile) {
        List<ComparisonPair> generatedPairs = new ArrayList<ComparisonPair>();
        for (FileItem pastFile : fileHistory) {
            if (pastFile == newFile) {
                throw new IllegalArgumentException(
                        "The provided file already exists in the " +
                        "comparator history; please provide a new file " +
                        "to avoid redundant similarity checks."
                );
            }
            ComparisonPair newPair = new ComparisonPair(newFile, pastFile);
            generatedPairs.add(newPair);
        }

        return generatedPairs;
    }

    /**
     * Scan through the directory tree and stop at each file.
     * If the suffix is ok, extract the contents and push it to the queue.
     */
    @Override
    public void run() {
        System.out.println("Starting Comparator... ");

        try {
            exService = Executors.newFixedThreadPool(NUM_COMPARISON_THREADS);

            boolean running = true;
            while (running) {
                FileItem fileItem = fileProducer.getNextFile();

                if (fileItem == null) {
                    running = false;
                } else {
                    List<ComparisonPair> pairs = generatePairs(fileItem);
                    for (ComparisonPair pair : pairs) { // submit to similarity checking pool
                        exService.execute(new ComparisonJob(pair));
                    }

                    fileHistory.add(fileItem);
                }
            }

            // Wait for all remaining comparison jobs to finish
            exService.shutdown();
            exService.awaitTermination(1, TimeUnit.HOURS);

            // Stop the Logger
            logger.stop();

            // Notify user
            Platform.runLater(() -> {
                Alert aa = new Alert(Alert.AlertType.INFORMATION);
                aa.setContentText("Comparisons completed");
                aa.show();
            });

        } catch (InterruptedException e) {
            exService.shutdownNow(); // prematurely shutdown all comparison jobs
        }

        System.out.println("Stopping Comparator... ");
    }

    private class ComparisonJob implements Runnable {

        private ComparisonPair comparisonPair;

        public ComparisonJob(ComparisonPair comparisonPair) {
            this.comparisonPair = comparisonPair;
        }

        @Override
        public void run() {
            System.out.println("Start Comparison Job");

            try {
                double sim = Helpers.calcSimilarity(
                        comparisonPair.getFile1Content(),
                        comparisonPair.getFile2Content()
                );

                // Log results
                ComparisonResult comparisonResult = new ComparisonResult(comparisonPair, sim);
                logger.putNextResult(comparisonResult);

                // Update GUI
                synchronized (mutex) {
                    comparisonJobsComplete++;
                    int nFiles = fileProducer.getNumFilesInDirectory();
                    int predictedNumJobs = (int)(0.5 * (Math.pow(nFiles, 2) - nFiles));
                    Platform.runLater(() -> {
                        guiJobText.setText(comparisonJobsComplete + "/" + predictedNumJobs + " Comparisons");
                        guiProgressBar.setProgress((double)(comparisonJobsComplete) / (predictedNumJobs));
                        guiTable.getItems().add(0, comparisonResult);
                    });
                }

            } catch (InterruptedException e) { /*Thread Finished*/ }

            System.out.println("Stop Comparison Job, #" + comparisonJobsComplete);
        }
    }
}
