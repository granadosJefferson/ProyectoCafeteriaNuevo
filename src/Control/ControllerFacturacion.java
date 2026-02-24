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

public class ControllerFacturacion {

    private static final Map<GestionFacturacion, ControllerFacturacion> REGISTRY = new WeakHashMap<>();

    public static ControllerFacturacion getFor(GestionFacturacion vista) {
        return REGISTRY.get(vista);
    }

    private final GestionFacturacion vista;
    private final FacturacionDAO facturaDAO;
    private final PagosFacturaDAO pagosDAO;
    private final pedidosDAO pedidosDAO;
    private final productosDAO productosDao;
    private final PagosFacturaDetalleDAO pagosDetalleDAO;
    private final Set<Integer> filasPagadas = new HashSet<>();
    private final List<Set<Integer>> filasPagadasPorPago = new ArrayList<>();
    private final Set<String> cedulasPermitidas = new HashSet<>();
    private boolean modoMesaCargado = false;

    private DefaultTableModel modeloFactura;
    private DefaultTableModel modeloPagos;

    private int idPedidoActual = 0;
    private String metodoPagoSeleccionado = null;

    private int subtotalPedido = 0;
    private int ivaPedido = 0;
    private int totalPedido = 0;

    private int subtotalAPagar = 0;
    private int ivaAPagar = 0;
    private int totalAPagar = 0;

    private final DecimalFormat df = new DecimalFormat("#,##0");

    public ControllerFacturacion(GestionFacturacion vista) {
        this.vista = vista;
        this.facturaDAO = new FacturacionDAO();
        this.pagosDAO = new PagosFacturaDAO();
        this.pedidosDAO = new pedidosDAO();
        this.productosDao = productosDAO.getInstancia();
        this.pagosDetalleDAO = new PagosFacturaDetalleDAO();

        configurarTablaFactura();
        configurarTablaPagos();
        configurarListeners();

        cargarDatosIniciales();
        limpiarPagosUI();
        setMontosCeroUI();

        REGISTRY.put(vista, this);
    }

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

        modeloFactura.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                recalcularMontoAPagarSegunModo();
                recalcularPagadoYSaldo();
            }
        });
    }

    private String normalizarCedula(String c) {
        if (c == null) {
            return "";
        }
        return c.trim().replaceAll("[^0-9]", "");
    }

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

            if (isModoPorProductos()) {
                int subPend = subtotalPendienteNoPagado();
                if (subPend > 0) {
                    int ivaPend = ivaProporcional(subPend);
                    int totalPend = subPend + ivaPend;
                    JOptionPane.showMessageDialog(vista,
                            "Aún quedan productos pendientes.\nFalta: ₡" + df.format(totalPend));
                    return;
                }

                subtotalAPagar = subtotalPedido;
                ivaAPagar = ivaPedido;
                totalAPagar = totalPedido;

            } else {
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

            String[] facturaExistente = null;
            if (idPedidoActual > 0) {
                facturaExistente = facturaDAO.buscarFacturaPorIdPedido(idPedidoActual);
            }

            if (facturaExistente != null) {
                idFactura = Integer.parseInt(facturaExistente[0].trim());
            } else {
                idFactura = facturaDAO.siguienteIdFactura();
                facturaNueva = true;

                String fecha = vista.getTxtFecha().getText().trim();
                String hora = vista.getTxtHora().getText().trim();
                String mesa = vista.getTxtMesaTipo().getText().trim();

                String clienteInfo = vista.getTxtCliente().getText().trim();
                String cedulaCliente = clienteInfo;
                String nombreCliente = clienteInfo;

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

            for (int i = 0; i < modeloPagos.getRowCount(); i++) {
                String met = String.valueOf(modeloPagos.getValueAt(i, 0));
                int monto = ((Number) modeloPagos.getValueAt(i, 1)).intValue();
                String ref = String.valueOf(modeloPagos.getValueAt(i, 2));
                String cedulaPagador = String.valueOf(modeloPagos.getValueAt(i, 3));

                pagosDAO.guardarPago(idFactura, met, monto, ref, cedulaPagador);

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

            limpiarFacturaCompleta();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
        } finally {
            vista.getBtnRealizarPedido().setEnabled(true);
        }
    }

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

    private void cargarDatosIniciales() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();

        vista.getTxtFecha().setText(dateFormat.format(now));
        vista.getTxtHora().setText(timeFormat.format(now));
    }

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
    // ControllerFacturacion.java (reemplazar método completo)
    private void buscarYCargarPedido() {
        cedulasPermitidas.clear();
        modoMesaCargado = false;

        String txtId = vista.getTxtIFIDFactura().getText().trim();
        String txtMesa = vista.getTxtMesaTipo().getText().trim();

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

            cedulasPermitidas.add(normalizarCedula(partes[4].trim()));
            modoMesaCargado = false;

            vista.getTxtSubTotal().setText("₡" + df.format(subtotalPedido));
            vista.getTxtIVA().setText("₡" + df.format(ivaPedido));
            vista.getTxtMontoTotal().setText("₡" + df.format(totalPedido));

            aplicarModoPagoUI();
            recalcularMontoAPagarSegunModo();

            limpiarPagosUI();
            recalcularPagadoYSaldo();

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

    private boolean isModoPorProductos() {
        String sel = String.valueOf(vista.getCmbModoPago().getSelectedItem()).trim().toLowerCase();
        return sel.contains("por");
    }

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

            for (int i = 0; i < modeloFactura.getRowCount(); i++) {
                if (!filaEstaPagada(i)) {
                    modeloFactura.setValueAt(Boolean.FALSE, i, 0);
                }
            }

            recalcularMontoAPagarSegunModo();
            recalcularPagadoYSaldo();
            return;
        }

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

    public void cargarPedidoPorId(int idPedido) {
        vista.getTxtIFIDFactura().setText(String.valueOf(idPedido));
        buscarYCargarPedido();
    }

    private boolean filaEstaPagada(int row) {
        return filasPagadas.contains(row);
    }

    private int totalLinea(int row) {
        Object val = modeloFactura.getValueAt(row, 4);
        return (val instanceof Number) ? ((Number) val).intValue() : 0;
    }

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

    private int subtotalPendienteNoPagado() {
        int sub = 0;
        for (int i = 0; i < modeloFactura.getRowCount(); i++) {
            if (!filaEstaPagada(i)) {
                sub += totalLinea(i);
            }
        }
        return sub;
    }

    private int ivaProporcional(int subtotalBase) {
        if (subtotalPedido <= 0) {
            return 0;
        }
        double proporcion = (double) subtotalBase / (double) subtotalPedido;
        return (int) Math.round(ivaPedido * proporcion);
    }

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
