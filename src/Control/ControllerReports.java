package Control;

import Vista.Reports;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import javax.swing.Timer;

public class ControllerReports {

    private final Reports vista;

    private static final String ARCHIVO_PEDIDOS = "pedidos.txt";
    private static final String ARCHIVO_PAGOS_FACTURA = "pagos_factura.txt";
    private Timer refreshTimer;
    private long lastPedidos = 0;
    private long lastPagos = 0;

    public ControllerReports(Reports vista) {
        this.vista = vista;

        cargarTablaPedidos();
        cargarTablaPagosFactura();
        
        iniciarAutoRefresh();

        vista.getBtnVerDetallesPedidos().addActionListener(e -> mostrarDetallePedidoPorId());

        vista.getTxtIDPedidoBuscar().addActionListener(e -> mostrarDetallePedidoPorId());
    }
    
    
    private void iniciarAutoRefresh() {
    File fPedidos = new File(ARCHIVO_PEDIDOS);
    File fPagos = new File(ARCHIVO_PAGOS_FACTURA);

    lastPedidos = fPedidos.exists() ? fPedidos.lastModified() : 0;
    lastPagos = fPagos.exists() ? fPagos.lastModified() : 0;

    refreshTimer = new Timer(1000, e -> { 
        long modPedidos = fPedidos.exists() ? fPedidos.lastModified() : 0;
        long modPagos = fPagos.exists() ? fPagos.lastModified() : 0;

        if (modPedidos != lastPedidos) {
            lastPedidos = modPedidos;
            cargarTablaPedidos();
        }

        if (modPagos != lastPagos) {
            lastPagos = modPagos;
            cargarTablaPagosFactura();
        }
    });

    refreshTimer.start();
}

    private void cargarTablaPedidos() {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID Pedido", "Fecha", "Hora", "Mesa", "Cliente/Cedula", "Total", "Estado"}, 0
        );

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PEDIDOS))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] p = linea.split(",", -1);

                if (p.length < 9) {
                    continue;
                }

                String idPedido = p[0].trim();
                String fecha = p[1].trim();
                String hora = p[2].trim();
                String mesa = p[3].trim();
                String cliente = p[4].trim();
                String total = "₡" + p[8].trim();

                String estado = (p.length > 9 && !p[9].trim().isEmpty()) ? p[9].trim() : "Pendiente";

                modelo.addRow(new Object[]{
                    idPedido, fecha, hora, mesa, cliente, total, estado
                });
            }

        } catch (IOException e) {
            System.out.println("Error leyendo " + ARCHIVO_PEDIDOS + ": " + e.getMessage());
        }

        vista.getjTableReceptorPedidos().setModel(modelo);
    }

    private void cargarTablaPagosFactura() {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID_Factura", "Metodo", "Monto", "Referencia", "Cedula"}, 0
        );

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PAGOS_FACTURA))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }

                if (linea.toUpperCase().startsWith("ID_FACTURA")) {
                    continue;
                }

                String[] p = linea.split("\\|", -1);
                if (p.length < 5) {
                    continue;
                }

                String idFactura = p[0].trim();
                String metodo = p[1].trim();
                String monto = "₡" + p[2].trim();
                String referencia = p[3].trim();
                String cedula = p[4].trim();

                modelo.addRow(new Object[]{
                    idFactura, metodo, monto, referencia, cedula
                });
            }

        } catch (IOException e) {
            System.out.println("Error leyendo " + ARCHIVO_PAGOS_FACTURA + ": " + e.getMessage());
        }

        vista.getjTableReceptorReportesPagos().setModel(modelo);
    }

    private void mostrarDetallePedidoPorId() {

        String idBuscado = vista.getTxtIDPedidoBuscar().getText().trim();

        if (idBuscado.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingrese un ID de pedido.");
            return;
        }

        DefaultTableModel m = (DefaultTableModel) vista.getjTableReceptorPedidos().getModel();

        int fila = -1;
        for (int i = 0; i < m.getRowCount(); i++) {
            String id = String.valueOf(m.getValueAt(i, 0)).trim();
            if (id.equals(idBuscado)) {
                fila = i;
                break;
            }
        }

        if (fila == -1) {
            limpiarDetalle();
            JOptionPane.showMessageDialog(vista, "Pedido no encontrado en la lista.");
            return;
        }

        String fecha = val(m.getValueAt(fila, 1));
        String hora = val(m.getValueAt(fila, 2));
        String mesa = val(m.getValueAt(fila, 3));
        String cliente = val(m.getValueAt(fila, 4));
        String total = val(m.getValueAt(fila, 5));
        String estado = val(m.getValueAt(fila, 6));

        vista.getLblFecha().setText(fecha);
        vista.getLblHora().setText(hora);
        vista.getLblMesa().setText(mesa);
        vista.getLblClienteCedula().setText(cliente);
        vista.getLblTotal().setText(total);
        vista.getLblEstadoPedido().setText(estado);

        vista.getjTableReceptorPedidos().setRowSelectionInterval(fila, fila);
    }

    private void limpiarDetalle() {
        vista.getLblFecha().setText("-");
        vista.getLblHora().setText("-");
        vista.getLblMesa().setText("-");
        vista.getLblClienteCedula().setText("-");
        vista.getLblTotal().setText("-");
        vista.getLblEstadoPedido().setText("-");
    }

    private String val(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }
}
