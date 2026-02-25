/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
/**
 *
 * @author dh057
 */
public class SoloNumerosDecimalesFilter extends DocumentFilter {

    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string == null) return;

        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = actual.substring(0, offset) + string + actual.substring(offset);

        if (esDecimalValido(nuevo)) {
            fb.insertString(offset, string, attr);
        }
    }

    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text == null) return;

        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = actual.substring(0, offset) + text + actual.substring(offset + length);

        if (esDecimalValido(nuevo)) {
            fb.replace(offset, length, text, attrs);
        }
    }

    private boolean esDecimalValido(String s) {
        if (s.equals("")) return true; // permitir borrar
        int puntos = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                puntos++;
                if (puntos > 1) return false;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
