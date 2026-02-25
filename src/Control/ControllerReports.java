package Control;

import Vista.Reports;
import Vista.Mensajes;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import javax.swing.Timer;

/**
 *
 * @author Jefferson Granados
 * En esta clase se controla la lógica del módulo de reportes.
 * Se encarga de:
 * - Cargar la tabla de pedidos desde el archivo "pedidos.txt".
 * - Cargar la tabla de pagos/facturas desde el archivo "pagos_factura.txt".
 * - Refrescar automáticamente las tablas cuando detecta cambios en los archivos.
 * - Permitir la búsqueda de un pedido por ID y mostrar su detalle en pantalla.
 *
 * Funciona como controlador de la vista Reports, manejando eventos y actualizando componentes.
 */
public class ControllerReports {

    private final Reports vista;
    private final Mensajes mensajes;

    /** Archivo base donde se almacenan los pedidos (formato CSV). */
    private static final String ARCHIVO_PEDIDOS = "pedidos.txt";

    /** Archivo donde se almacenan los pagos asociados a facturas (formato separado por '|'). */
    private static final String ARCHIVO_PAGOS_FACTURA = "pagos_factura.txt";

    /** Timer para refrescar automáticamente cuando los archivos cambian. */
    private Timer refreshTimer;

    /** Marca de última modificación del archivo de pedidos para detectar cambios. */
    private long lastPedidos = 0;

    /** Marca de última modificación del archivo de pagos para detectar cambios. */
    private long lastPagos = 0;

    /**
     * Constructor: recibe la vista Reports, carga las tablas iniciales,
     * inicia el refresco automático y registra los eventos de búsqueda.
     *
     * @param vista formulario Reports
     */
    public ControllerReports(Reports vista) {
        this.vista = vista;
        this.mensajes = new Mensajes();

        // Carga inicial de datos
        cargarTablaPedidos();
        cargarTablaPagosFactura();

        // Refresco automático si los archivos cambian
        iniciarAutoRefresh();

        // Evento: botón Ver Detalles (busca por ID)
        vista.getBtnVerDetallesPedidos().addActionListener(e -> mostrarDetallePedidoPorId());

        // Evento: Enter en el campo de texto (busca por ID)
        vista.getTxtIDPedidoBuscar().addActionListener(e -> mostrarDetallePedidoPorId());
    }

    /**
     * Inicia un Timer que revisa cada 1 segundo si los archivos fueron modificados.
     * Si detecta cambios en "pedidos.txt" o "pagos_factura.txt", recarga las tablas.
     *
     * Se apoya en lastModified() para comparar cambios.
     */
    private void iniciarAutoRefresh() {
        File fPedidos = new File(ARCHIVO_PEDIDOS);
        File fPagos = new File(ARCHIVO_PAGOS_FACTURA);

        lastPedidos = fPedidos.exists() ? fPedidos.lastModified() : 0;
        lastPagos = fPagos.exists() ? fPagos.lastModified() : 0;

        refreshTimer = new Timer(1000, e -> {
            long modPedidos = fPedidos.exists() ? fPedidos.lastModified() : 0;
            long modPagos = fPagos.exists() ? fPagos.lastModified() : 0;

            // Si cambió el archivo de pedidos, recargar tabla
            if (modPedidos != lastPedidos) {
                lastPedidos = modPedidos;
                cargarTablaPedidos();
            }

            // Si cambió el archivo de pagos, recargar tabla
            if (modPagos != lastPagos) {
                lastPagos = modPagos;
                cargarTablaPagosFactura();
            }
        });

        refreshTimer.start();
    }

    /**
     * Carga los pedidos desde el archivo "pedidos.txt" y los coloca en la tabla de pedidos.
     *
     * - Lee cada línea del archivo.
     * - Ignora líneas vacías.
     * - Separa por coma (CSV).
     * - Si faltan campos mínimos, ignora la línea.
     * - Arma un DefaultTableModel con columnas específicas.
     */
    private void cargarTablaPedidos() {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID Pedido", "Fecha", "Hora", "Mesa", "Cliente/Cedula", "Total", "Estado"}, 0
        );

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PEDIDOS))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                // Ignorar líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }

                // Formato esperado: CSV con al menos 9 columnas
                String[] p = linea.split(",", -1);

                // Validación mínima de estructura
                if (p.length < 9) {
                    continue;
                }

                // Campos visibles en tabla
                String idPedido = p[0].trim();
                String fecha = p[1].trim();
                String hora = p[2].trim();
                String mesa = p[3].trim();
                String cliente = p[4].trim();

                // Total se muestra con símbolo de colón y se toma desde la columna 8
                String total = "₡" + p[8].trim();

                // Estado: si viene en columna 9 lo usa; si no, muestra "Pendiente"
                String estado = (p.length > 9 && !p[9].trim().isEmpty()) ? p[9].trim() : "Pendiente";

                modelo.addRow(new Object[]{
                    idPedido, fecha, hora, mesa, cliente, total, estado
                });
            }

        } catch (IOException e) {

        }

        vista.getjTableReceptorPedidos().setModel(modelo);
    }

    /**
     * Carga los registros de pagos/facturas desde "pagos_factura.txt" y los coloca en la tabla.
     *
     * - Ignora líneas vacías.
     * - Ignora encabezado si inicia con "ID_FACTURA".
     * - Separa por '|'.
     * - Requiere al menos 5 campos: ID_Factura, Metodo, Monto, Referencia, Cedula.
     */
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

                // Omitir encabezado del archivo si existe
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

        }

        vista.getjTableReceptorReportesPagos().setModel(modelo);
    }

    /**
     * Busca un pedido por ID dentro de la tabla ya cargada y muestra el detalle en labels.
     *
     * Flujo:
     * - Toma el ID del JTextField.
     * - Si está vacío, muestra advertencia.
     * - Recorre el modelo de la tabla buscando coincidencia en la columna 0 (ID Pedido).
     * - Si lo encuentra, asigna valores a los labels y selecciona la fila.
     * - Si no lo encuentra, limpia el detalle y muestra mensaje.
     */
    private void mostrarDetallePedidoPorId() {

        String idBuscado = vista.getTxtIDPedidoBuscar().getText().trim();

        if (idBuscado.isEmpty()) {
            mensajes.message("Ingrese un ID de pedido.");
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
            mensajes.message("Pedido no encontrado en la lista.");
            return;
        }

        // Obtener datos desde la tabla (ya formateados)
        String fecha = val(m.getValueAt(fila, 1));
        String hora = val(m.getValueAt(fila, 2));
        String mesa = val(m.getValueAt(fila, 3));
        String cliente = val(m.getValueAt(fila, 4));
        String total = val(m.getValueAt(fila, 5));
        String estado = val(m.getValueAt(fila, 6));

        // Actualizar labels de detalle
        vista.getLblFecha().setText(fecha);
        vista.getLblHora().setText(hora);
        vista.getLblMesa().setText(mesa);
        vista.getLblClienteCedula().setText(cliente);
        vista.getLblTotal().setText(total);
        vista.getLblEstadoPedido().setText(estado);

        // Seleccionar fila encontrada en la tabla
        vista.getjTableReceptorPedidos().setRowSelectionInterval(fila, fila);
    }

    /**
     * Limpia los labels del panel de detalle del pedido y los deja en valores por defecto.
     */
    private void limpiarDetalle() {
        vista.getLblFecha().setText("-");
        vista.getLblHora().setText("-");
        vista.getLblMesa().setText("-");
        vista.getLblClienteCedula().setText("-");
        vista.getLblTotal().setText("-");
        vista.getLblEstadoPedido().setText("-");
    }

    /**
     * Método utilitario para evitar nulls al convertir valores a String.
     *
     * @param o objeto a convertir
     * @return cadena vacía si es null; en caso contrario, el valor como String
     */
    private String val(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }
}