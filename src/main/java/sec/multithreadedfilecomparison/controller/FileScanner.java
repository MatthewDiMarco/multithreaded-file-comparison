package sec.multithreadedfilecomparison.controller;

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
 * Provided the file is not empty, and features one of the user configured suffixes, then
 * extract the text and submit it to a blocking queue for other threads to access.
 */
public class FileScanner implements Runnable {

    private static final String THREAD_NAME = "file-scanner-thread";
    private static final String POISON = new String();
    private static final int QUEUE_CAP = 10;
    private File directoryPath;
    private Set<String> suffixes;
    private BlockingQueue<String> fileQueue;
    private Thread thread;

    public FileScanner(File directoryPath) {
        this.directoryPath = directoryPath;
        this.suffixes = Set.of("txt");
        this.fileQueue = new ArrayBlockingQueue<String>(QUEUE_CAP);
    }

    public FileScanner(File directoryPath, Set<String> suffixes) {
        this.directoryPath = directoryPath;
        this.suffixes = suffixes;
        this.fileQueue = new ArrayBlockingQueue<String>(QUEUE_CAP);
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
     * Scan through the directory tree and stop at each file.
     * If the suffix is ok, extract the contents and push it to the queue.
     */
    @Override
    public void run() {
        System.out.println("Starting File Scanner... ");

        try {
            Files.walkFileTree(Paths.get(directoryPath.getPath()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        String[] elements = file.getFileName().toString().split("\\.");
                        if (elements.length >= 2 && suffixes.contains(elements[1])) { //todo: collapse strings
                            Thread.sleep(500); // temp
                            fileQueue.put(readFileContents(file));
                        }

                    } catch (IOException e) {
                        System.out.println("File Scanner ERROR: " + e.getMessage());

                    } catch (InterruptedException e) {}

                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            System.out.println("File Scanner ERROR: " + e.getMessage());
        }

        System.out.println("Stopping File Scanner... ");
    }

    /**
     * Pop's the next item of file contents in the blocking queue.
     * @return File Contents
     * @throws InterruptedException
     */
    public String getNextFile() throws InterruptedException {
        String fileContents = fileQueue.take();
        if (fileContents == POISON) {
            fileContents = null;
        }

        return fileContents;
    }

    /**
     * Read and extract the contents of a file.
     * @param path Target File
     * @return File Contents
     * @throws IOException
     */
    private String readFileContents(Path path) throws IOException {
        // todo
        System.out.println(path.toString());
        return "Test: " + path.toString();
    }
}
