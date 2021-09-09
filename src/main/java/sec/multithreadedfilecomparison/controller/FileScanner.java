package sec.multithreadedfilecomparison.controller;

import sec.multithreadedfilecomparison.model.FileItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class's responsibility is to:
 * Scan the given directory tree for all files.
 * Provided the file is not empty, and it ends in one of the user configured suffixes, then
 * extract the text and submit it to a blocking queue for other threads to access.
 */
public class FileScanner implements Runnable {

    private static final String THREAD_NAME = "file-scanner-thread";
    private static final FileItem POISON = new FileItem();
    private static final int QUEUE_CAP = 10;
    private Thread thread;
    private File directoryPath;
    private Set<String> suffixes;
    private BlockingQueue<FileItem> fileQueue;
    private int numFilesInDirectory;

    public FileScanner(File directoryPath) {
        this.directoryPath = directoryPath;
        this.suffixes = Set.of("txt");
        this.fileQueue = new ArrayBlockingQueue<FileItem>(QUEUE_CAP);
        this.numFilesInDirectory = 0;
    }

    public FileScanner(File directoryPath, Set<String> suffixes) {
        this.directoryPath = directoryPath;
        this.suffixes = suffixes;
        this.fileQueue = new ArrayBlockingQueue<FileItem>(QUEUE_CAP);
        this.numFilesInDirectory = 0;
    }

    /**
     * Start scanning files in a separate thread.
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
     * Pop's the next item of file contents in the blocking queue.
     * @return File Contents
     * @throws InterruptedException Interrupt
     */
    public FileItem getNextFile() throws InterruptedException {
        FileItem fileItem = fileQueue.take();
        if (fileItem == POISON) {
            fileItem = null;
        }

        return fileItem;
    }

    /**
     * Get the size of the queue.
     * @return Number of items in queue
     * @throws InterruptedException Interrupt
     */
    public int getNumFilesInDirectory() {
        return numFilesInDirectory;
    }

    /**
     * Read and extract the contents of a file.
     * @param path Target File
     * @return File Contents
     * @throws IOException Reading Error
     */
    private String readFileContents(Path path) {
        String content;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            System.out.println("File Scanner ERROR: " + e.getMessage());
            content = "ERR: *Failed to extract file text*";
        }

        return content;
    }

    /**
     * Scan through the directory tree and stop at each file.
     * If the suffix is ok, extract the contents and push it to the queue.
     */
    @Override
    public void run() {
        System.out.println("Starting File Scanner... ");

        try {
            try {
                numFilesInDirectory = directoryPath.list().length;
                Files.walkFileTree(Paths.get(directoryPath.getPath()), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            String[] elements = file.getFileName().toString().split("\\.");
                            if (elements.length >= 2 && suffixes.contains(elements[1])) { // todo: collapse strings
                                Thread.sleep((int)((Math.random() * (500 - 100)) + 100)); // temp
                                fileQueue.put(new FileItem(elements[0], readFileContents(file)));
                            }

                        } catch (InterruptedException e) {}

                        return FileVisitResult.CONTINUE;
                    }
                });

            } catch (IOException e) {
                System.out.println("File Scanner ERROR: " + e.getMessage());

            } finally {
                fileQueue.put(POISON);
            }

        } catch (InterruptedException e) {}

        System.out.println("Stopping File Scanner... ");
    }
}
