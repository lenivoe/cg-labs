package lab2.gui;

import cg.drawning3d.scene.IScene3d;
import cg.drawning3d.scene.ISceneObject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import lab2.SceneBuilder;
import cg.matrix.Axis;

import java.util.Arrays;

public class PlotController {
    private enum State {
        IDLE, ROTATION, TRANSLATION, SCALE, PROCESSING
    }

    @FXML private Canvas plot;
    @FXML private Canvas depthPlot;

    @FXML private Button acceptTransformationBtn;
    @FXML private Button cancelTransformationBtn;
    @FXML private Button centerBtn;
    @FXML private ComboBox<IScene3d.ProjectionType> projectionComboBox;

    @FXML private Tab rotationTab;
    @FXML private Tab translationTab;
    @FXML private Tab scaleTab;

    @FXML private Slider rotationX;
    @FXML private Slider rotationY;
    @FXML private Slider rotationZ;

    @FXML private Slider translationX;
    @FXML private Slider translationY;
    @FXML private Slider translationZ;

    @FXML private Slider scaleX;
    @FXML private Slider scaleY;
    @FXML private Slider scaleZ;


    private State state = State.IDLE;
    private IScene3d scene;
    private ISceneObject demoObject;
    private ISceneObject.IState demoObjectState;


    public void initialize(SceneBuilder sceneBuilder, ISceneObject demoObject) {
        scene = sceneBuilder.buildScene(plot, depthPlot);
        this.demoObject = demoObject;
        demoObjectState = demoObject.saveState();

        initButtons();
        initRotationSliders();
        initOtherSliders();
        initComboBox();

        scene.render();
    }

    private void initButtons() {
        EventHandler<ActionEvent> resetControls = e -> {
            if (state == State.IDLE) {
                return;
            }

            assert state != State.PROCESSING : "state is PROCESSING yet";

            state = State.PROCESSING;

            acceptTransformationBtn.setDisable(true);
            cancelTransformationBtn.setDisable(true);

            rotationX.setDisable(false);
            rotationY.setDisable(false);
            rotationZ.setDisable(false);

            rotationTab.setDisable(false);
            translationTab.setDisable(false);
            scaleTab.setDisable(false);

            var defaultAngle = (rotationX.getMax() + rotationX.getMin()) / 2;
            rotationX.setValue(defaultAngle);
            rotationY.setValue(defaultAngle);
            rotationZ.setValue(defaultAngle);

            var defaultShift = (translationX.getMax() + translationX.getMin()) / 2;
            translationX.setValue(defaultShift);
            translationY.setValue(defaultShift);
            translationZ.setValue(defaultShift);

            var defaultScale = 1 + (scaleX.getMax() + scaleX.getMin()) / 2;
            scaleX.setValue(defaultScale);
            scaleY.setValue(defaultScale);
            scaleZ.setValue(defaultScale);

            state = State.IDLE;

            demoObjectState.restore();
            scene.render();
        };

        cancelTransformationBtn.setOnAction(resetControls);
        acceptTransformationBtn.setOnAction(e -> {
            demoObjectState = demoObject.saveState();
            resetControls.handle(e);
        });

        centerBtn.setOnAction(e -> {
            demoObject.fitIntoScene();
            demoObjectState = demoObject.saveState();
            scene.render();
        });
    }

    private void initRotationSliders() {
        ChangeListener<Number> onSlide = (observable, oldValue, newValue) -> {
            if(state == State.PROCESSING) {
                return;
            }

            assert state == State.IDLE || state == State.ROTATION
                    : "wrong state inside rotation callback: " + state;

            var property = (DoubleProperty)observable;
            var slider = (Slider)property.getBean();

            if (state == State.IDLE) {
                acceptTransformationBtn.setDisable(false);
                cancelTransformationBtn.setDisable(false);

                translationTab.setDisable(true);
                scaleTab.setDisable(true);

                rotationX.setDisable(rotationX != slider);
                rotationY.setDisable(rotationY != slider);
                rotationZ.setDisable(rotationZ != slider);

                state = State.ROTATION;
            }

            Axis axis;
            if (slider == rotationX) {
                axis = Axis.X;
            } else if (slider == rotationY) {
                axis = Axis.Y;
            } else if (slider == rotationZ) {
                axis = Axis.Z;
            } else {
                throw new RuntimeException("unexpected slider");
            }

            var angle = Math.toRadians(slider.getValue());
            demoObjectState.restore();
            demoObject.rotate(angle, axis);
            scene.render();
        };

        rotationX.valueProperty().addListener(onSlide);
        rotationY.valueProperty().addListener(onSlide);
        rotationZ.valueProperty().addListener(onSlide);
    }

    private void initOtherSliders() {
        ChangeListener<Number> onSlide = (observable, oldValue, newValue) -> {
            if(state == State.PROCESSING) {
                return;
            }

            var property = (DoubleProperty)observable;
            var slider = (Slider)property.getBean();

            var translationList = Arrays.asList(translationX, translationY, translationZ);
            var scaleList = Arrays.asList(scaleX, scaleY, scaleZ);

            if (state == State.IDLE) {
                state = scaleList.contains(slider) ? State.SCALE : State.TRANSLATION;

                assert state == State.SCALE || translationList.contains(slider);

                acceptTransformationBtn.setDisable(false);
                cancelTransformationBtn.setDisable(false);

                rotationTab.setDisable(true);
                translationTab.setDisable(state != State.TRANSLATION);
                scaleTab.setDisable(state != State.SCALE);
            }

            var sliderList = state == State.SCALE ? scaleList : translationList;
            var middleValue = (sliderList.get(0).getMax() + sliderList.get(0).getMin()) / 2;
            var x = sliderList.get(0).getValue() - middleValue;
            var y = sliderList.get(1).getValue() - middleValue;
            var z = sliderList.get(2).getValue() - middleValue;

            demoObjectState.restore();
            if (state == State.TRANSLATION) {
                demoObject.translate(x, y, z);
            } else {
                demoObject.scale(x, y, z);
            }
            scene.render();
        };

        var sliders = Arrays.asList(translationX, translationY, translationZ, scaleX, scaleY, scaleZ);
        for (var slider : sliders) {
            slider.valueProperty().addListener(onSlide);
        }
    }

    private void initComboBox() {
        var items = FXCollections.observableArrayList(IScene3d.ProjectionType.values());
        projectionComboBox.setItems(items);
        projectionComboBox.setValue(IScene3d.ProjectionType.ORTHOGRAPHIC);
        scene.setProjection(projectionComboBox.getValue());
        projectionComboBox.setOnAction(e -> {
            scene.setProjection(projectionComboBox.getValue());
            scene.render();
        });
    }
}
