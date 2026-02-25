package Modelo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Daniel Araya
 *
 * Clase que representa una Mesa dentro del sistema.
 *
 * Contiene información estructural y de estado:
 * - tableId: identificador interno (ej: "M1", "M2"...).
 * - tableNumber: número visible de la mesa (1..5).
 * - capacity: capacidad máxima de personas.
 * - estado: estado actual de la mesa (LIBRE, OCUPADA, LLENA).
 * - cedulas: lista de cédulas asociadas actualmente a la mesa.
 *
 * Incluye además métodos para:
 * - Obtener cantidad de personas actuales.
 * - Convertir la mesa a formato texto para persistencia.
 * - Reconstruir una mesa desde una línea de texto.
 *
 * Forma parte del Modelo dentro del patrón MVC.
 */
public class Tables {

    /**
     * Enumeración que define los posibles estados de una mesa.
     */
    public enum EstadoMesa {
        LIBRE, OCUPADA, LLENA
    }

    /**
     * Identificador interno de la mesa (ej: "M1").
     */
    private String tableId;

    /**
     * Número visible de la mesa (ej: 1, 2, 3...).
     */
    private int tableNumber;

    /**
     * Capacidad máxima de personas permitidas.
     */
    private int capacity;

    /**
     * Estado actual de la mesa.
     */
    private EstadoMesa estado;

    /**
     * Lista de cédulas asociadas actualmente a la mesa.
     */
    private List<String> cedulas;

    /**
     * Constructor principal.
     *
     * Inicializa:
     * - Estado por defecto: LIBRE.
     * - Lista de cédulas vacía.
     *
     * @param tableId identificador interno
     * @param tableNumber número visible de la mesa
     * @param capacity capacidad máxima
     */
    public Tables(String tableId, int tableNumber, int capacity) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.estado = EstadoMesa.LIBRE;
        this.cedulas = new ArrayList<>();
    }

    public String getTableId() { 
        return tableId; 
    }

    public int getTableNumber() { 
        return tableNumber; 
    }

    public int getCapacity() { 
        return capacity; 
    }

    public EstadoMesa getEstado() { 
        return estado; 
    }

    public void setEstado(EstadoMesa estado) { 
        this.estado = estado; 
    }

    public List<String> getCedulas() { 
        return cedulas; 
    }

    /**
     * Retorna la cantidad actual de personas en la mesa
     * según el tamaño de la lista de cédulas.
     *
     * @return número de personas actuales
     */
    public int personasActuales() {
        return cedulas.size();
    }

    // ====== Persistencia en texto ======

    /**
     * Convierte el objeto Tables en una línea de texto
     * para ser almacenada en un archivo.
     *
     * Formato:
     * tableId,tableNumber,capacity,estado,cedula1;cedula2;...
     *
     * @return línea lista para persistencia
     */
    public String toDataString() {
        String personas = String.join(";", cedulas);
        return tableId + "," + tableNumber + "," + capacity + "," + estado + "," + personas;
    }

    /**
     * Reconstruye un objeto Tables desde una línea de texto.
     *
     * Flujo:
     * - Divide por comas.
     * - Crea la mesa con ID, número y capacidad.
     * - Asigna estado.
     * - Si existen cédulas, las separa por ";" y las agrega.
     *
     * @param linea línea leída del archivo
     * @return objeto Tables reconstruido
     */
    public static Tables fromDataString(String linea) {
        String[] p = linea.split(",", -1);

        Tables t = new Tables(
                p[0],
                Integer.parseInt(p[1]),
                Integer.parseInt(p[2])
        );

        t.estado = EstadoMesa.valueOf(p[3]);

        if (p.length > 4 && !p[4].isEmpty()) {
            String[] personas = p[4].split(";");
            for (String c : personas) {
                t.cedulas.add(c);
            }
        }

        return t;
    }
}