/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Exceptions;

/**
 *
 * @author Personal
 */
public class InvalidEmailException extends RowValidationException {
    public InvalidEmailException() {
        super("El correo debe terminar en '@gmail.com'.");
    }
}