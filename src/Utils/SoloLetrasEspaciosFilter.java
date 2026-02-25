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
public class SoloLetrasEspaciosFilter extends DocumentFilter {

    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string == null) return;

        if (soloLetrasEspacios(string)) {
            fb.insertString(offset, string, attr);
        }
    }

    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text == null) return;

        if (text.equals("") || soloLetrasEspacios(text)) {
            fb.replace(offset, length, text, attrs);
        }
    }

    private boolean soloLetrasEspacios(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == ' ') continue;

            // Letras normales o con tildes (Unicode)
            if (!Character.isLetter(c)) return false;
        }
        return true;
    }
}
