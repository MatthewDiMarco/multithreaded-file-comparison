package sec.multithreadedfilecomparison.model;

public class FileItem {
    private final String fileName;
    private final String fileContent;

    public FileItem () {
        this.fileName = null;
        this.fileContent = null;
    }

    public FileItem(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    @Override
    public boolean equals(Object inObj) {
        boolean valid = false;
        if (inObj instanceof FileItem) {
            FileItem inFileItem = (FileItem)inObj;
            if (inFileItem.getFileName().equals(fileName) &&
                inFileItem.getFileContent().equals(fileContent)
            ) {
                valid = true;
            }
        }

        return valid;
    }
}
