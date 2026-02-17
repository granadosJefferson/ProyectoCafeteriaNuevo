/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 *
 * @author Jefferson granados
 */
public class Mensajes {
    
     /**
     * Muestra un mensaje simple (solo el texto, sin título)
     * @param mensaje Texto a mostrar
     */
    public static void mostrarMensaje(JComponent parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje);
    }
    
    
}
