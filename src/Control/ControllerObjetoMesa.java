package Control;

import Modelo.Tables;
import Modelo.TablesDAO;
import Vista.ObjetoMesa;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ControllerObjetoMesa {

    private final ObjetoMesa vista;
    private final String tableId;
    private final JFrame ventana;
    private final ControllerGestionMesas controllerMesas;
    private final TablesDAO tablesDAO;

    private static final String ARCHIVO_PEDIDOS = "pedidos.txt";

    public ControllerObjetoMesa(ObjetoMesa vista, String tableId, JFrame ventana, ControllerGestionMesas controllerMesas) {
        this.vista = vista;
        this.tableId = tableId;
        this.ventana = ventana;
        this.controllerMesas = controllerMesas;
        this.tablesDAO = TablesDAO.getInstancia();

        // ✅ Eventos botones
        this.vista.getjBtnCerrarMesa().addActionListener(e -> cerrarMesa());
        this.vista.getjBtnSacarMesa().addActionListener(e -> sacarPersona());
    }

    public void cargar() {
        cargarTablaInformacionMesa();
    }

    /**
     * Carga SOLO lo importante en la tabla TableInformacionMesa:
     * ID Pedido, Fecha, Cliente (cédula), TotalConIva
     */
    private void cargarTablaInformacionMesa() {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID Pedido", "Fecha", "Cliente (Cédula)", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        String filtroMesa = obtenerFiltroMesa();

        asegurarArchivoPedidos();

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PEDIDOS))) {
            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) continue;

                String numeroMesaTxt = partes[3].trim();
                if (!numeroMesaTxt.equalsIgnoreCase(filtroMesa)) continue;

                String idPedido = partes[0].trim();
                String fecha = (partes[1].trim() + " " + partes[2].trim()).trim();
                String cedula = partes[4].trim();
                String totalConIva = partes[8].trim();

                modelo.addRow(new Object[]{idPedido, fecha, cedula, "₡" + totalConIva});
            }

        } catch (IOException e) {
            System.out.println("Error leyendo pedidos.txt: " + e.getMessage());
        }

        vista.getTableInformacionMesa().setModel(modelo);
    }

    /**
     * jBtnCerrarMesa:
     * Borra del TXT TODOS los registros (líneas) de esa mesa.
     * Luego recarga la tabla (quedará vacía por lógica).
     */
    private void cerrarMesa() {

        String filtroMesa = obtenerFiltroMesa();

        int op = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea cerrar la mesa y borrar los pedidos asociados?",
                "Cerrar mesa",
                JOptionPane.YES_NO_OPTION
        );
        if (op != JOptionPane.YES_OPTION) return;

        asegurarArchivoPedidos();

        boolean seBorroAlgo = reescribirPedidosExcluyendoMesa(filtroMesa);

        // recargar tabla (vacía si borró)
        cargarTablaInformacionMesa();

        // opcional: actualizar colores/estados en vista de mesas (si lo manejas por pedidos)
        if (controllerMesas != null) {
            controllerMesas.actualizarMesas();
        }

        JOptionPane.showMessageDialog(
                vista,
                seBorroAlgo ? "Mesa cerrada. Pedidos eliminados." : "No había pedidos para esa mesa."
        );
    }

    /**
     * jBtnSacarMesa:
     * El usuario selecciona una fila (persona/cliente) en la tabla.
     * Se elimina del TXT la línea que coincida con esa mesa y esa cédula.
     * Luego se recarga la tabla.
     */
    private void sacarPersona() {

        int row = vista.getTableInformacionMesa().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione una persona (fila) en la tabla.");
            return;
        }

        String filtroMesa = obtenerFiltroMesa();

        // columna 2 = "Cliente (Cédula)" según el modelo que cargamos
        String cedula = String.valueOf(vista.getTableInformacionMesa().getValueAt(row, 2)).trim();

        if (cedula.isEmpty() || cedula.equals("-")) {
            JOptionPane.showMessageDialog(vista, "Cédula inválida en la fila seleccionada.");
            return;
        }

        int op = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea eliminar a esta persona de la mesa?\nCédula: " + cedula,
                "Sacar persona",
                JOptionPane.YES_NO_OPTION
        );
        if (op != JOptionPane.YES_OPTION) return;

        asegurarArchivoPedidos();

        boolean seBorro = reescribirPedidosExcluyendoMesaYCedula(filtroMesa, cedula);

        cargarTablaInformacionMesa();

        if (controllerMesas != null) {
            controllerMesas.actualizarMesas();
        }

        JOptionPane.showMessageDialog(
                vista,
                seBorro ? "Persona eliminada de la mesa." : "No se encontró esa persona en esta mesa."
        );
    }

    // ==========================
    // Helpers de persistencia
    // ==========================

    private boolean reescribirPedidosExcluyendoMesa(String filtroMesa) {

        File original = new File(ARCHIVO_PEDIDOS);
        File temp = new File("pedidos_tmp.txt");

        boolean borro = false;

        try (BufferedReader br = new BufferedReader(new FileReader(original));
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) continue;

                String mesa = partes[3].trim();
                if (mesa.equalsIgnoreCase(filtroMesa)) {
                    borro = true;
                    continue; // ❌ no escribir
                }

                bw.write(linea);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error reescribiendo pedidos: " + e.getMessage());
        }

        reemplazarArchivo(original, temp);
        return borro;
    }

    private boolean reescribirPedidosExcluyendoMesaYCedula(String filtroMesa, String cedula) {

        File original = new File(ARCHIVO_PEDIDOS);
        File temp = new File("pedidos_tmp.txt");

        boolean borro = false;

        try (BufferedReader br = new BufferedReader(new FileReader(original));
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) continue;

                String mesa = partes[3].trim();
                String cliente = partes[4].trim();

                // ❌ borrar solo si coincide mesa + cédula
                if (mesa.equalsIgnoreCase(filtroMesa) && cliente.equalsIgnoreCase(cedula)) {
                    borro = true;
                    continue;
                }

                bw.write(linea);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error reescribiendo pedidos: " + e.getMessage());
        }

        reemplazarArchivo(original, temp);
        return borro;
    }

    private void reemplazarArchivo(File original, File temp) {
        // Windows a veces falla si intentas overwrite directo: borramos y renombramos
        if (original.exists()) original.delete();
        temp.renameTo(original);
    }

    private void asegurarArchivoPedidos() {
        try {
            File f = new File(ARCHIVO_PEDIDOS);
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("No se pudo crear pedidos.txt: " + e.getMessage());
        }
    }

    /**
     * Devuelve lo que viene en el campo "numeroMesa" del TXT:
     * - Si es llevar: "LLEVAR"
     * - Si es mesa: "1".."5"
     */
    private String obtenerFiltroMesa() {

        if (tableId.equalsIgnoreCase("LLEVAR")) {
            return "LLEVAR";
        }

        Tables mesa = tablesDAO.buscarPorTableId(tableId);

        // fallback si por alguna razón no está en DAO
        if (mesa == null) return tableId;

        return String.valueOf(mesa.getTableNumber());
    }
}