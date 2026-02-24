package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PagosFacturaDAO {

    private static final String FILE_NAME = "pagos_factura.txt";
    private static final String HEADER = "ID_FACTURA|METODO|MONTO|REFERENCIA";

    public PagosFacturaDAO() {
        crearArchivoSiNoExiste();
    }

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

    public void guardarPago(int idFactura, String metodo, int monto, String referencia) {
        if (referencia == null) referencia = "";
        String linea = idFactura + "|" + metodo + "|" + monto + "|" + referencia;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(linea);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> listarPagosPorFactura(int idFacturaBuscada) {
        List<String[]> res = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linea;
            br.readLine(); // header
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split("\\|", -1);
                if (partes.length >= 4) {
                    try {
                        int id = Integer.parseInt(partes[0].trim());
                        if (id == idFacturaBuscada) res.add(partes);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }
}