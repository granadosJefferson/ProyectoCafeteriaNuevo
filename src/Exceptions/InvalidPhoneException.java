package Exceptions;

/**
 *
 * @author Daniel Araya
 *
 * Excepción personalizada que se lanza cuando el número de teléfono
 * ingresado no cumple con el formato esperado.
 *
 * Se utiliza dentro del sistema de validaciones para formularios,
 * verificando que el teléfono contenga únicamente números y que
 * cumpla con las reglas definidas como válidas en el sistema.
 *
 * Extiende de RowValidationException para mantener coherencia
 * dentro de la jerarquía de validaciones personalizadas.
 */
public class InvalidPhoneException extends RowValidationException {

    /**
     * Constructor por defecto.
     * 
     * Define el mensaje estándar que se mostrará cuando el teléfono
     * no sea válido.
     */
    public InvalidPhoneException() {
        super("El teléfono debe contener solo números y ser válido.");
    }
}