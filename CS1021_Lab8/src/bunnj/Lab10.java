/*
 * Course: CS1021 - 041
 * Winter 2021
 * Lab 10 - Final Project III
 * Name: John Paul Bunn
 * Created: Feb 18 2021
 */
package bunnj;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main class for the program. Implements the view.
 */
public class Lab10 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader primaryLoader = new FXMLLoader(getClass().getResource("Lab10.fxml"));
        Parent root = primaryLoader.load();
        Lab10Controller controller = primaryLoader.getController();
        primaryStage.setTitle("Image Manipulator");

        FXMLLoader kernelLoader = new FXMLLoader(getClass().getResource("kernelUI.fxml"));
        Stage kernelStage = new Stage();
        Parent kernelRoot = kernelLoader.load();
        kernelStage.setTitle("Image Manipulator");
        kernelStage.setScene(new Scene(kernelRoot));

        //Create coupling between the kernelController and primary controller
        controller.setKernel(kernelStage, kernelLoader.getController());

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
