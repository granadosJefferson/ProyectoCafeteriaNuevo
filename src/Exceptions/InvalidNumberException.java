/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Exceptions;

/**
 *
 * @author dh057
 */
public class InvalidNumberException extends RowValidationException {
    public InvalidNumberException(String fieldName) {
        super("El campo '" + fieldName + "' debe ser un número válido.");
    }
}
