package Exceptions;

/**
 *
 * @author Daniel Araya
 *
 * Excepción personalizada que se lanza cuando un campo obligatorio
 * no ha sido completado por el usuario.
 *
 * Se utiliza dentro del sistema de validaciones para asegurar que
 * los campos requeridos (por ejemplo: cédula, nombre, tipo, etc.)
 * no estén vacíos antes de procesar o guardar información.
 *
 * Extiende de RowValidationException para mantener una jerarquía
 * unificada de validaciones personalizadas.
 */
public class RequiredFieldException extends RowValidationException {

    /**
     * Constructor que recibe el nombre del campo obligatorio
     * que no fue completado.
     *
     * @param fieldName nombre del campo requerido
     */
    public RequiredFieldException(String fieldName) {
        super("El campo '" + fieldName + "' es obligatorio.");
    }
}