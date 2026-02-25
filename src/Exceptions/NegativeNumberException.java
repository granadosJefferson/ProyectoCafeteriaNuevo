package Exceptions;

/**
 *
 * @author Daniel Araya
 *
 * Excepción personalizada que se lanza cuando un campo numérico
 * contiene un valor negativo no permitido.
 *
 * Se utiliza dentro del sistema de validaciones para asegurar que
 * ciertos campos (por ejemplo: visitas, total, cantidades, etc.)
 * no acepten valores menores a cero.
 *
 * Extiende de RowValidationException para mantener una jerarquía
 * común en las validaciones personalizadas del sistema.
 */
public class NegativeNumberException extends RowValidationException {

    /**
     * Constructor que recibe el nombre del campo que presentó
     * el valor negativo.
     *
     * @param fieldName nombre del campo que no puede ser negativo
     */
    public NegativeNumberException(String fieldName) {
        super("El campo '" + fieldName + "' no puede ser negativo.");
    }
}