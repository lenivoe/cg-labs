package lab2.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import cg.mesh.ObjParser;
import lab2.SceneBuilder;
import cg.drawning3d.scene.SceneObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * JavaFX JFXApp
 */
public class JFXApp extends Application {
    private static SceneBuilder sceneBuilder;
    private static SceneObject demoObject;

    @Override
    public void start(Stage stage) throws IOException {
        var url = JFXApp.class.getResource("plot.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(url);

        var scene = new Scene(fxmlLoader.load());
        PlotController controller = fxmlLoader.getController();
        controller.initialize(sceneBuilder, demoObject);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }


    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("empty parameter: model name");
            return;
        }

        var parser = new ObjParser();
        var sceneObjects = Arrays.stream(args)
                .map(parser::parseMesh)
                .map(SceneObject::new)
                .collect(Collectors.toList());

        demoObject = sceneObjects.get(sceneObjects.size() - 1);
        sceneBuilder = new SceneBuilder(sceneObjects);

        launch();
    }
}