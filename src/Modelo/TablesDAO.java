package Modelo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 *
 * @author Daniel Araya
 *
 * Clase DAO (Data Access Object) encargada de la persistencia y administración
 * de las mesas del sistema.
 *
 * Funciones principales:
 * - Crear e inicializar el archivo "Tables.txt" si no existe.
 * - Cargar las mesas desde el archivo hacia memoria.
 * - Guardar los cambios de la lista de mesas nuevamente en el archivo.
 * - Proveer métodos de consulta y actualización sobre las mesas.
 *
 * Implementa el patrón Singleton para garantizar una única instancia de acceso
 * a las mesas durante la ejecución del programa.
 */
public class TablesDAO {

    /**
     * Nombre del archivo donde se almacenan las mesas.
     */
    private static final String ARCHIVO = "Tables.txt";

    /**
     * Instancia única del DAO (Singleton).
     */
    private static TablesDAO instancia;

    /**
     * Lista en memoria con todas las mesas cargadas desde el archivo.
     */
    private List<Tables> mesas;

    /**
     * Constructor privado para patrón Singleton.
     *
     * Inicializa la lista, crea el archivo si no existe, y carga los datos
     * iniciales en memoria.
     */
    private TablesDAO() {
        mesas = new ArrayList<>();
        inicializarArchivo();
        cargar();
    }

    /**
     * Retorna la instancia única del DAO.
     * Si no existe, la crea y carga su información.
     *
     * @return instancia única de TablesDAO
     */
    public static synchronized TablesDAO getInstancia() {
        if (instancia == null) {
            instancia = new TablesDAO();
        }
        return instancia;
    }

    /**
     * Crea el archivo "Tables.txt" si no existe.
     *
     * Si el archivo se crea por primera vez, también se generan 5 mesas por
     * defecto con:
     * - tableId = "M1".."M5"
     * - tableNumber = 1..5
     * - capacity = 4
     *
     * El contenido se guarda usando el formato retornado por Tables.toDataString().
     */
    private void inicializarArchivo() {
        try {
            Path path = Paths.get(ARCHIVO);

            if (!Files.exists(path)) {
                Files.createFile(path);

                // Crear 5 mesas por defecto
                List<String> lineas = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    Tables t = new Tables("M" + i, i, 4);
                    lineas.add(t.toDataString());
                }

                Files.write(path, lineas);
            }

        } catch (IOException e) {
            System.out.println("Error creando archivo Tables.txt: " + e.getMessage());
        }
    }

    /**
     * Carga todas las mesas desde "Tables.txt" a la lista en memoria.
     *
     * - Limpia primero la lista actual.
     * - Lee línea por línea.
     * - Ignora líneas vacías.
     * - Convierte cada línea a un objeto Tables usando Tables.fromDataString().
     */
    private void cargar() {
        mesas.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    mesas.add(Tables.fromDataString(linea));
                }
            }

        } catch (IOException e) {
            System.out.println("Error cargando mesas: " + e.getMessage());
        }
    }

    /**
     * Guarda el estado actual de la lista de mesas en el archivo "Tables.txt".
     *
     * - Sobrescribe completamente el archivo.
     * - Escribe cada mesa usando Tables.toDataString().
     */
    private void guardar() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Tables t : mesas) {
                bw.write(t.toDataString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error guardando mesas: " + e.getMessage());
        }
    }

    /**
     * Retorna la lista de mesas actualmente en memoria.
     *
     * @return lista de mesas
     */
    public List<Tables> listar() {
        return mesas;
    }

    /**
     * Busca una mesa por su identificador interno (tableId).
     *
     * @param id identificador interno (ej: "M1")
     * @return objeto Tables si existe; si no, null
     */
    public Tables buscarPorTableId(String id) {
        for (Tables t : mesas) {
            if (t.getTableId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Actualiza una mesa.
     *
     * Nota: Actualmente este método solo persiste el estado en el archivo,
     * asumiendo que la mesa ya fue modificada en memoria.
     *
     * @param mesa mesa que se desea actualizar
     */
    public void actualizarMesa(Tables mesa) {
        guardar();
    }

    /**
     * Libera una mesa específica:
     * - Limpia la lista de cédulas asociadas.
     * - Cambia el estado a LIBRE.
     * - Guarda cambios en archivo.
     *
     * @param tableId identificador interno de la mesa
     */
    public void liberarMesa(String tableId) {
        Tables mesa = buscarPorTableId(tableId);
        if (mesa != null) {
            mesa.getCedulas().clear();
            mesa.setEstado(Tables.EstadoMesa.LIBRE);
            guardar();
        }
    }
}