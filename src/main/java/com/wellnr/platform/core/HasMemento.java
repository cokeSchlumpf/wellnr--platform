package com.wellnr.platform.core;

/**
 * Markup interface to indicate that a value class has a memento. A memento is used to extract the state of
 * a value class/ object to store it (e.g., within a database). The memento can be used to restore the
 * state of an object.
 *
 * By convention, its expected that a class implementing the {@link HasMemento} interface has also a static
 * factory method which can be called by the system to instantiate an instance of the original object
 * from the memento.
 *
 * The factory method must have the signature:
 *
 * ```
 * CompletionStage<ORIGINAL_TYPE> fromMemento(PlatformContext context, MEMENTO_TYPE memento);
 * ```
 *
 * Example
 * -------
 *
 * Given we have the memento:
 *
 * ```
 * public class TextWindowMemento {
 *
 *     private String text;
 *
 *     public TextWindowMemento(String text) {
 *         this.text = text;
 *     }
 *
 *     public String getText() {
 *         return text;
 *     }
 * }
 * ```
 *
 * ```
 * public class TextWindow implements HasMemento<TextWindowMemento> {
 *
 *     private StringBuilder currentText;
 *
 *     public TextWindow() {
 *         this.currentText = new StringBuilder();
 *     }
 *
 *     public static CompletionStage<TextWindow> fromMemento(PlatformContext context TextWindowMemento memento) {
 *          var window = new TextWindow();
 *          window.currentText.append(memento.getText);
 *          return CompletableFuture.completedFuture(window);
 *     }
 *
 *     public void addText(String text) {
 *         currentText.append(text);
 *     }
 *
 *     public TextWindowMemento getMemento() {
 *         return new TextWindowMemento(currentText.toString());
 *     }
 * }
 * ```
 *
 * @param <T> The type of the memento.
 */
public interface HasMemento<T> {

    T getMemento();

}
