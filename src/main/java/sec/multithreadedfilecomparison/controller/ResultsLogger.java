package sec.multithreadedfilecomparison.controller;

import sec.multithreadedfilecomparison.model.ComparisonResult;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class's responsibility is to:
 * Update the results file.
 */
public class ResultsLogger implements Runnable {

    private static final String THREAD_NAME = "results-logger-thread";
    private static final int QUEUE_CAP = 10;
    private Thread thread;
    private String outputFileName;
    private BlockingQueue<ComparisonResult> resultsQueue;

    public ResultsLogger(String outputFileName) {
        this.outputFileName = outputFileName;
        this.resultsQueue = new ArrayBlockingQueue<ComparisonResult>(QUEUE_CAP);
    }

    /**
     * Start waiting for log requests in another thread.
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
     * Add a result to the queue for logging.
     * @param comparisonResult The Result
     * @throws InterruptedException Interrupt
     */
    public void putNextResult(ComparisonResult comparisonResult) throws InterruptedException {
        resultsQueue.put(comparisonResult);
    }

    /**
     * Keep checking the queue for any new results to write.
     * This task continues until interrupted.
     */
    @Override
    public void run() {
        System.out.println("Starting Results Logger... ");

        try {
            boolean running = true;
            while (running) {
                ComparisonResult cr = resultsQueue.take();
                System.out.println("Results Logger: " + cr.toString());
                // todo
            }

        } catch (InterruptedException e) { /*Thread Finished*/ }

        System.out.println("Stopping Results Logger... ");
    }
}
