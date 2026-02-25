package Utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author Daniel Araya
 *
 * Filtro de documento para campos de texto (JTextField).
 *
 * Permite únicamente:
 * - Letras (incluye letras con tilde gracias a Character.isLetter).
 * - Espacios en blanco.
 *
 * Se utiliza principalmente en campos como:
 * - Nombre del cliente.
 *
 * Forma parte de la capa Utils y apoya la validación en la Vista,
 * respetando el patrón MVC.
 */
public class SoloLetrasEspaciosFilter extends DocumentFilter {

    /**
     * Método que se ejecuta cuando se intenta insertar texto.
     *
     * - Si el texto es null, no hace nada.
     * - Si el texto contiene solo letras y espacios, lo permite.
     * - Si contiene cualquier otro carácter, lo bloquea.
     */
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {

        if (string == null) return;

        if (soloLetrasEspacios(string)) {
            fb.insertString(offset, string, attr);
        }
    }

    /**
     * Método que se ejecuta cuando se reemplaza texto.
     *
     * - Permite borrar contenido (text.equals("")).
     * - Permite reemplazar solo si el nuevo texto contiene
     *   únicamente letras y espacios.
     */
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) return;

        if (text.equals("") || soloLetrasEspacios(text)) {
            fb.replace(offset, length, text, attrs);
        }
    }

    /**
     * Verifica que una cadena contenga únicamente:
     * - Letras (incluye letras con acentos).
     * - Espacios.
     *
     * @param s texto a validar
     * @return true si cumple la regla; false si contiene otro carácter
     */
    private boolean soloLetrasEspacios(String s) {

        for (int i = 0; i < s.length(); i++) {

            char c = s.charAt(i);

            if (c == ' ') continue;

            // Permite letras normales y con tildes (Unicode)
            if (!Character.isLetter(c)) return false;
        }

        return true;
    }
}