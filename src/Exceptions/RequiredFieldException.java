/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Exceptions;

/**
 *
 * @author Personal
 */
public class RequiredFieldException extends RowValidationException {
    public RequiredFieldException(String fieldName) {
        super("El campo '" + fieldName + "' es obligatorio.");
    }
}
