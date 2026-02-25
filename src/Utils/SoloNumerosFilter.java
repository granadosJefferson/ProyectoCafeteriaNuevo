package Utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author Daniel Araya
 *
 * Filtro de documento que permite únicamente números enteros.
 *
 * Reglas:
 * - Solo permite caracteres numéricos (0-9).
 * - Permite borrar contenido.
 * - Bloquea cualquier letra, símbolo o carácter especial.
 *
 * Se utiliza en campos como:
 * - ID (cédula).
 * - Cantidad de visitas.
 *
 * Forma parte del paquete Utils y se aplica a JTextField
 * mediante DocumentFilter para validar entrada desde la Vista.
 */
public class SoloNumerosFilter extends DocumentFilter {

    /**
     * Se ejecuta cuando se intenta insertar texto.
     *
     * - Si el texto es null, no hace nada.
     * - Si contiene únicamente números, permite la inserción.
     */
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {

        if (string == null) {
            return;
        }

        if (esSoloNumeros(string)) {
            fb.insertString(offset, string, attr);
        }
    }

    /**
     * Se ejecuta cuando se reemplaza texto.
     *
     * - Permite borrar (text vacío).
     * - Permite reemplazar solo si el nuevo texto contiene únicamente números.
     */
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) {
            return;
        }

        if (text.equals("") || esSoloNumeros(text)) {
            fb.replace(offset, length, text, attrs);
        }
    }

    /**
     * Verifica que una cadena contenga únicamente dígitos.
     *
     * @param s texto a validar
     * @return true si todos los caracteres son números; false en caso contrario
     */
    private boolean esSoloNumeros(String s) {

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}