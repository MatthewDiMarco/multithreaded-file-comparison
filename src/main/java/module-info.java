module sec.multithreadedfilecomparison {
    requires javafx.controls;
    requires javafx.fxml;


    opens sec.multithreadedfilecomparison to javafx.fxml;
    exports sec.multithreadedfilecomparison;
}