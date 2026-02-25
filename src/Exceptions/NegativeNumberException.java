/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Exceptions;

/**
 *
 * @author dh057
 */
public class NegativeNumberException extends RowValidationException {
    public NegativeNumberException(String fieldName) {
        super("El campo '" + fieldName + "' no puede ser negativo.");
    }
}
