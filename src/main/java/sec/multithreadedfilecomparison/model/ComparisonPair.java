package sec.multithreadedfilecomparison.model;

public class ComparisonPair {
    private final FileItem file1;
    private final FileItem file2;

    public ComparisonPair(FileItem file1, FileItem file2) {
        this.file1 = file1;
        this.file2 = file2;
    }

    public String getFile1Name() {
        return file1.getFileName();
    }

    public String getFile2Name() {
        return file2.getFileName();
    }

    public String getFile1Content() {
        return file1.getFileContent();
    }

    public String getFile2Content() {
        return file2.getFileContent();
    }

    @Override
    public String toString() {
        return "[" +
                file1.getFileName() + ", " +
                file2.getFileName() +
                "]";
    }
}
