package Control;

import Modelo.FacturacionDAO;
import Modelo.PagosFacturaDAO;
import Modelo.Product;
import Modelo.pedidosDAO;
import Modelo.productosDAO;
import Vista.GestionFacturacion;
import Modelo.PagosFacturaDetalleDAO;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import Modelo.ClientsDAO;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * Clase controladora del módulo de facturación. Se encarga de: - Cargar pedidos
 * por ID o por mesa (uniendo varios pedidos). - Mostrar el detalle del pedido
 * en la tabla de facturación. - Gestionar pagos (efectivo / tarjeta / sinpe) en
 * modo completo o por productos. - Validar montos, referencias, cédulas
 * autorizadas y reglas de vuelto. - Generar o actualizar una factura asociada a
 * un pedido y registrar pagos y sus detalles.
 *
 * @author Jefferson granados
 */
public class ControllerFacturacion {

    /**
     * Registro débil de controladores por vista, para poder recuperar el
     * controlador asociado a una instancia específica de GestionFacturacion sin
     * fugas de memoria.
     */
    private static final Map<GestionFacturacion, ControllerFacturacion> REGISTRY = new WeakHashMap<>();

    /**
     * Obtiene el controlador asociado a la vista indicada (si existe en el
     * registro).
     *
     * @param vista instancia de la vista GestionFacturacion.
     * @return controlador asociado o null si no existe.
     */
    public static ControllerFacturacion getFor(GestionFacturacion vista) {
        return REGISTRY.get(vista);
    }

    /**
     * Vista principal del módulo de facturación (UI).
     */
    private final GestionFacturacion vista;

    /**
     * DAO para operaciones de factura (crear, buscar, siguiente ID, etc.).
     */
    private final FacturacionDAO facturaDAO;

    /**
     * DAO para registrar pagos de factura.
     */
    private final PagosFacturaDAO pagosDAO;

    /**
     * DAO para leer pedidos desde almacenamiento (ej: pedidos.txt).
     */
    private final pedidosDAO pedidosDAO;

    /**
     * DAO singleton para consulta de productos (nombre, etc.).
     */
    private final productosDAO productosDao;

    /**
     * DAO para registrar detalle por producto pagado (cuando es modo por
     * productos).
     */
    private final PagosFacturaDetalleDAO pagosDetalleDAO;

    /**
     * Set con índices de filas (tabla factura) que ya fueron pagadas (modo por
     * productos). Sirve para bloquear selección/edición de dichas filas.
     */
    private final Set<Integer> filasPagadas = new HashSet<>();

    /**
     * Lista paralela a la tabla de pagos: por cada pago agregado, guarda el
     * conjunto de filas de factura que se liquidaron con ese pago (solo aplica
     * en modo por productos).
     */
    private final List<Set<Integer>> filasPagadasPorPago = new ArrayList<>();

    /**
     * En búsqueda por mesa, se guardan las cédulas válidas provenientes de los
     * pedidos de esa mesa. Luego se valida que el pagador pertenezca a esas
     * cédulas.
     */
    private final Set<String> cedulasPermitidas = new HashSet<>();

    /**
     * Bandera para indicar que se cargó información por mesa (unificando
     * pedidos), y no un pedido individual.
     */
    private boolean modoMesaCargado = false;

    /**
     * Modelo de tabla para la factura (detalle de
     * productos/cantidades/precios).
     */
    private DefaultTableModel modeloFactura;

    /**
     * Modelo de tabla para pagos (método, monto, referencia, cédula).
     */
    private DefaultTableModel modeloPagos;

    /**
     * ID del pedido cargado actualmente. - >0 : pedido individual. - 0 : sin
     * pedido cargado. - <0 : modo mesa (se usa -mesa como marcador).
     */
    private int idPedidoActual = 0;

    /**
     * Método de pago seleccionado actualmente en UI (EFECTIVO/TARJETA/SINPE).
     */
    private String metodoPagoSeleccionado = null;

    /**
     * Montos totales del pedido cargado.
     */
    private int subtotalPedido = 0;
    private int ivaPedido = 0;
    private int totalPedido = 0;

    /**
     * Montos que se van a pagar según el modo (completo o por productos
     * seleccionados).
     */
    private int subtotalAPagar = 0;
    private int ivaAPagar = 0;
    private int totalAPagar = 0;

    /**
     * Formateador para montos (separadores de miles).
     */
    private final DecimalFormat df = new DecimalFormat("#,##0");

    /**
     * Constructor del controlador. Inicializa DAOs, configura tablas,
     * listeners, carga fecha/hora y deja la UI lista con montos en cero.
     *
     * @param vista instancia de la UI GestionFacturacion.
     */
    public ControllerFacturacion(GestionFacturacion vista) {
        this.vista = vista;
        this.facturaDAO = new FacturacionDAO();
        this.pagosDAO = new PagosFacturaDAO();
        this.pedidosDAO = new pedidosDAO();
        this.productosDao = productosDAO.getInstancia();
        this.pagosDetalleDAO = new PagosFacturaDetalleDAO();

        configurarTablaFactura();
        configurarTablaPagos();

        ocultarTablaPagos();

        configurarListeners();

        vista.getTblPagos().setVisible(false);
        vista.getTblPagos().getParent().setVisible(false);
        vista.revalidate();
        vista.repaint();

        cargarDatosIniciales();
        limpiarPagosUI();
        setMontosCeroUI();

        REGISTRY.put(vista, this);
    }

    /**
     * Configura la tabla principal de facturación: - Define columnas y tipos. -
     * Controla edición de selección (col 0) solo en modo por productos y si no
     * está pagada. - Agrega listener para recalcular montos al cambiar
     * selección.
     */
    private void configurarTablaFactura() {
        modeloFactura = new DefaultTableModel(
                new String[]{"Sel", "Cantidad", "Producto", "Precio", "Total"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 ->
                        Boolean.class;
                    case 1, 3, 4 ->
                        Integer.class;
                    default ->
                        String.class;
                };
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return (col == 0)
                        && isModoPorProductos()
                        && !ControllerFacturacion.this.filaEstaPagada(row);
            }
        };

        vista.getjTableFacturacion().setModel(modeloFactura);

        // Cuando cambia la selección (columna 0) en modo por productos,
        // se recalcula el monto a pagar y el estado pagado/saldo.
        modeloFactura.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                recalcularMontoAPagarSegunModo();
                recalcularPagadoYSaldo();
            }
        });
    }

    /**
     * Normaliza una cédula dejando únicamente dígitos, y eliminando
     * espacios/símbolos. Si viene null retorna "".
     *
     * @param c cédula original ingresada.
     * @return cédula normalizada solo con números.
     */
    private String normalizarCedula(String c) {
        if (c == null) {
            return "";
        }
        return c.trim().replaceAll("[^0-9]", "");
    }

    /**
     * Oculta visualmente la tabla de pagos y su contenedor (JScrollPane si
     * existe). Se usa para mantener la UI limpia según el diseño.
     */
    private void ocultarTablaPagos() {
        JTable t = vista.getTblPagos();
        if (t == null) {
            return;
        }

        // Oculta la tabla
        t.setVisible(false);

        // Busca y oculta el JScrollPane que la contiene
        java.awt.Component c = t;
        while (c != null && !(c instanceof javax.swing.JScrollPane)) {
            c = c.getParent();
        }
        if (c != null) {
            c.setVisible(false);
        } else {
            // Fallback: al menos oculta los padres inmediatos
            java.awt.Container p = t.getParent();
            if (p != null) {
                p.setVisible(false);
            }
        }

        vista.revalidate();
        vista.repaint();
    }

    /**
     * Procesa el pago final: - Valida que haya un pedido cargado y al menos un
     * pago agregado. - En modo por productos: exige que no queden productos
     * pendientes. - En modo completo: valida monto suficiente y reglas de
     * vuelto (solo con efectivo). - Crea o reutiliza factura (si ya existía
     * para el pedido). - Guarda cada pago y, si aplica, el detalle de productos
     * pagados por pago. - Muestra confirmación y limpia la factura completa.
     */
    private void procesarPago() {
        vista.getBtnRealizarPedido().setEnabled(false);

        try {
            if (modeloFactura.getRowCount() == 0) {
                JOptionPane.showMessageDialog(vista, "Primero cargue un pedido o una mesa con productos.");
                return;
            }

            if (modeloPagos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(vista, "Debe agregar al menos un pago.");
                return;
            }

            // Validaciones según modo
            if (isModoPorProductos()) {
                int subPend = subtotalPendienteNoPagado();
                if (subPend > 0) {
                    int ivaPend = ivaProporcional(subPend);
                    int totalPend = subPend + ivaPend;
                    JOptionPane.showMessageDialog(vista,
                            "Aún quedan productos pendientes.\nFalta: ₡" + df.format(totalPend));
                    return;
                }

                // Si no quedan pendientes, se registra como pago total del pedido.
                subtotalAPagar = subtotalPedido;
                ivaAPagar = ivaPedido;
                totalAPagar = totalPedido;

            } else {
                // Modo completo: valida monto pagado vs total y reglas de vuelto
                int pagado = pagadoTotalEnTabla();
                int saldo = totalPedido - pagado;

                boolean hayEfectivo = false;
                for (int i = 0; i < modeloPagos.getRowCount(); i++) {
                    String met = String.valueOf(modeloPagos.getValueAt(i, 0));
                    if ("EFECTIVO".equalsIgnoreCase(met)) {
                        hayEfectivo = true;
                    }
                }

                if (saldo > 0) {
                    JOptionPane.showMessageDialog(vista, "Pago insuficiente. Falta: ₡" + df.format(saldo));
                    return;
                }
                if (saldo < 0 && !hayEfectivo) {
                    JOptionPane.showMessageDialog(vista, "Solo se permite vuelto si hay pago en EFECTIVO.");
                    return;
                }

                subtotalAPagar = subtotalPedido;
                ivaAPagar = ivaPedido;
                totalAPagar = totalPedido;
            }

            int idFactura;
            boolean facturaNueva = false;

            // Busca factura existente si el pedido es individual (>0)
            String[] facturaExistente = null;
            if (idPedidoActual > 0) {
                facturaExistente = facturaDAO.buscarFacturaPorIdPedido(idPedidoActual);
            }

            // Si existe factura -> se reutiliza el idFactura, si no -> se crea nueva
            if (facturaExistente != null) {
                idFactura = Integer.parseInt(facturaExistente[0].trim());
            } else {
                idFactura = facturaDAO.siguienteIdFactura();
                facturaNueva = true;

                String fecha = vista.getTxtFecha().getText().trim();
                String hora = vista.getTxtHora().getText().trim();
                String mesa = vista.getTxtMesaTipo().getText().trim();

                // En este flujo, se usa el texto del cliente como cédula y nombre (según implementación actual).
                String clienteInfo = vista.getTxtCliente().getText().trim();
                String cedulaCliente = clienteInfo;
                String nombreCliente = clienteInfo;

                // Si solo hay un pago, el método es ese; si no, MIXTO.
                String metodoFactura = (modeloPagos.getRowCount() == 1)
                        ? String.valueOf(modeloPagos.getValueAt(0, 0))
                        : "MIXTO";

                int idPedidoParaFactura = (idPedidoActual > 0) ? idPedidoActual : 0;

                facturaDAO.guardarFactura(
                        idFactura, fecha, hora, idPedidoParaFactura,
                        cedulaCliente, nombreCliente, mesa,
                        subtotalAPagar, ivaAPagar, totalAPagar, metodoFactura
                );
            }

            // Guarda cada pago y su detalle por producto (si aplica)
            for (int i = 0; i < modeloPagos.getRowCount(); i++) {
                String met = String.valueOf(modeloPagos.getValueAt(i, 0));
                int monto = ((Number) modeloPagos.getValueAt(i, 1)).intValue();
                String ref = String.valueOf(modeloPagos.getValueAt(i, 2));
                String cedulaPagador = String.valueOf(modeloPagos.getValueAt(i, 3));

                pagosDAO.guardarPago(idFactura, met, monto, ref, cedulaPagador);

                // En modo por productos, si hay filas asociadas a este pago,
                // se registra el detalle para auditoría / trazabilidad.
                if (isModoPorProductos() && i < filasPagadasPorPago.size()) {
                    Set<Integer> filas = filasPagadasPorPago.get(i);
                    if (filas != null && !filas.isEmpty()) {
                        for (Integer row : filas) {
                            if (row == null) {
                                continue;
                            }
                            if (row < 0 || row >= modeloFactura.getRowCount()) {
                                continue;
                            }

                            int cant = ((Number) modeloFactura.getValueAt(row, 1)).intValue();
                            String nombreProducto = String.valueOf(modeloFactura.getValueAt(row, 2));
                            int precio = ((Number) modeloFactura.getValueAt(row, 3)).intValue();
                            int totalLinea = ((Number) modeloFactura.getValueAt(row, 4)).intValue();

                            int idPedidoDetalle = (idPedidoActual > 0) ? idPedidoActual : 0;

                            pagosDetalleDAO.guardarDetalle(
                                    idFactura,
                                    idPedidoDetalle,
                                    met,
                                    ref,
                                    cedulaPagador,
                                    nombreProducto,
                                    cant,
                                    precio,
                                    totalLinea
                            );
                        }
                    }
                }
            }

            String msg = "Factura #" + idFactura + (facturaNueva ? " generada" : " actualizada") + " con éxito\n"
                    + "Total: ₡" + df.format(totalPedido);

            JOptionPane.showMessageDialog(vista, msg);
            //
            try {
                String cedula = vista.getTxtCliente().getText().trim(); // aquí viene la cédula

                if (!cedula.isEmpty()) {

                    // Normaliza a solo números (método ya existe en este controlador)
                    String cedNorm = normalizarCedula(cedula);

                    if (!cedNorm.isEmpty()) {

                        // Total real pagado (ya calculado en este controlador)
                        double monto = (double) totalPedido;

                        // Fecha actual para "ultima visita"
                        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                        ClientsDAO cdao = new ClientsDAO();
                        cdao.sumarVisitaYTotal(cedNorm, monto, fechaHoy);
                    }
                }
            } catch (Exception ex) {
                System.out.println("No se pudo actualizar cliente: " + ex.getMessage());
            }
            //
            limpiarFacturaCompleta();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
        } finally {
            vista.getBtnRealizarPedido().setEnabled(true);
        }
    }

    /**
     * Configura la tabla de pagos: - Define columnas y tipos (monto como
     * Integer). - Evita edición directa de celdas.
     */
    private void configurarTablaPagos() {

        modeloPagos = new DefaultTableModel(
                new String[]{"Metodo", "Monto", "Referencia", "Cedula"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1) ? Integer.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        vista.getTblPagos().setModel(modeloPagos);
    }

    /**
     * Enlaza acciones de UI (botones, combos, inputs) con métodos del
     * controlador: - Selección de método de pago. - Agregar/eliminar pagos. -
     * Procesar pago. - Buscar/cargar pedido. - Limpiar pedido. - Cambios de
     * modo de pago (completo vs por productos).
     */
    private void configurarListeners() {
        vista.getBtnEfectivo().addActionListener(e -> seleccionarMetodoPago("EFECTIVO"));
        vista.getBtnTarjeta().addActionListener(e -> seleccionarMetodoPago("TARJETA"));
        vista.getBtnSinpeMovil().addActionListener(e -> seleccionarMetodoPago("SINPE"));

        vista.getBtnAgregarPago().addActionListener(e -> agregarPago());
        vista.getBtnEliminarPago().addActionListener(e -> eliminarPago());

        vista.getBtnRealizarPedido().addActionListener(e -> procesarPago());

        vista.getBtnBuscarFacturas().addActionListener(e -> buscarYCargarPedido());
        vista.getTxtIFIDFactura().addActionListener(e -> buscarYCargarPedido());

        vista.getBtnLimpiarPedido().addActionListener(e -> limpiarFacturaCompleta());

        vista.getCmbModoPago().addActionListener(e -> {
            aplicarModoPagoUI();
            recalcularMontoAPagarSegunModo();
            recalcularPagadoYSaldo();
        });
    }

    /**
     * Carga fecha y hora actual al iniciar el controlador. Se usa para
     * precargar campos en la UI.
     */
    private void cargarDatosIniciales() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();

        vista.getTxtFecha().setText(dateFormat.format(now));
        vista.getTxtHora().setText(timeFormat.format(now));
    }

    /**
     * Deja todos los montos visibles en cero (subtotal, IVA, total, pagado,
     * etc.).
     */
    private void setMontosCeroUI() {
        vista.getTxtSubTotal().setText("₡0");
        vista.getTxtIVA().setText("₡0");
        vista.getTxtMontoTotal().setText("₡0");
        vista.getTxtMontoaPagar().setText("₡0");
        vista.getTxtPagado().setText("₡0");
        vista.getTxtSaldo().setText("—");
    }

    /* ------------------------- PEDIDOS.TXT -------------------------
       ID_PEDIDO, FECHA, HORA, MESA, CEDULA_CLIENTE, ITEMS, SUBTOTAL, IVA, TOTAL
       indices: 0      1      2     3     4              5     6        7   8
    ----------------------------------------------------------------- */
    /**
     * Busca y carga un pedido en la UI ya sea por: - ID de pedido
     * (vista.txtIFIDFactura), o - número de mesa (vista.txtMesaTipo) uniendo
     * varios pedidos.
     *
     * Flujo: 1) Limpia cédulas permitidas y estado de modoMesa. 2) Si hay ID:
     * valida, busca línea de pedido, parsea y carga productos/valores. 3) Si no
     * hay ID y hay mesa: busca todas las líneas de esa mesa, une items y suma
     * montos. 4) Recalcula UI y muestra mensajes según tenga factura previa o
     * no.
     */
    private void buscarYCargarPedido() {
        cedulasPermitidas.clear();
        modoMesaCargado = false;

        String txtId = vista.getTxtIFIDFactura().getText().trim();
        String txtMesa = vista.getTxtMesaTipo().getText().trim();

        // Búsqueda por ID de pedido
        if (!txtId.isEmpty()) {
            int idPedido;
            try {
                idPedido = Integer.parseInt(txtId);
                if (idPedido <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(vista, "El ID del pedido debe ser numérico válido.");
                return;
            }

            String lineaPedido = pedidosDAO.obtenerPedidoLineaPorId(idPedido);
            if (lineaPedido == null) {
                JOptionPane.showMessageDialog(vista, "No existe el pedido #" + idPedido);
                return;
            }

            String[] partes = lineaPedido.split(",", -1);
            if (partes.length < 9) {
                JOptionPane.showMessageDialog(vista,
                        "Pedido #" + idPedido + " tiene formato inválido.\nVerifique pedidos.txt");
                return;
            }

            this.idPedidoActual = idPedido;

            try {
                vista.getTxtFecha().setText(partes[1].trim());
                vista.getTxtHora().setText(partes[2].trim());
                vista.getTxtMesaTipo().setText(partes[3].trim());
                vista.getTxtCliente().setText(partes[4].trim());

                // Items vienen codificados como: id|cant|precio|total;id|cant|precio|total;...
                cargarItemsDesdeLineaPedido(partes[5].trim());

                subtotalPedido = Integer.parseInt(partes[6].trim());
                ivaPedido = Integer.parseInt(partes[7].trim());
                totalPedido = Integer.parseInt(partes[8].trim());

            } catch (Exception ex) {
                subtotalPedido = 0;
                ivaPedido = 0;
                totalPedido = 0;

                JOptionPane.showMessageDialog(vista,
                        "Pedido #" + idPedido + " tiene datos corruptos.\nRevise pedidos.txt");
                return;
            }

            // En modo ID, solo se permite pagar con la cédula del pedido
            cedulasPermitidas.add(normalizarCedula(partes[4].trim()));
            modoMesaCargado = false;

            vista.getTxtSubTotal().setText("₡" + df.format(subtotalPedido));
            vista.getTxtIVA().setText("₡" + df.format(ivaPedido));
            vista.getTxtMontoTotal().setText("₡" + df.format(totalPedido));

            aplicarModoPagoUI();
            recalcularMontoAPagarSegunModo();

            limpiarPagosUI();
            recalcularPagadoYSaldo();

            // Mensaje informativo si ya existe factura para el pedido
            String[] factura = facturaDAO.buscarFacturaPorIdPedido(idPedido);
            if (factura != null) {
                JOptionPane.showMessageDialog(vista,
                        "Pedido #" + idPedido + " cargado.\nFactura existente: #" + factura[0]);
            } else {
                JOptionPane.showMessageDialog(vista,
                        "Pedido #" + idPedido + " cargado.\nAún no tiene factura.");
            }

            return;
        }

        // Búsqueda por mesa (unifica varios pedidos)
        if (!txtMesa.isEmpty()) {
            int mesa;
            try {
                mesa = Integer.parseInt(txtMesa.replaceAll("[^0-9]", ""));
                if (mesa <= 0) {
                    throw new NumberFormatException();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vista, "La mesa debe ser un número válido.");
                return;
            }

            List<String> lineas = pedidosDAO.obtenerPedidosLineasPorMesa(mesa);

            if (lineas == null || lineas.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "No hay pedidos registrados para la mesa " + mesa + ".");
                return;
            }

            modeloFactura.setRowCount(0);
            filasPagadas.clear();
            filasPagadasPorPago.clear();

            String fecha = "";
            String hora = "";
            String cliente = "MESA " + mesa + " (VARIOS)";

            StringBuilder itemsAll = new StringBuilder();
            int sub = 0;
            int iva = 0;
            int total = 0;

            // Recorre los pedidos de la mesa para unir items y sumar montos
            for (String linea : lineas) {
                if (linea == null || linea.isBlank()) {
                    continue;
                }

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) {
                    continue;
                }

                if (fecha.isEmpty()) {
                    fecha = partes[1].trim();
                }
                if (hora.isEmpty()) {
                    hora = partes[2].trim();
                }

                cedulasPermitidas.add(normalizarCedula(partes[4].trim()));

                String items = partes[5].trim();
                if (!items.isEmpty()) {
                    if (itemsAll.length() > 0 && itemsAll.charAt(itemsAll.length() - 1) != ';') {
                        itemsAll.append(";");
                    }
                    itemsAll.append(items);
                    if (itemsAll.length() > 0 && itemsAll.charAt(itemsAll.length() - 1) != ';') {
                        itemsAll.append(";");
                    }
                }

                try {
                    sub += Integer.parseInt(partes[6].trim());
                    iva += Integer.parseInt(partes[7].trim());
                    total += Integer.parseInt(partes[8].trim());
                } catch (Exception ignored) {
                }
            }

            // Marca el estado como "mesa cargada"
            this.idPedidoActual = -mesa;
            modoMesaCargado = true;

            vista.getTxtFecha().setText(fecha);
            vista.getTxtHora().setText(hora);
            vista.getTxtMesaTipo().setText(String.valueOf(mesa));
            vista.getTxtCliente().setText(cliente);

            cargarItemsDesdeLineaPedido(itemsAll.toString());

            subtotalPedido = sub;
            ivaPedido = iva;
            totalPedido = total;

            vista.getTxtSubTotal().setText("₡" + df.format(subtotalPedido));
            vista.getTxtIVA().setText("₡" + df.format(ivaPedido));
            vista.getTxtMontoTotal().setText("₡" + df.format(totalPedido));

            aplicarModoPagoUI();
            recalcularMontoAPagarSegunModo();

            limpiarPagosUI();
            recalcularPagadoYSaldo();

            JOptionPane.showMessageDialog(vista,
                    "Mesa " + mesa + " cargada.\nPedidos encontrados: " + lineas.size()
                    + "\nTotal: ₡" + df.format(totalPedido));
            return;
        }

        JOptionPane.showMessageDialog(vista, "Ingrese el ID del pedido o el número de mesa.");
    }

    /**
     * Carga items codificados en un string (formato: id|cant|precio|total;...),
     * los convierte en filas de la tabla de factura y consulta el nombre real
     * del producto.
     *
     * @param itemsStr string de items codificados.
     */
    private void cargarItemsDesdeLineaPedido(String itemsStr) {
        modeloFactura.setRowCount(0);

        filasPagadas.clear();
        filasPagadasPorPago.clear();

        if (itemsStr == null || itemsStr.isBlank()) {
            return;
        }

        String[] items = itemsStr.split(";");

        for (String it : items) {
            if (it == null || it.isBlank()) {
                continue;
            }

            String[] p = it.split("\\|");
            if (p.length < 4) {
                continue;
            }

            try {
                String idProd = p[0].trim();
                int cant = Integer.parseInt(p[1].trim());
                int precio = Integer.parseInt(p[2].trim());
                int total = Integer.parseInt(p[3].trim());

                // Nombre por defecto si no se encuentra en catálogo
                String nombre = "Producto " + idProd;
                Product prod = productosDao.buscarProductoPorId(idProd);
                if (prod != null) {
                    nombre = prod.getNameProduct();
                }

                modeloFactura.addRow(new Object[]{Boolean.FALSE, cant, nombre, precio, total});
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Determina si el modo de pago seleccionado en el combo es "por productos".
     * Se basa en texto del item (contiene "por").
     *
     * @return true si es modo por productos, false si es modo completo.
     */
    private boolean isModoPorProductos() {
        String sel = String.valueOf(vista.getCmbModoPago().getSelectedItem()).trim().toLowerCase();
        return sel.contains("por");
    }

    /**
     * Ajusta la selección de la columna "Sel" según modo: - Modo completo:
     * selecciona todo. - Modo por productos: solo marca como seleccionadas las
     * filas ya pagadas; las demás quedan desmarcadas.
     */
    private void aplicarModoPagoUI() {
        if (modeloFactura.getRowCount() == 0) {
            return;
        }

        if (!isModoPorProductos()) {
            for (int i = 0; i < modeloFactura.getRowCount(); i++) {
                modeloFactura.setValueAt(Boolean.TRUE, i, 0);
            }
        } else {
            for (int i = 0; i < modeloFactura.getRowCount(); i++) {
                if (filaEstaPagada(i)) {
                    modeloFactura.setValueAt(Boolean.TRUE, i, 0);
                } else {
                    modeloFactura.setValueAt(Boolean.FALSE, i, 0);
                }
            }
        }
    }

    /**
     * Recalcula el monto a pagar dependiendo del modo: - Si no hay filas: todo
     * en cero. - Modo completo: monto a pagar = total del pedido. - Modo por
     * productos: monto a pagar = total (subtotal seleccionado + IVA
     * proporcional).
     */
    private void recalcularMontoAPagarSegunModo() {
        if (modeloFactura.getRowCount() == 0) {
            subtotalAPagar = 0;
            ivaAPagar = 0;
            totalAPagar = 0;
            vista.getTxtMontoaPagar().setText("₡0");
            return;
        }

        if (!isModoPorProductos()) {
            subtotalAPagar = subtotalPedido;
            ivaAPagar = ivaPedido;
            totalAPagar = totalPedido;
            vista.getTxtMontoaPagar().setText("₡" + df.format(totalAPagar));
            return;
        }

        int subSel = subtotalSeleccionadoNoPagado();
        subtotalAPagar = subSel;
        ivaAPagar = ivaProporcional(subSel);
        totalAPagar = subtotalAPagar + ivaAPagar;

        vista.getTxtMontoaPagar().setText("₡" + df.format(totalAPagar));
    }

    /**
     * Agrega un pago a la tabla de pagos: - Valida que exista pedido cargado. -
     * Valida método de pago seleccionado. - Valida referencia si no es
     * efectivo. - Solicita cédula del pagador y valida formato y pertenencia
     * (si es búsqueda por mesa).
     *
     * Modo por productos: - Solo permite pagar productos seleccionados no
     * pagados. - Calcula IVA proporcional del subtotal seleccionado. - Marca
     * filas como pagadas y las asocia al pago en filasPagadasPorPago.
     *
     * Modo completo: - Solicita monto, permite excedente solo si hay efectivo
     * (se valida luego en saldo/vuelto). - No asocia filas al pago (usa set
     * vacío).
     */
    private void agregarPago() {
        if (modeloFactura.getRowCount() == 0) {
            JOptionPane.showMessageDialog(vista, "Primero cargue un pedido o una mesa con productos.");
            return;
        }

        if (metodoPagoSeleccionado == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione un método de pago (SINPE / Tarjeta / Efectivo).");
            return;
        }

        boolean esBusquedaPorMesa = vista.getTxtIFIDFactura().getText().trim().isEmpty()
                && !vista.getTxtMesaTipo().getText().trim().isEmpty();

        // MODO POR PRODUCTOS
        if (isModoPorProductos()) {
            int subSel = subtotalSeleccionadoNoPagado();
            if (subSel <= 0) {
                JOptionPane.showMessageDialog(vista, "Seleccione al menos un producto pendiente para pagar.");
                return;
            }

            int ivaSel = ivaProporcional(subSel);
            int totalSel = subSel + ivaSel;

            String referencia = "";
            if (!"EFECTIVO".equalsIgnoreCase(metodoPagoSeleccionado)) {
                referencia = JOptionPane.showInputDialog(vista, "Referencia (" + metodoPagoSeleccionado + "):", "");
                if (referencia == null) {
                    return;
                }

                referencia = referencia.trim();
                if (referencia.isEmpty()) {
                    JOptionPane.showMessageDialog(vista, "La referencia es obligatoria para " + metodoPagoSeleccionado + ".");
                    return;
                }
            }

            String cedulaPagador = JOptionPane.showInputDialog(vista, "Cédula del pagador:", "");
            if (cedulaPagador == null) {
                return;
            }

            cedulaPagador = cedulaPagador.trim();
            if (cedulaPagador.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "La cédula del pagador es obligatoria.");
                return;
            }

            String cedNorm = normalizarCedula(cedulaPagador);
            if (cedNorm.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "La cédula del pagador debe contener números.");
                return;
            }

            // En búsqueda por mesa: solo se acepta cédula si pertenece a alguno de los pedidos encontrados
            if (esBusquedaPorMesa) {
                if (cedulasPermitidas.isEmpty()) {
                    JOptionPane.showMessageDialog(vista, "No se encontraron cédulas válidas en los pedidos de esta mesa.");
                    return;
                }
                if (!cedulasPermitidas.contains(cedNorm)) {
                    JOptionPane.showMessageDialog(vista, "La cédula no pertenece a ninguno de los pedidos de esta mesa.");
                    return;
                }
            }

            // Marca filas seleccionadas como pagadas y las asocia a este pago
            Set<Integer> filasDeEstePago = new HashSet<>();
            for (int i = 0; i < modeloFactura.getRowCount(); i++) {
                Boolean sel = (Boolean) modeloFactura.getValueAt(i, 0);
                if (Boolean.TRUE.equals(sel) && !filaEstaPagada(i)) {
                    filasPagadas.add(i);
                    filasDeEstePago.add(i);
                    modeloFactura.setValueAt(Boolean.TRUE, i, 0);
                }
            }

            modeloPagos.addRow(new Object[]{metodoPagoSeleccionado, totalSel, referencia, cedNorm});
            filasPagadasPorPago.add(filasDeEstePago);

            // Limpia selección de filas pendientes para el siguiente pago
            for (int i = 0; i < modeloFactura.getRowCount(); i++) {
                if (!filaEstaPagada(i)) {
                    modeloFactura.setValueAt(Boolean.FALSE, i, 0);
                }
            }

            recalcularMontoAPagarSegunModo();
            recalcularPagadoYSaldo();
            return;
        }

        // MODO COMPLETO
        if (totalAPagar <= 0) {
            JOptionPane.showMessageDialog(vista, "No hay monto a pagar.");
            return;
        }

        String montoStr = JOptionPane.showInputDialog(vista, "Monto (" + metodoPagoSeleccionado + "):", "");
        if (montoStr == null) {
            return;
        }

        int monto;
        try {
            montoStr = montoStr.trim().replaceAll("[^0-9]", "");
            if (montoStr.isEmpty()) {
                throw new NumberFormatException();
            }
            monto = Integer.parseInt(montoStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Monto inválido.");
            return;
        }

        if (monto <= 0) {
            JOptionPane.showMessageDialog(vista, "El monto debe ser mayor a 0.");
            return;
        }

        String referencia = "";
        if (!"EFECTIVO".equalsIgnoreCase(metodoPagoSeleccionado)) {
            referencia = JOptionPane.showInputDialog(vista, "Referencia (" + metodoPagoSeleccionado + "):", "");
            if (referencia == null) {
                return;
            }

            referencia = referencia.trim();
            if (referencia.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "La referencia es obligatoria para " + metodoPagoSeleccionado + ".");
                return;
            }
        }

        String cedulaPagador = JOptionPane.showInputDialog(vista, "Cédula del pagador:", "");
        if (cedulaPagador == null) {
            return;
        }

        cedulaPagador = cedulaPagador.trim();
        if (cedulaPagador.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "La cédula del pagador es obligatoria.");
            return;
        }

        String cedNorm = normalizarCedula(cedulaPagador);
        if (cedNorm.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "La cédula del pagador debe contener números.");
            return;
        }

        if (esBusquedaPorMesa) {
            if (cedulasPermitidas.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "No se encontraron cédulas válidas en los pedidos de esta mesa.");
                return;
            }
            if (!cedulasPermitidas.contains(cedNorm)) {
                JOptionPane.showMessageDialog(vista, "La cédula no pertenece a ninguno de los pedidos de esta mesa.");
                return;
            }
        }

        modeloPagos.addRow(new Object[]{metodoPagoSeleccionado, monto, referencia, cedNorm});
        filasPagadasPorPago.add(Collections.emptySet());

        recalcularPagadoYSaldo();
    }

    /**
     * Elimina un pago seleccionado de la tabla de pagos. - Si es modo por
     * productos: desmarca como pagadas las filas asociadas a ese pago. - Luego
     * elimina la fila del modelo y recalcula montos/estado.
     */
    private void eliminarPago() {
        int row = vista.getTblPagos().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione un pago para eliminar.");
            return;
        }

        if (isModoPorProductos() && row < filasPagadasPorPago.size()) {
            Set<Integer> filas = filasPagadasPorPago.get(row);
            if (filas != null && !filas.isEmpty()) {
                for (Integer f : filas) {
                    filasPagadas.remove(f);
                    if (f >= 0 && f < modeloFactura.getRowCount()) {
                        modeloFactura.setValueAt(Boolean.FALSE, f, 0);
                    }
                }
            }
        }

        modeloPagos.removeRow(row);
        if (row < filasPagadasPorPago.size()) {
            filasPagadasPorPago.remove(row);
        }

        aplicarModoPagoUI();
        recalcularMontoAPagarSegunModo();
        recalcularPagadoYSaldo();
    }

    /**
     * Recalcula: - Total pagado (sumatoria tabla pagos). - Estado de saldo en
     * UI (pendiente, pagado, vuelto, excedente no permitido).
     *
     * En modo completo: - Se valida vuelto solo si existe pago en efectivo.
     *
     * En modo por productos: - Se calcula pendiente a partir de productos no
     * pagados con IVA proporcional.
     */
    private void recalcularPagadoYSaldo() {
        int pagado = pagadoTotalEnTabla();
        vista.getTxtPagado().setText("₡" + df.format(pagado));

        if (totalPedido <= 0) {
            vista.getTxtSaldo().setText("—");
            return;
        }

        if (!isModoPorProductos()) {
            int pendiente = totalPedido - pagado;

            boolean hayEfectivo = false;
            for (int i = 0; i < modeloPagos.getRowCount(); i++) {
                String met = String.valueOf(modeloPagos.getValueAt(i, 0));
                if ("EFECTIVO".equalsIgnoreCase(met)) {
                    hayEfectivo = true;
                }
            }

            if (pagado == 0) {
                vista.getTxtSaldo().setText("Pendiente");
                return;
            }
            if (pendiente > 0) {
                vista.getTxtSaldo().setText("Pendiente: ₡" + df.format(pendiente));
                return;
            }
            if (pendiente == 0) {
                vista.getTxtSaldo().setText("Pagado ✔");
                return;
            }

            if (hayEfectivo) {
                vista.getTxtSaldo().setText("Vuelto: ₡" + df.format(Math.abs(pendiente)));
            } else {
                vista.getTxtSaldo().setText("Excedente no permitido");
            }
            return;
        }

        int subPend = subtotalPendienteNoPagado();
        int ivaPend = ivaProporcional(subPend);
        int totalPend = subPend + ivaPend;

        if (totalPend > 0) {
            vista.getTxtSaldo().setText("Pendiente: ₡" + df.format(totalPend));
        } else {
            vista.getTxtSaldo().setText("Pagado ✔");
        }
    }

    /**
     * Limpia el estado de pagos: - Vacía la tabla de pagos y estructuras
     * asociadas. - Reinicia campos pagado/saldo. - Limpia selección de método y
     * estilos. - Reinicia filas pagadas y recalcula UI según modo.
     */
    private void limpiarPagosUI() {
        modeloPagos.setRowCount(0);
        filasPagadasPorPago.clear();

        vista.getTxtPagado().setText("₡0");
        vista.getTxtSaldo().setText("—");
        metodoPagoSeleccionado = null;
        resetearEstilosBotones();

        filasPagadas.clear();
        aplicarModoPagoUI();
        recalcularMontoAPagarSegunModo();
    }

    /**
     * Selecciona un método de pago y resalta el botón correspondiente. También
     * reinicia estilos para evitar múltiples botones “activos”.
     *
     * @param metodo "EFECTIVO", "TARJETA" o "SINPE".
     */
    private void seleccionarMetodoPago(String metodo) {
        this.metodoPagoSeleccionado = metodo;
        resetearEstilosBotones();

        JButton botonActual = null;
        java.awt.Color colorResaltado = new java.awt.Color(0, 140, 255);

        switch (metodo) {
            case "EFECTIVO" -> {
                botonActual = vista.getBtnEfectivo();
                colorResaltado = new java.awt.Color(76, 175, 80);
            }
            case "TARJETA" -> {
                botonActual = vista.getBtnTarjeta();
                colorResaltado = new java.awt.Color(84, 110, 122);
            }
            case "SINPE" -> {
                botonActual = vista.getBtnSinpeMovil();
                colorResaltado = new java.awt.Color(33, 150, 243);
            }
        }

        if (botonActual != null) {
            botonActual.setBackground(colorResaltado.brighter());
            botonActual.setForeground(java.awt.Color.WHITE);
            botonActual.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 2));
        }
    }

    /**
     * Restaura colores/bordes por defecto de los botones de método de pago. Se
     * usa antes de resaltar el método seleccionado.
     */
    private void resetearEstilosBotones() {
        vista.getBtnEfectivo().setBackground(new java.awt.Color(76, 175, 80));
        vista.getBtnEfectivo().setForeground(java.awt.Color.BLACK);
        vista.getBtnEfectivo().setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));

        vista.getBtnTarjeta().setBackground(new java.awt.Color(84, 110, 122));
        vista.getBtnTarjeta().setForeground(java.awt.Color.BLACK);
        vista.getBtnTarjeta().setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));

        vista.getBtnSinpeMovil().setBackground(new java.awt.Color(33, 150, 243));
        vista.getBtnSinpeMovil().setForeground(java.awt.Color.BLACK);
        vista.getBtnSinpeMovil().setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));
    }

    /**
     * Limpia completamente la factura en la UI y reinicia el estado interno: -
     * Tablas (factura y pagos). - Campos de texto (cliente, mesa, id). - IDs y
     * montos. - Estructuras de filas pagadas/cédulas. - Reaplica fecha/hora
     * actual y estilos de botones.
     */
    private void limpiarFacturaCompleta() {
        modeloFactura.setRowCount(0);
        modeloPagos.setRowCount(0);

        vista.getTxtCliente().setText("");
        vista.getTxtMesaTipo().setText("");
        vista.getTxtIFIDFactura().setText("");

        idPedidoActual = 0;

        subtotalPedido = 0;
        ivaPedido = 0;
        totalPedido = 0;
        subtotalAPagar = 0;
        ivaAPagar = 0;
        totalAPagar = 0;

        filasPagadas.clear();
        filasPagadasPorPago.clear();

        cedulasPermitidas.clear();
        modoMesaCargado = false;

        setMontosCeroUI();
        metodoPagoSeleccionado = null;
        resetearEstilosBotones();
        cargarDatosIniciales();
    }

    /**
     * Carga un pedido por ID (helper para flujos externos): - Pone el ID en el
     * campo correspondiente. - Ejecuta búsqueda/carga normal.
     *
     * @param idPedido id del pedido a cargar.
     */
    public void cargarPedidoPorId(int idPedido) {
        vista.getTxtIFIDFactura().setText(String.valueOf(idPedido));
        buscarYCargarPedido();
    }

    /**
     * Indica si una fila del detalle de factura ya está pagada (modo por
     * productos).
     *
     * @param row índice de fila.
     * @return true si está pagada, false si no.
     */
    private boolean filaEstaPagada(int row) {
        return filasPagadas.contains(row);
    }

    /**
     * Obtiene el total de una línea (columna 4) de la tabla factura.
     *
     * @param row índice de fila.
     * @return total de la línea como int (0 si no es Number).
     */
    private int totalLinea(int row) {
        Object val = modeloFactura.getValueAt(row, 4);
        return (val instanceof Number) ? ((Number) val).intValue() : 0;
    }

    /**
     * Calcula subtotal de productos seleccionados que aún NO han sido pagados.
     * Se usa en modo por productos para determinar el monto base a pagar.
     *
     * @return suma de totales de filas seleccionadas no pagadas.
     */
    private int subtotalSeleccionadoNoPagado() {
        int sub = 0;
        for (int i = 0; i < modeloFactura.getRowCount(); i++) {
            Boolean sel = (Boolean) modeloFactura.getValueAt(i, 0);
            if (Boolean.TRUE.equals(sel) && !filaEstaPagada(i)) {
                sub += totalLinea(i);
            }
        }
        return sub;
    }

    /**
     * Calcula subtotal pendiente (sumatoria de líneas no pagadas). Se usa para
     * validar pendientes en modo por productos.
     *
     * @return subtotal pendiente (sin IVA) de filas no pagadas.
     */
    private int subtotalPendienteNoPagado() {
        int sub = 0;
        for (int i = 0; i < modeloFactura.getRowCount(); i++) {
            if (!filaEstaPagada(i)) {
                sub += totalLinea(i);
            }
        }
        return sub;
    }

    /**
     * Calcula IVA proporcional al subtotal base, según proporción del subtotal
     * base respecto al subtotal total del pedido.
     *
     * @param subtotalBase subtotal al que se le quiere aplicar IVA
     * proporcional.
     * @return IVA proporcional redondeado.
     */
    private int ivaProporcional(int subtotalBase) {
        if (subtotalPedido <= 0) {
            return 0;
        }
        double proporcion = (double) subtotalBase / (double) subtotalPedido;
        return (int) Math.round(ivaPedido * proporcion);
    }

    /**
     * Suma todos los montos en la tabla de pagos (columna 1).
     *
     * @return total pagado actualmente.
     */
    private int pagadoTotalEnTabla() {
        int pagado = 0;
        for (int i = 0; i < modeloPagos.getRowCount(); i++) {
            Object m = modeloPagos.getValueAt(i, 1);
            int monto = (m instanceof Number) ? ((Number) m).intValue() : 0;
            pagado += monto;
        }
        return pagado;
    }
}
