module lab_1st {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires kotlin.stdlib;
    requires computer_graphic;

    opens lab1.gui to javafx.controls, javafx.fxml, javafx.graphics;
    exports lab1;
}