/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Exceptions;

/**
 *
 * @author dh057
 */
public class InvalidPhoneException extends RowValidationException {
    public InvalidPhoneException() {
        super("El teléfono debe contener solo números y ser válido.");
    }
}