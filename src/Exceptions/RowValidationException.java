package Exceptions;

/**
 *
 * @author Daniel Araya
 *
 * Clase base para todas las excepciones personalizadas relacionadas
 * con validaciones de datos en filas o formularios del sistema.
 *
 * Esta clase extiende de Exception y permite crear una jerarquía
 * de validaciones específicas como:
 * - RequiredFieldException
 * - InvalidNumberException
 * - NegativeNumberException
 * - InvalidEmailException
 * - InvalidPhoneException
 *
 * Su objetivo es centralizar los errores de validación y permitir
 * un manejo uniforme mediante bloques try-catch en los controladores.
 */
public class RowValidationException extends Exception {

    /**
     * Constructor que recibe el mensaje de error personalizado.
     *
     * @param message descripción del error de validación
     */
    public RowValidationException(String message) {
        super(message);
    }
}