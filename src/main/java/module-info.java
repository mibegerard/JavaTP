module org.javaproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires webcam.capture;
    requires java.desktop;
    requires org.geotools.main;
    requires org.geotools.referencing;
    requires org.json;
    requires org.geotools.opengis;
    requires org.locationtech.jts;
    requires org.geotools.shapefile;
    requires com.google.gson;

    // Open the package to allow JavaFX to reflectively access FXML controllers
    opens org.javaproject to javafx.fxml;

    // Export the base package
    exports org.javaproject;
}
