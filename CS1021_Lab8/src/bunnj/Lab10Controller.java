/*
 * Course: CS1021 - 041
 * Winter 2021
 * Lab 9 - Final Project II
 * Name: John Paul Bunn
 * Created: Feb 14 2021
 */
package bunnj;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Controller class for the program, which assigns functionality
 * for modifying the JavaFX nodes and modifying the primary image
 */
public class Lab10Controller implements Initializable {

    //Image View data
    @FXML private ImageView imageView = new ImageView();
    @FXML private ImageView originalImageView = new ImageView();

    //Kernel data
    @FXML private Button showFilterButton = new Button();
    private Stage kernelStage;

    //History data
    private Stack<Image> imageHistory = new Stack<>();
    private Stack<Image> redoHistory = new Stack<>();

    //Slideshow animation data
    @FXML private HBox slideshowBar = new HBox();
    private List<Image> slides = new ArrayList<>();
    private final int DEFAULT_SLIDE_LENGTH = 1000;
    private boolean slideshowAlreadyActive = false;

    //File data
    private FileChooser fileChooser = new FileChooser();
    private File file;
    private Path filePath;
    private Image reloadImage;
    private Image image;

    //Control variables
    private static boolean pathNotExist = true;
    private static boolean badExtension = true;

    //Functionals
    private final Transformable<Integer, Color> grayscale = (y, color) -> color.grayscale();
    private final Transformable<Integer, Color> negative = (y, color) -> color.invert();
    private final Transformable<Integer, Color> brighten = (y, color) -> color.brighter();
    private final Transformable<Integer, Color> darken = (y, color) -> color.darker();
    private final Transformable<Integer, Color> saturate = (y, color) -> color.saturate();
    private final Transformable<Integer, Color> desaturate = (y, color) -> color.desaturate();
    private final Transformable<Integer, Color> red = (y, color) -> new Color(
                                                        color.getRed(), 0, 0, color.getOpacity());
    private final Transformable<Integer, Color> redgray = (y, color) -> (y % 2 == 0) ?
                                                red.apply(y, color) : grayscale.apply(y, color);
    private final Transformable<Integer, Color> paintRed = (y, color) -> new Color(
                                                        color.getRed(), 0, 0, color.getOpacity());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Imageview initialization
        imageView.setPickOnBounds(true);

        //Set the initial filepath
        filePath = Paths.get(System.getProperty("user.dir") + "/images");

        //Assign the filechooser appropriately and determine if the path exists
        if (filePath.toFile().exists()) {
            fileChooser.setTitle("Open Input File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir") + "/images"));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Image Files", "*.jpg", "*.png", "*.msoe", "*.bmsoe"),
                    new FileChooser.ExtensionFilter("JPG Files", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                    new FileChooser.ExtensionFilter("MSOE Files", "*.msoe"),
                    new FileChooser.ExtensionFilter("BMSOE Files", "*.bmsoe")
            );

            pathNotExist = false;
        } else {
            pathNotExist = true;
        }
    }

    /**
     * This method loads the image file through use of a FileChooser
     * @param event the event calling the method
     */
    public void load(ActionEvent event) {
        if (filePath.toFile().exists()) {
            file = fileChooser.showOpenDialog(null);

            //Check to see if the file is null
            try {
                if (file == null) {
                    throw new IOException("File was not chosen");
                }

                //Set a new reload file, and a primary image
                image = ImageIO.read(file.toPath());
                reloadImage = image;

                //Clear undo/redo stacks
                imageHistory.clear();
                redoHistory.clear();
                imageHistory.push(image);

                //Set views
                setImageView(image);
                setOriginalImageView(image);

                //Confirm a working extension
                badExtension = false;

            } catch (IOException e) {
                errorAlert("Error reading file", "Error: " + e.getMessage());
            } catch (IllegalArgumentException | NoSuchElementException e) {
                errorAlert("Error in file extension", "Error: " + e.getMessage());
                badExtension = true;
            }
        } else {
            errorAlert("Correct file path does not exist", "Error: Invalid file path");
        }
    }

    /**
     * This method saves the current image to a local file, with an
     * option to overwrite the current instance of the file
     * @param event the event calling the method
     */
    public void save(ActionEvent event) {
        if (!pathNotExist && !badExtension && image != null) {
            //Write the image
            try {
                Path newPath = fileChooser.showSaveDialog(null).toPath();
                ImageIO.write(image, newPath);
            } catch (IOException|NullPointerException e) {
                errorAlert("Error writing file", "Error: " + e.getMessage());
            }

        } else if (pathNotExist) {
            errorAlert("Path does not exist", "Error: cannot save without images folder");
        } else if (image == null) {
            errorAlert("Error in image", "Error: there is an error with the image file");
        } else {
            errorAlert("Bad extension", "Error: cannot save with an invalid extension");
        }
    }

    /**
     * Reloads the current image to what it originally was
     * @param event the event calling the method
     */
    public void reload(ActionEvent event) {
        //If the image is not null (it exists), set the old file to the new file
        if (image != null) {
            imageHistory.push(image);
            image = reloadImage;
            setImageView(image);
        } else {
            errorAlert("Error in image", "Error: there is a problem with the image");
        }
    }

    private Image transformImage(Image image, Transformable<Integer, Color> transform) {
        if (image != null) {
            //Create a copy of the image in a writable form
            PixelReader pixelReader = image.getPixelReader();
            WritableImage writableImage = new WritableImage(image.getPixelReader(),
                    (int) image.getWidth(),
                    (int) image.getHeight());
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            //Apply a transformable operation on every pixel
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pixelWriter.setColor(x, y, transform.apply(y, pixelReader.getColor(x, y)));
                }
            }
            image = writableImage;
        } else {
            errorAlert("Error reading image",
                    "Error: cannot apply transformation to a nonexistent image");
        }

        return image;
    }

    /**
     * This method will transform a single pixel upon click
     *
     * @param mouseEvent the event calling the method
     * @param image the image being transformed
     * @param transform the transformation to be applied
     * @return the altered image with a single pixel changed
     * @throws NullPointerException thrown if the image is null
     * @throws IndexOutOfBoundsException thrown if a draw is attempted
     *                                  on an out of bounds space
     */
    public Image transformPixelOnClick(MouseEvent mouseEvent, Image image,
                                       Transformable<Integer, Color> transform)
                                        throws NullPointerException, IndexOutOfBoundsException {
        if (image != null) {
            double imageViewWidth = imageView.getFitWidth();
            double imageViewHeight = imageView.getFitHeight();

            double widthScale = imageViewWidth / image.getWidth();
            double heightScale = imageViewHeight / image.getHeight();

            int x = (int) (mouseEvent.getX() / widthScale);
            int y = (int) (mouseEvent.getY() / heightScale);

            WritableImage writableImage = new WritableImage(
                    image.getPixelReader(), (int)image.getWidth(), (int)image.getHeight());
            writableImage.getPixelWriter().setColor(
                    x, y, transform.apply(y, writableImage.getPixelReader().getColor(x, y)));

            return writableImage;
        }
        throw new NullPointerException("Null image selected");
    }

    /**
     * If this method is called by the Grayscale button, then
     * this method will apply a grayscale transformation
     * @param event the event calling the method
     */
    public void grayscale(ActionEvent event) {
        setImage(transformImage(image, grayscale));
    }

    /**
     * If this method is called by the Negative button, then
     * this method will apply a negative transformation
     * @param event the event calling the method
     */
    public void negative(ActionEvent event) {
        setImage(transformImage(image, negative));
    }

    /**
     * This method will perform a brighten transformation on the image object
     * @param event the event calling the method
     */
    public void brighten(ActionEvent event) {
        setImage(transformImage(image, brighten));
    }

    /**
     * This method will perform a darken transformation on the image object
     * @param event the event calling the method
     */
    public void darken(ActionEvent event) {
        setImage(transformImage(image, darken));
    }

    /**
     * This method will perform a desaturate transformation on the image object
     * @param event the event calling the method
     */
    public void desaturate(ActionEvent event) {
        setImage(transformImage(image, desaturate));
    }

    /**
     * This method will perform a saturate transformation on the image object
     * @param event the event calling the method
     */
    public void saturate(ActionEvent event) {
        setImage(transformImage(image, saturate));
    }

    /**
     * This method will perform a red transformation on the image object
     * @param event the event calling the method
     */
    public void red(ActionEvent event) {
        setImage(transformImage(image, red));
    }

    /**
     * This method will perform a redgray transformation on the image object
     * @param event the event calling the method
     */
    public void redgray(ActionEvent event) {
        setImage(transformImage(image, redgray));
    }

    /**
     * This method will color the pixel the mouse is on to be red
     * @param mouseEvent the event calling the method
     */
    public void colorPixelRed(MouseEvent mouseEvent) {
        try {
            image = transformPixelOnClick(mouseEvent, image, paintRed);
            setImageView(image);
        } catch (NullPointerException|IndexOutOfBoundsException e) {
            errorAlert("Error in image drawing", "Error: " + e.getClass() + ": " + e.getMessage());
        }
    }

    /**
     * This method will maintain the history of the image when the mouse is released
     * @param mouseEvent the event calling the method
     */
    public void maintainImageHistory(MouseEvent mouseEvent) {
        if (image != null) {
            imageHistory.push(image);
        }
    }

    /**
     * This method undoes the last image transformation
     */
    public void undo() {
        if (!imageHistory.empty()) {
            redoHistory.push(image);
            image = imageHistory.pop();
            setImageView(image);
        }
    }

    /**
     * This method redoes an undone transformation
     */
    public void redo() {
        if (!redoHistory.empty()) {
            imageHistory.push(image);
            image = redoHistory.pop();
            setImageView(image);
        }
    }

    /**
     * This method will cause the kernel to be displayed
     * @param event the event calling the method
     */
    public void showKernel(ActionEvent event) {
        if (kernelStage != null) {
            kernelStage.show();
            showFilterButton.setText("Hide Filter");
            showFilterButton.setOnAction(this::hideKernel);
        }
    }

    /**
     * This method will cause the kernel to be hidden
     * @param event the event calling the method
     */
    public void hideKernel(ActionEvent event) {
        if (kernelStage != null) {
            kernelStage.hide();
            showFilterButton.setText("Show Filter");
            showFilterButton.setOnAction(this::showKernel);
        }
    }

    public void addCurrentImageToSlideshow(ActionEvent event) {
        addToSlideshow(this.getImage());
    }

    public void removeFromSlideshow(ActionEvent event) {
        List<Node> slideshowNodes = slideshowBar.getChildren();
        if (slideshowNodes.size() > 0) {
            slideshowNodes.remove(slideshowNodes.size()-1);
        }
    }

    public void playSlides(ActionEvent event) {
        List<Node> slideshowNodes = slideshowBar.getChildren();

        slideshowAlreadyActive = true;
        int i = 0;
        Timeline t = new Timeline();
        t.getKeyFrames().add(new KeyFrame(
                Duration.millis(DEFAULT_SLIDE_LENGTH),
                e -> {
                    setImageView(slides.get(i));
                }
        ));



    }

    //HELPER METHODS
    /**
     * This method wll set the stage and controller of the kernel to local
     * instances of the main controller; this is for coupled functionality
     * between these classes
     * @param kernelStage the instance of the kernel stage
     * @param kernelController the instance of the kernel controller
     */
    public void setKernel(Stage kernelStage, KernelController kernelController) {
        this.kernelStage = kernelStage;
        kernelController.setLab8Controller(this);
    }

    private void setImageView(Image image) {
        imageView.setImage(image);
    }

    private void setOriginalImageView(Image image) {
        originalImageView.setImage(image);
    }

    /**
     * This method is a blueprint for other error methods
     *
     * @param header the header text for the error
     * @param context the context text for the error
     */
    public static void errorAlert(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialogue");
        alert.setHeaderText(header);
        alert.setContentText(context);

        alert.showAndWait();
    }

    /**
     * This method will display an alert for the "about" message
     */
    public void aboutAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("About This Program");
        alert.setContentText(
                "Author: John Paul Bunn\n" +
                "Class: CS1021 041\n" +
                "Professor: Roby Velez\n\n" +
                "Highlights: This program can be used to store and edit images in " +
                "real time by applying filters. Additionally, red lines can be drawn " +
                "directly onto the new image, and undo/redo functionality is implemented. " +
                "Any other custom filter can be set through the kernel. This program can process " +
                ".jpg, .png, .msoe, and .bmsoe files.");
        alert.showAndWait();
    }

    public Image getImage() {
        return image;
    }

    /**
     * Sets the image object and pushes the previous image into the history stack
     * @param image the new image to be set
     */
    public void setImage(Image image) {
        imageHistory.push(image);
        redoHistory.clear();
        this.image = image;
        setImageView(this.image);
    }

    public void addToSlideshow(Image image) {
        if (image != null) {
            List<Node> slideshowNodes = slideshowBar.getChildren();
            slides.add(image);

            ImageView newView = new ImageView();
            newView.setImage(image);
            newView.setFitWidth(50);
            newView.setFitHeight(50);
            newView.setPreserveRatio(true);

            slideshowNodes.add(newView);
        } else {
            errorAlert("Error in image", "Error: there is an error with the image file");
        }
    }

    public void playAllSlides(int index) {

    }
}