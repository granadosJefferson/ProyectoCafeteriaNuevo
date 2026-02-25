package Utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author Daniel Araya
 *
 * Filtro de documento para permitir únicamente números decimales.
 *
 * Reglas:
 * - Solo permite dígitos (0-9).
 * - Permite un único punto decimal ('.').
 * - Permite borrar completamente el contenido.
 *
 * Se utiliza principalmente en campos como:
 * - Total gastado.
 *
 * Forma parte del paquete Utils y se aplica sobre JTextField
 * mediante DocumentFilter, apoyando la validación desde la Vista.
 */
public class SoloNumerosDecimalesFilter extends DocumentFilter {

    /**
     * Se ejecuta cuando se intenta insertar texto en el campo.
     *
     * - Construye el texto resultante antes de permitir la inserción.
     * - Valida si el nuevo contenido cumple con el formato decimal.
     */
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {

        if (string == null) return;

        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = actual.substring(0, offset) + string + actual.substring(offset);

        if (esDecimalValido(nuevo)) {
            fb.insertString(offset, string, attr);
        }
    }

    /**
     * Se ejecuta cuando se reemplaza texto en el campo.
     *
     * - Reconstruye el texto final después del reemplazo.
     * - Solo permite la operación si el resultado es un decimal válido.
     */
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) return;

        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = actual.substring(0, offset) + text + actual.substring(offset + length);

        if (esDecimalValido(nuevo)) {
            fb.replace(offset, length, text, attrs);
        }
    }

    /**
     * Valida si una cadena representa un número decimal válido.
     *
     * Reglas:
     * - Puede estar vacía (para permitir borrar).
     * - Puede contener solo un punto decimal.
     * - El resto deben ser únicamente dígitos.
     *
     * @param s texto a validar
     * @return true si cumple las reglas; false en caso contrario
     */
    private boolean esDecimalValido(String s) {

        if (s.equals("")) return true; // permitir borrar

        int puntos = 0;

        for (int i = 0; i < s.length(); i++) {

            char c = s.charAt(i);

            if (c == '.') {
                puntos++;
                if (puntos > 1) return false;
            } 
            else if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }
}