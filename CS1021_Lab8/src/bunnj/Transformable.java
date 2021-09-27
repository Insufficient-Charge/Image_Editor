/*
 * Course: CS1021 - 041
 * Winter 2021
 * Lab 10 - Final Project III
 * Name: John Paul Bunn
 * Created: Feb 18 2021
 */
package bunnj;

/**
 * This interface defines the functional methodology of the Transformable
 * type, which will allow for methods to be applied to a specific Color
 * object.
 * @param <T> a given Y value for custom applications depending on the
 *                 value
 * @param <G> The color to be acted upon
 */
@FunctionalInterface public interface Transformable<T, G> {
    /**
     * This method applies a method to a particular Color object, with the
     * possibility of differentiating between methods determined by the Y
     * value.
     *
     * Please note that Java Generics are used to satisfy the specifications
     * of CheckStyle
     *
     * @param pixelY the given Y value
     * @param color the Color object to be acted upon
     * @return the new Color object instance
     */
    G apply(T pixelY, G color);
}
