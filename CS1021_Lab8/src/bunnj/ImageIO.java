/*
 * Course: CS1021 - 041
 * Winter 2021
 * Lab 10 - Final Project III
 * Name: John Paul Bunn
 * Created: Feb 18 2021
 */
package bunnj;

import edu.msoe.cs1021.ImageUtil;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Model class for Image input and output management. Models the functionality of
 * reading and writing an image file.
 */
public class ImageIO {

    private static final int MAX_COLOR_LENGTH = 7;
    private static final int HEX_START_VALUE = 2;
    private static final int HEX_END_VALUE = 8;

    private static String header;
    private static int width;
    private static int height;

    /**
     * This method reads an image file from a given path
     * @param path the given path to the image file
     * @return an Image object found by the file
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws NoSuchElementException
     */
    public static Image read(Path path)
            throws IOException, IllegalArgumentException, NoSuchElementException {

        if (path == null) {
            throw new IOException("Null file reference");
        }

        String filePathString = path.toString().toLowerCase();

        if (filePathString.contains(".jpg") || filePathString.contains(".png")) {
            return ImageUtil.readImage(path);
        } else if (filePathString.contains(".msoe")) {
            return readMSOE(path);
        } else if (filePathString.contains(".bmsoe")) {
            return readBMSOE(path);
        }
        throw new IllegalArgumentException("Invalid extension");
    }

    /**
     * This method reads the specific text-to-image MSOE file
     * @param path the given path to the image file
     * @return an instance of the Image defined by the path
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws NoSuchElementException
     */
    public static Image readMSOE(Path path)
            throws IOException, IllegalArgumentException, NoSuchElementException {
        Scanner in = new Scanner(path);

        header = in.nextLine();
        if (!header.equals("MSOE") && !header.equals("BMSOE")) {
            throw new IOException("Invalid MSOE file: incorrect header");
        }

        String[] dimensions = in.nextLine().split("\\s");

        width = Integer.parseInt(dimensions[0]);
        height = Integer.parseInt(dimensions[1]);

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            String line = in.nextLine();
            String[] colors = line.split("  ");
            for (int x = 0; x < width; x++) {
                String color = colors[x];
                if (color.length() > 1) {
                    pixelWriter.setColor(x, y, stringToColor(color));
                }
            }
        }
        return image;
    }

    /**
     * This method reads the specific text-to-image MSOE file
     * @param path the given path to the image file
     * @return an instance of the Image defined by the path
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static Image readBMSOE(Path path) throws IOException, IllegalArgumentException {

        try (DataInputStream in = new DataInputStream(new FileInputStream(path.toString()))) {
            boolean hasB = in.readByte() == (byte)'B';
            boolean hasM = in.readByte() == (byte)'M';
            boolean hasS = in.readByte() == (byte)'S';
            boolean hasO = in.readByte() == (byte)'O';
            boolean hasE = in.readByte() == (byte)'E';
            if (!(hasB && hasM && hasS && hasO && hasE)) {
                throw new IllegalArgumentException("Invalid BMSOE header");
            } else {
                header = "BMSOE";
            }

            width = in.readInt();
            height = in.readInt();

            WritableImage image = new WritableImage(width, height);
            PixelWriter pixelWriter = image.getPixelWriter();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, intToColor(in.readInt()));
                }
            }

            return image;
        } catch (NullPointerException e) {
            throw new IOException("Error reading BMSOE file: " + e.getMessage());
        }
    }
    /**
     * This method writes to a given path defined by a given image
     * @param image the given image to be written
     * @param path the path the image will be written to
     * @throws IOException
     */
    public static void write(Image image, Path path) throws IOException {
        String filePathString = path.toFile().toString();
        try {
            boolean isInImagesDirectory = filePathString.contains("image");
            if (!isInImagesDirectory || !(filePathString.contains(".png") ||
                    filePathString.contains(".jpg") ||
                    filePathString.contains(".msoe") ||
                    filePathString.contains(".bmsoe"))) {
                throw new IOException("Invalid file destination");
            }
            ImageUtil.writeImage(path, image);
        } catch (IllegalArgumentException e) {

            if (filePathString.contains(".msoe")) {
                writeMSOE(image, path);
            } else {
                writeBMSOE(image, path);
            }
        }
    }

    /**
     * Writes the specific image-to-text MSOE file
     * @param image the given image to write
     * @param path the given path to write the image to
     * @throws IOException
     */
    public static void writeMSOE(Image image, Path path) throws IOException {
        try (PrintWriter writer = new PrintWriter(
                                    new FileWriter(
                                            new File(path.toString())), true)) {
            width = (int) image.getWidth();
            height = (int) image.getHeight();
            writer.println("MSOE");
            writer.println(width + " " + height);

            PixelReader pixelReader = image.getPixelReader();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = pixelReader.getColor(x, y);
                    String colorString = "#" +
                            color.toString().substring(HEX_START_VALUE, HEX_END_VALUE);
                    String message = (x == width-1) ? colorString : colorString + "  ";
                    writer.print(message);
                }

                if (y != height-1) {
                    writer.print("\n");
                }
            }
        }
    }

    /**
     * Writes the specific image-to-binary BMSOE file
     * @param image the given image to write
     * @param path the given path to write the image to
     * @throws IOException
     */
    public static void writeBMSOE(Image image, Path path) throws IOException {
        try (DataOutputStream writer = new DataOutputStream(
                new FileOutputStream(path.toString(), true))) {
            for (char c : "BMSOE".toCharArray()) {
                writer.writeByte((byte)c);
            }

            width = (int) image.getWidth();
            height = (int) image.getHeight();

            writer.writeInt(width);
            writer.writeInt(height);

            PixelReader pixelReader = image.getPixelReader();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writer.writeInt(colorToInt(pixelReader.getColor(x, y)));
                }
            }
        }
    }

    /**
     * This method will parse String data into a Color object
     * @param colorString the String to be parsed
     * @return the Color object based off of the String
     * @throws IOException thrown if an input was mismatched
     */
    public static Color stringToColor(String colorString) throws IOException {
        //Input validation
        if (colorString.charAt(0) != '#') {
            throw new IOException("Color does not start with '#'");
        } else if (colorString.length() != MAX_COLOR_LENGTH) {
            throw new IOException("Color was not 7 characters long");
        }

        for (int i = 1; i < colorString.length(); i++) {
            if (!Character.isDigit(colorString.charAt(i)) &&
                    colorString.charAt(i) < 'A' &&
                    colorString.charAt(i) > 'F') {
                throw new IOException("Color was not a valid hexadecimal digit");
            }
        }

        return Color.web(colorString);
    }

    /**
     * This method applies a kernel filter upon an image
     * @param image the image to be altered
     * @param kernelData the data from the kernel filter specifications
     * @return the newly altered image
     * @throws IllegalArgumentException
     */
    public static Image applyKernel(Image image, double[] kernelData)
            throws IllegalArgumentException {
        double sum = Arrays.stream(kernelData).sum();
        if (sum == 0) {
            throw new IllegalArgumentException("Error: sum is zero");
        }

        kernelData = Arrays.stream(kernelData)
                .map(k -> k / sum)
                .toArray();
        if (Arrays.stream(kernelData).sum() < 0) {
            throw new IllegalArgumentException(
                    "Error: values were not properly averaged to equal 1");
        }

        return ImageUtil.convolve(image, kernelData);
    }

    private static Color intToColor(int color) {
        double red = ((color >> 16) & 0x000000FF)/255.0;
        double green = ((color >> 8) & 0x000000FF)/255.0;
        double blue = (color & 0x000000FF)/255.0;
        double alpha = ((color >> 24) & 0x000000FF)/255.0;
        return new Color(red, green, blue, alpha);
    }

    private static int colorToInt(Color color) {
        int red = ((int)(color.getRed()*255)) & 0x000000FF;
        int green = ((int)(color.getGreen()*255)) & 0x000000FF;
        int blue = ((int)(color.getBlue()*255)) & 0x000000FF;
        int alpha = ((int)(color.getOpacity()*255)) & 0x000000FF;
        return (alpha << 24) + (red << 16) + (green << 8) + blue;
    }
}
