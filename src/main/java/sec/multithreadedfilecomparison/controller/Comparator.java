package sec.multithreadedfilecomparison.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class's responsibility is to:
 * Accept files from the File Scanner and arrange them into pairs, which are
 * submitted to a thread pool for similarity processing.
 * It should do this without creating any redundant combinations
 * (i.e. combinations that have already been submitted to the pool).
 */
public class Comparator implements Runnable {

    private static final String THREAD_NAME = "comparator-thread";
    private Thread thread;
    private List<String> fileHistory;
    private FileScanner producer;
    private ResultsLogger logger;

    public Comparator(FileScanner producer, ResultsLogger logger) {
        this.fileHistory = new ArrayList<String>();
        this.producer = producer;
        this.logger = logger;
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
     * Scan through the directory tree and stop at each file.
     * If the suffix is ok, extract the contents and push it to the queue.
     */
    @Override
    public void run() {
        System.out.println("Starting Comparator... ");

        // todo

        System.out.println("Stopping Comparator... ");
    }
}
