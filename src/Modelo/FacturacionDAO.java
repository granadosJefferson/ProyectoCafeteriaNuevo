package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FacturacionDAO {

    private static final String FILE_NAME = "facturas.txt";
    private static final String HEADER =
            "ID_FACTURA|FECHA|HORA|ID_PEDIDO|CEDULA_CLIENTE|NOMBRE_CLIENTE|MESA|SUBTOTAL|IVA|TOTAL|METODO_PAGO";

    public FacturacionDAO() {
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

    // ✅ ÚNICO: buscar factura por ID_PEDIDO
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