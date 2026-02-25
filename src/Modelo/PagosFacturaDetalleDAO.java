package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * DAO que guarda y consulta el detalle de pagos por factura (por producto)
 * en un archivo de texto plano: pagos_factura_detalle.txt.
 *
 * Formato:
 * ID_FACTURA|ID_PEDIDO|METODO|REFERENCIA|CEDULA_PAGADOR|PRODUCTO|CANTIDAD|PRECIO|TOTAL_LINEA
 *
 * @author Jefferson
 */
public class PagosFacturaDetalleDAO {

    private static final String ARCHIVO = "pagos_factura_detalle.txt";
    private static final String HEADER =
            "ID_FACTURA|ID_PEDIDO|METODO|REFERENCIA|CEDULA_PAGADOR|PRODUCTO|CANTIDAD|PRECIO|TOTAL_LINEA";

    /**
     * Constructor: asegura que el archivo exista con su encabezado.
     */
    public PagosFacturaDetalleDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Crea el archivo si no existe y escribe el encabezado en la primera línea.
     */
    private void crearArchivoSiNoExiste() {
        File file = new File(ARCHIVO);
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(HEADER);
                bw.newLine();
            } catch (IOException e) {

            }
        }
    }

    /**
     * Guarda un registro de detalle (por producto) al final del archivo.
     * Normaliza Strings null a "" y escribe en modo append.
     *
     * @return true si se guardó, false si ocurrió un error de escritura.
     */
    public boolean guardarDetalle(int idFactura, int idPedido, String metodo, String referencia,
                                  String cedulaPagador, String producto, int cantidad, int precio, int totalLinea) {
        crearArchivoSiNoExiste();

        if (metodo == null) metodo = "";
        if (referencia == null) referencia = "";
        if (cedulaPagador == null) cedulaPagador = "";
        if (producto == null) producto = "";

        String linea = idFactura + "|" + idPedido + "|"
                + metodo + "|" + referencia + "|"
                + cedulaPagador + "|"
                + producto + "|"
                + cantidad + "|"
                + precio + "|"
                + totalLinea;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(linea);
            bw.newLine();
            return true;
        } catch (IOException e) {

            return false;
        }
    }

    /**
     * Lee el archivo y devuelve todas las filas cuyo ID_FACTURA coincida.
     * Omite el encabezado y retorna cada línea separada por campos (String[]).
     */
    public List<String[]> listarDetallePorFactura(int idFacturaBuscada) {
        crearArchivoSiNoExiste();

        List<String[]> res = new ArrayList<>();
        File f = new File(ARCHIVO);
        if (!f.exists()) return res;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            br.readLine();

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] partes = linea.split("\\|", -1);
                if (partes.length >= 9) {
                    try {
                        int idFactura = Integer.parseInt(partes[0].trim());
                        if (idFactura == idFacturaBuscada) res.add(partes);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {

        }

        return res;
    }
}