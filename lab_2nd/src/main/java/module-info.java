module lab_2nd {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires kotlin.stdlib;
    requires computer_graphic;

    opens lab2.gui to javafx.controls, javafx.fxml, javafx.graphics;
    exports lab2;
}