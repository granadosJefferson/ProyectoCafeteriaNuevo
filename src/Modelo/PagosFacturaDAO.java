package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PagosFacturaDAO {

    private static final String ARCHIVO = "pagos_factura.txt";
    private static final String HEADER = "ID_FACTURA|METODO|MONTO|REFERENCIA|CEDULA_PAGADOR";

    public PagosFacturaDAO() {
        
        crearArchivoSiNoExiste();
    }

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

                // Formato esperado: 0 ID_FACTURA | 1 METODO | 2 MONTO | 3 REFERENCIA | 4 CEDULA_PAGADOR
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