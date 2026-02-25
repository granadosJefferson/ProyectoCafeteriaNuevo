package Exceptions;

/**
 *
 * Excepción de validación usada cuando se está pagando por mesa
 * y la cédula ingresada no coincide con ninguna cédula de los pedidos de esa mesa.
 *
 * @author Jefferson
 */
public class CedulaNotAllowedForMesaException extends UiValidationException {

    /**
     * Constructor: fija el mensaje estándar para cédula no autorizada en modo mesa.
     */
    public CedulaNotAllowedForMesaException() {
        super("La cédula no pertenece a ninguno de los pedidos de esta mesa.");
    }
}