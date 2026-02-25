package Exceptions;

/**
 *
 * Excepción de validación usada cuando un método de pago (no efectivo)
 * requiere referencia y el usuario no la proporciona.
 *
 * @author Jefferson
 */
public class MissingPaymentReferenceException extends UiValidationException {

    /**
     * Constructor: arma el mensaje indicando el método de pago que exige referencia.
     */
    public MissingPaymentReferenceException(String metodoPago) {
        super("La referencia es obligatoria para el método de pago '" + metodoPago + "'.");
    }
}