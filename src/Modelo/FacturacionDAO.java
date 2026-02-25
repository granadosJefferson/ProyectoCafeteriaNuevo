package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jefferson Granados
 * En esta clase se maneja la persistencia de facturas mediante un archivo de texto (facturas.txt).
 * Se encarga de:
 * - Crear el archivo si no existe y escribir un encabezado.
 * - Generar el siguiente ID de factura de forma incremental.
 * - Guardar facturas (append) en el archivo.
 * - Buscar facturas por ID de factura, por ID de pedido o por cédula.
 * - Listar las últimas N facturas registradas.
 *
 * Formato de almacenamiento:
 * ID_FACTURA|FECHA|HORA|ID_PEDIDO|CEDULA_CLIENTE|NOMBRE_CLIENTE|MESA|SUBTOTAL|IVA|TOTAL|METODO_PAGO
 */
public class FacturacionDAO {

    private static final String FILE_NAME = "facturas.txt";
    private static final String HEADER =
            "ID_FACTURA|FECHA|HORA|ID_PEDIDO|CEDULA_CLIENTE|NOMBRE_CLIENTE|MESA|SUBTOTAL|IVA|TOTAL|METODO_PAGO";

    /**
     * Constructor: inicializa el DAO y asegura que el archivo exista.
     * Si el archivo no existe, lo crea e inserta el encabezado.
     */
    public FacturacionDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Crea el archivo de facturas si no existe.
     * En caso de crearlo, escribe el encabezado con el formato de columnas.
     */
    private void crearArchivoSiNoExiste() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(HEADER);
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtiene el siguiente ID de factura basado en el mayor ID almacenado en el archivo.
     * Lee el archivo completo, identifica el ID máximo y retorna max + 1.
     *
     * @return siguiente ID disponible para registrar una factura
     */
    public int siguienteIdFactura() {
        int maxId = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linea;
            br.readLine(); // header

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split("\\|", -1);
                if (partes.length > 0) {
                    try {
                        int id = Integer.parseInt(partes[0].trim());
                        if (id > maxId) maxId = id;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return maxId + 1;
    }

    /**
     * Guarda una factura en el archivo facturas.txt.
     * Los datos se guardan en una línea, separados por el carácter '|'.
     *
     * @param idFactura      ID de la factura
     * @param fecha          fecha de emisión
     * @param hora           hora de emisión
     * @param idPedido       ID del pedido asociado
     * @param cedulaCliente  cédula del cliente
     * @param nombreCliente  nombre del cliente
     * @param mesa           identificador de mesa
     * @param subtotal       subtotal calculado
     * @param iva            IVA calculado
     * @param total          total final
     * @param metodoPago     método de pago (ej: Efectivo, Tarjeta, Sinpe, etc.)
     */
    public void guardarFactura(int idFactura, String fecha, String hora, int idPedido,
                               String cedulaCliente, String nombreCliente, String mesa,
                               int subtotal, int iva, int total, String metodoPago) {

        String linea = idFactura + "|" + fecha + "|" + hora + "|" + idPedido + "|"
                + cedulaCliente + "|" + nombreCliente + "|" + mesa + "|"
                + subtotal + "|" + iva + "|" + total + "|" + metodoPago;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(linea);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca una factura por su ID de factura.
     * Recorre el archivo y retorna el arreglo de campos si encuentra coincidencia.
     *
     * @param idFacturaBuscada ID de la factura a buscar
     * @return arreglo con los campos de la factura si existe; null si no se encuentra
     */
    public String[] buscarFacturaPorId(int idFacturaBuscada) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linea;
            br.readLine(); // header

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split("\\|", -1);
                if (partes.length >= 11) {
                    try {
                        int id = Integer.parseInt(partes[0].trim());
                        if (id == idFacturaBuscada) return partes;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Busca una factura por el ID del pedido asociado.
     * Recorre el archivo y compara contra la columna ID_PEDIDO (posición 3).
     *
     * @param idPedidoBuscado ID del pedido a buscar
     * @return arreglo con los campos de la factura si existe; null si no se encuentra
     */
    public String[] buscarFacturaPorIdPedido(int idPedidoBuscado) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linea;
            br.readLine(); // header

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split("\\|", -1);
                if (partes.length >= 11) {
                    try {
                        int idPedido = Integer.parseInt(partes[3].trim());
                        if (idPedido == idPedidoBuscado) return partes;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lista las últimas N facturas almacenadas en el archivo.
     * Primero carga todas las facturas en memoria y luego retorna las últimas N.
     *
     * @param n cantidad de facturas finales a retornar
     * @return lista con arreglos de campos de las últimas facturas registradas
     */
    public List<String[]> listarUltimasFacturas(int n) {
        List<String[]> todas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linea;
            br.readLine(); // header

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split("\\|", -1);
                if (partes.length >= 11) todas.add(partes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String[]> ultimas = new ArrayList<>();
        for (int i = Math.max(0, todas.size() - n); i < todas.size(); i++) {
            ultimas.add(todas.get(i));
        }
        return ultimas;
    }

    /**
     * Busca facturas por cédula del cliente.
     * La comparación se realiza contra la columna CEDULA_CLIENTE (posición 4).
     *
     * @param cedula cédula a buscar
     * @return lista de facturas asociadas a esa cédula; lista vacía si no hay coincidencias
     */
    public List<String[]> buscarFacturasPorCedula(String cedula) {
        List<String[]> res = new ArrayList<>();
        if (cedula == null) return res;

        String target = cedula.trim();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linea;
            br.readLine(); // header

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split("\\|", -1);
                if (partes.length >= 11) {
                    if (partes[4].trim().equalsIgnoreCase(target)) {
                        res.add(partes);
                    }
                }
            }
        } catch (IOException e) {

            e.printStackTrace();
        }

        return res;
    }

}