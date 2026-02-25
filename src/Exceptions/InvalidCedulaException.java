package Exceptions;

public class InvalidCedulaException extends UiValidationException {
    public InvalidCedulaException() {
        super("La cédula del pagador debe contener números y no puede estar vacía.");
    }
}