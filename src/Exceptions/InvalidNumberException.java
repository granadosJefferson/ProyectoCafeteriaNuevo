package Exceptions;

/**
 *
 * @author Daniel Araya
 *
 * Excepción personalizada que se lanza cuando un campo numérico
 * no contiene un valor válido.
 *
 * Se utiliza dentro del sistema de validaciones para formularios,
 * permitiendo indicar específicamente qué campo presenta el error.
 *
 * Extiende de RowValidationException para mantener una jerarquía
 * común de validaciones personalizadas.
 */
public class InvalidNumberException extends RowValidationException {

    /**
     * Constructor que recibe el nombre del campo que generó el error.
     *
     * @param fieldName nombre del campo que debe contener un número válido
     */
    public InvalidNumberException(String fieldName) {
        super("El campo '" + fieldName + "' debe ser un número válido.");
    }
}