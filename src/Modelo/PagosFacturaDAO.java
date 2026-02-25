package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * DAO para guardar y consultar pagos asociados a una factura en un archivo de texto (pagos_factura.txt).
 * Cada pago se almacena en una línea con separador '|', y el archivo incluye un encabezado.
 *
 * @author Jefferson
 */
public class PagosFacturaDAO {

    private static final String ARCHIVO = "pagos_factura.txt";
    private static final String HEADER = "ID_FACTURA|METODO|MONTO|REFERENCIA|CEDULA_PAGADOR";

    /**
     * Constructor: asegura que el archivo exista (y crea el header si es nuevo).
     */
    public PagosFacturaDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Crea el archivo si no existe y escribe el encabezado.
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
     * Guarda un pago en el archivo (append).
     * Normaliza Strings null a "" para mantener el formato del registro.
     *
     * @return true si se guardó correctamente, false si falló la escritura.
     */
    public boolean guardarPago(int idFactura, String metodo, int monto, String referencia, String cedulaPagador) {
        crearArchivoSiNoExiste();

        if (metodo == null) metodo = "";
        if (referencia == null) referencia = "";
        if (cedulaPagador == null) cedulaPagador = "";

        String linea = idFactura + "|"
                + metodo + "|"
                + monto + "|"
                + referencia + "|"
                + cedulaPagador;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(linea);
            bw.newLine();
            return true;
        } catch (IOException e) {

            return false;
        }
    }

    /**
     * Lee el archivo y retorna todos los pagos que pertenezcan al ID de factura indicado.
     * Omite el encabezado y agrega cada línea encontrada como String[] (campos separados).
     */
    public List<String[]> listarPagosPorFactura(int idFacturaBuscada) {
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

                if (partes.length >= 5) {
                    try {
                        int id = Integer.parseInt(partes[0].trim());
                        if (id == idFacturaBuscada) {
                            res.add(partes);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {

        }

        return res;
    }
}