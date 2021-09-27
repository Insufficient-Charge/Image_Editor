/*
 * Course: CS1021 - 041
 * Winter 2021
 * Lab 10 - Final Project III
 * Name: John Paul Bunn
 * Created: Feb 18 2021
 */
package bunnj;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static bunnj.Lab10Controller.errorAlert;

/**
 * This controller is meant to provide functionality for the kernel
 * window. It is coupled with the main controller.
 */
public class KernelController implements Initializable {

    //Kernel data
    private List<TextField> kernelFields = new ArrayList<>();

    @FXML private TextField kernel0 = new TextField();
    @FXML private TextField kernel1 = new TextField();
    @FXML private TextField kernel2 = new TextField();
    @FXML private TextField kernel3 = new TextField();
    @FXML private TextField kernel4 = new TextField();
    @FXML private TextField kernel5 = new TextField();
    @FXML private TextField kernel6 = new TextField();
    @FXML private TextField kernel7 = new TextField();
    @FXML private TextField kernel8 = new TextField();

    @FXML private Button applyButton = new Button();

    private double[] kernelData;

    private boolean allFieldsFilled = false;
    private boolean validFieldValues = false;

    //Image data
    private Image image;

    //Lab10Controller data
    private Lab10Controller lab10Controller;

    //Miscellaneous data
    private static final int KERNEL_FILTER_SIZE = 9;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        kernelFields.add(kernel0);
        kernelFields.add(kernel1);
        kernelFields.add(kernel2);
        kernelFields.add(kernel3);
        kernelFields.add(kernel4);
        kernelFields.add(kernel5);
        kernelFields.add(kernel6);
        kernelFields.add(kernel7);
        kernelFields.add(kernel8);

        applyButton.setDisable(true);

        for (TextField field : kernelFields) {
            field.textProperty().addListener((obs, oldtext, newtext) -> {
                allFieldsFilled = kernelFields.stream().allMatch(((f) -> f.getText().length() > 0));
                if (allFieldsFilled) {
                    try {
                        double sum = Arrays.stream(getKernel()).sum();
                        validFieldValues = (Arrays.stream(getKernel()).map(k -> k / sum).sum() > 0);
                    } catch (NumberFormatException e) {
                        validFieldValues = false;
                    }
                } else {
                    validFieldValues = false;
                }

                if (allFieldsFilled && validFieldValues) {
                    applyButton.setDisable(false);
                } else {
                    applyButton.setDisable(true);
                }
            });
        }
    }

    /**
     * This method applies the data from the kernel filter. It is only active
     * when a valid input is defined.
     * @param event
     */
    public void apply(ActionEvent event) {
        image = lab10Controller.getImage();
        if (image != null) {
            try {
                kernelData = getKernel();
                image = ImageIO.applyKernel(image, kernelData);
                lab10Controller.setImage(image);
            } catch (IllegalArgumentException e) {
                errorAlert("Error reading kernel data", "Error: " + e.getMessage());
            }
        } else {
            errorAlert("Error in image", "Error: there is an error with the image file");
        }
    }

    /**
     * This method pre-sets the kernel data for a blurred filter
     * @param event
     */
    public void blur(ActionEvent event) {
        kernel0.setText("0");
        kernel1.setText("1");
        kernel2.setText("0");

        kernel3.setText("1");
        kernel4.setText("5");
        kernel5.setText("1");

        kernel6.setText("0");
        kernel7.setText("1");
        kernel8.setText("0");
    }

    /**
     * This method pre-sets the kernel data for a sharpened filter
     * @param event
     */
    public void sharpen(ActionEvent event) {
        kernel0.setText("0");
        kernel1.setText("-1");
        kernel2.setText("0");

        kernel3.setText("-1");
        kernel4.setText("5");
        kernel5.setText("-1");

        kernel6.setText("0");
        kernel7.setText("-1");
        kernel8.setText("0");
    }

    private double[] getKernel() throws NumberFormatException {
        double[] kernelData = new double[KERNEL_FILTER_SIZE];
        for (int i = 0; i < kernelData.length; i++) {
            kernelData[i] = Double.parseDouble(kernelFields.get(i).getText());
        }
        return kernelData;
    }

    public void setLab8Controller(Lab10Controller lab10Controller) {
        this.lab10Controller = lab10Controller;
    }
}
