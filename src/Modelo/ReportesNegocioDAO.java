package Modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author Daniel Araya
 *
 * Clase DAO (Data Access Object) encargada de obtener la información
 * necesaria para el módulo de Reportes del Negocio.
 *
 * Se encarga de:
 * - Verificar la existencia del archivo "pedidos.txt".
 * - Leer los registros de pedidos almacenados en formato CSV.
 * - Transformar cada línea en un arreglo de datos listo para ser
 *   mostrado en la tabla del módulo ReportesNegocio.
 *
 * El formato esperado del archivo pedidos.txt es:
 *
 * idPedido,fecha,hora,numeroMesa,idCliente,items,totalProductos,iva,totalConIva
 *
 * Esta clase forma parte del Modelo dentro del patrón MVC.
 */
public class ReportesNegocioDAO {

    /**
     * Archivo base donde se almacenan los pedidos.
     */
    private static final String ARCHIVO_PEDIDOS = "pedidos.txt";

    /**
     * Constructor:
     * - Asegura que el archivo pedidos.txt exista antes de intentar leerlo.
     */
    public ReportesNegocioDAO() {
        asegurarArchivo();
    }

    /**
     * Verifica si el archivo pedidos.txt existe.
     * Si no existe, lo crea automáticamente.
     */
    private void asegurarArchivo() {
        try {
            File f = new File(ARCHIVO_PEDIDOS);
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (Exception e) {
            System.out.println("No se pudo crear pedidos.txt: " + e.getMessage());
        }
    }

    /**
     * Retorna una lista de filas listas para ser cargadas en la tabla
     * del módulo ReportesNegocio.
     *
     * Cada fila contiene:
     * [0] = idPedido
     * [1] = fechaHora (fecha + hora)
     * [2] = idMesa
     * [3] = cedulaCliente
     * [4] = totalConIva
     *
     * Flujo:
     * - Lee cada línea del archivo pedidos.txt.
     * - Ignora líneas vacías.
     * - Valida que la línea tenga al menos 9 columnas.
     * - Extrae los campos necesarios.
     * - Agrega un arreglo String[] a la lista final.
     *
     * @return lista de filas listas para mostrarse en la tabla
     */
    public ArrayList<String[]> obtenerReportesDesdePedidos() {

        ArrayList<String[]> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PEDIDOS))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) continue;

                // idPedido,fecha,hora,numeroMesa,idCliente,items,totalProductos,iva,totalConIva
                String[] p = linea.split(",", -1);
                if (p.length < 9) continue;

                String idPedido = p[0].trim();
                String fechaHora = (p[1].trim() + " " + p[2].trim()).trim();
                String idMesa = p[3].trim();         // "1..5" o "LLEVAR"
                String cedula = p[4].trim();
                String totalConIva = p[8].trim();

                lista.add(new String[]{idPedido, fechaHora, idMesa, cedula, totalConIva});
            }

        } catch (Exception e) {
            System.out.println("Error leyendo pedidos.txt: " + e.getMessage());
        }

        return lista;
    }
}