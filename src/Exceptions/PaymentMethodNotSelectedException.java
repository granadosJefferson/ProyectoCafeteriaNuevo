package Exceptions;

/**
 *
 * Excepción de validación usada cuando el usuario intenta continuar
 * sin haber seleccionado un método de pago en la UI.
 *
 * @author Jefferson
 */
public class PaymentMethodNotSelectedException extends UiValidationException {

    /**
     * Constructor: fija el mensaje estándar indicando que debe seleccionar un método de pago.
     */
    public PaymentMethodNotSelectedException() {
        super("Seleccione un método de pago (SINPE / Tarjeta / Efectivo).");
    }
}