package Exceptions;

/**
 *
 * Excepción personalizada para validaciones de UI.
 * Se usa para lanzar errores de entrada/datos en la interfaz sin obligar a manejarla (RuntimeException).
 *
 * @author Jefferson granados
 */
public class UiValidationException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo del error de validación.
     */
    public UiValidationException(String message) {
        super(message);
    }
}