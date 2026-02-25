package Exceptions;

/**
 *
 * @author Daniel Araya
 *
 * Excepción personalizada que se lanza cuando el correo electrónico
 * ingresado no cumple con el formato esperado.
 *
 * En este caso específico, valida que el correo termine en "@gmail.com".
 *
 * Extiende de RowValidationException, permitiendo mantener una jerarquía
 * de validaciones personalizadas para formularios o filas de datos.
 */
public class InvalidEmailException extends RowValidationException {

    /**
     * Constructor por defecto.
     * 
     * Define el mensaje estándar que se mostrará cuando
     * el correo no termine en "@gmail.com".
     */
    public InvalidEmailException() {
        super("El correo debe terminar en '@gmail.com'.");
    }
}