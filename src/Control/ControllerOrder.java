package Control;

import Modelo.Clients;
import Modelo.ClientsDAO;
import Modelo.ItemPedido;
import Modelo.Product;
import Modelo.pedidosDAO;
import Modelo.productosDAO;
import Vista.OrderItemCard;
import Vista.ProductCard;
import Vista.orders;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ControllerOrder {

    private final orders vista;

    
    private final java.util.List<String>[] cedulasPorMesa = new java.util.ArrayList[5];

    private final ClientsDAO clientsDAO;
    private final productosDAO dao;
    private final List<ItemPedido> carrito = new ArrayList<>();
    private String categoriaActual = "Todos";
    private Clients clienteActual = null;
    private final pedidosDAO pedidosDAO = new pedidosDAO();
    private String mesaActual = null;

    private final java.awt.Color COLOR_MESA_NORMAL = new java.awt.Color(153, 153, 153); // gris
    private final java.awt.Color COLOR_MESA_SEL = new java.awt.Color(0, 140, 255);     // azul
    private final java.awt.Color COLOR_MESA_LLENA = new java.awt.Color(220, 60, 60);   // rojo

    private final java.util.List<JButton> botonesMesa = new java.util.ArrayList<>();

    public ControllerOrder(orders vista) {
        for (int i = 0; i < cedulasPorMesa.length; i++) {
            cedulasPorMesa[i] = new java.util.ArrayList<>();
        }

        this.vista = vista;
        this.clientsDAO = new ClientsDAO();
        this.dao = productosDAO.getInstancia();

        this.dao.addStockChangeCallback(() -> SwingUtilities.invokeLater(() -> {
            System.out.println("Stock actualizado - Recargando productos en pedidos");
            cargarProductos();
            recargarResumen();
        }));

        this.vista.getBtnSearch().addActionListener(e -> buscarCliente());
        this.vista.getBtnPedir().addActionListener(e -> realizarPedido());

        vista.getTxtBuscarProducto().setForeground(java.awt.Color.BLACK);

        vista.getBtnClean().addActionListener(e -> limpiarPedidoUI());

        vista.getTxtBuscarProducto().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { cargarProductos(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { cargarProductos(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { cargarProductos(); }
        });

        configurarMesas();
        configurarProductos();
        configurarPedidos();

        vista.getBtnAll().addActionListener(e -> { categoriaActual = "Todos"; cargarProductos(); });
        vista.getBtnCafe().addActionListener(e -> { categoriaActual = "Cafe"; cargarProductos(); });
        vista.getBtnDrinks().addActionListener(e -> { categoriaActual = "Lacteos"; cargarProductos(); });
        vista.getBtnPostres().addActionListener(e -> { categoriaActual = "Postres"; cargarProductos(); });
        vista.getBtnCheeseCake().addActionListener(e -> { categoriaActual = "CheeseCake"; cargarProductos(); });

        cargarProductos();
        recargarResumen();
        ocultarCliente(); 
    }


    private boolean esLlevar(String mesa) {
        return mesa != null && mesa.trim().equalsIgnoreCase("LLEVAR");
    }

    private int idxMesa(String mesa) {
        if (mesa == null) return -1;
        mesa = mesa.trim();
        try {
            int n = Integer.parseInt(mesa);
            int idx = n - 1;
            return (idx >= 0 && idx < 5) ? idx : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void estiloMesaNormal(JButton b) {
        b.setBackground(COLOR_MESA_NORMAL);
        b.setForeground(java.awt.Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
    }

    private boolean mesaLlena(String mesa) {
        if (esLlevar(mesa)) return false;
        int idx = idxMesa(mesa);
        if (idx < 0) return false;
        return cedulasPorMesa[idx].size() >= 4;
    }

    private void marcarMesaLlena(String mesa) {
        JButton b = switch (mesa) {
            case "1" -> vista.getBtnTable1();
            case "2" -> vista.getBtnTable2();
            case "3" -> vista.getBtnTable3();
            case "4" -> vista.getBtnTable4();
            case "5" -> vista.getBtnTable5();
            default -> null;
        };
        if (b != null) b.setBackground(COLOR_MESA_LLENA);
    }

    private void refrescarColoresMesas() {
        vista.getBtnTable1().setBackground(mesaLlena("1") ? COLOR_MESA_LLENA : COLOR_MESA_NORMAL);
        vista.getBtnTable2().setBackground(mesaLlena("2") ? COLOR_MESA_LLENA : COLOR_MESA_NORMAL);
        vista.getBtnTable3().setBackground(mesaLlena("3") ? COLOR_MESA_LLENA : COLOR_MESA_NORMAL);
        vista.getBtnTable4().setBackground(mesaLlena("4") ? COLOR_MESA_LLENA : COLOR_MESA_NORMAL);
        vista.getBtnTable5().setBackground(mesaLlena("5") ? COLOR_MESA_LLENA : COLOR_MESA_NORMAL);
        vista.getBtnLlevar().setBackground(COLOR_MESA_NORMAL);

        // si hay mesaActual, remarcarla (pero solo si no es llena)
        if (mesaActual != null) {
            if (esLlevar(mesaActual)) {
                vista.getBtnLlevar().setBackground(COLOR_MESA_SEL);
            } else if (!mesaLlena(mesaActual)) {
                switch (mesaActual.trim()) {
                    case "1" -> vista.getBtnTable1().setBackground(COLOR_MESA_SEL);
                    case "2" -> vista.getBtnTable2().setBackground(COLOR_MESA_SEL);
                    case "3" -> vista.getBtnTable3().setBackground(COLOR_MESA_SEL);
                    case "4" -> vista.getBtnTable4().setBackground(COLOR_MESA_SEL);
                    case "5" -> vista.getBtnTable5().setBackground(COLOR_MESA_SEL);
                }
            }
        }
    }

    private void seleccionarMesa(JButton btn, String mesa) {
        mesa = (mesa == null) ? null : mesa.trim();

        if (mesa == null) return;

        if (mesaLlena(mesa)) {
            marcarMesaLlena(mesa);
            JOptionPane.showMessageDialog(vista, "Mesa " + mesa + " está llena (máx 4 personas).");
            return;
        }

        // ✅ Normalizamos llevar
        mesaActual = esLlevar(mesa) ? "LLEVAR" : mesa;

        refrescarColoresMesas();
        btn.setBackground(COLOR_MESA_SEL);
    }

    private void configurarMesas() {
        botonesMesa.clear();
        botonesMesa.add(vista.getBtnTable1());
        botonesMesa.add(vista.getBtnTable2());
        botonesMesa.add(vista.getBtnTable3());
        botonesMesa.add(vista.getBtnTable4());
        botonesMesa.add(vista.getBtnTable5());
        botonesMesa.add(vista.getBtnLlevar());

        for (JButton b : botonesMesa) estiloMesaNormal(b);

        vista.getBtnTable1().addActionListener(e -> seleccionarMesa(vista.getBtnTable1(), "1"));
        vista.getBtnTable2().addActionListener(e -> seleccionarMesa(vista.getBtnTable2(), "2"));
        vista.getBtnTable3().addActionListener(e -> seleccionarMesa(vista.getBtnTable3(), "3"));
        vista.getBtnTable4().addActionListener(e -> seleccionarMesa(vista.getBtnTable4(), "4"));
        vista.getBtnTable5().addActionListener(e -> seleccionarMesa(vista.getBtnTable5(), "5"));

        // ✅ aunque en diseño el texto sea "6", acá lo tratamos como LLEVAR
        vista.getBtnLlevar().addActionListener(e -> seleccionarMesa(vista.getBtnLlevar(), "LLEVAR"));

        refrescarColoresMesas();
    }

    /* ---------------------- UI config ---------------------- */

    private void configurarProductos() {
        JPanel cont = vista.getPanelProductos();

        // Grid tipo cards
        cont.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JScrollPane sp = vista.getScrollProductos();
        sp.setViewportView(cont);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // ✅ con GridLayout solo una vez
        cont.setLayout(new java.awt.GridLayout(0, 3, 14, 14));
    }

    private void configurarPedidos() {
        JPanel cont = vista.getPanelPedidos();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        cont.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JScrollPane sp = vista.getScrollPedidos();
        sp.setViewportView(cont);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void limpiarPedidoUI() {
        carrito.clear();
        recargarResumen();
        mesaActual = null;

        // ✅ no borramos el rojo de mesas llenas
        refrescarColoresMesas();
    }

    /* ---------------------- productos ---------------------- */

    private void cargarProductos() {
        String txt = vista.getTxtBuscarProducto().getText().trim().toLowerCase();

        JPanel cont = vista.getPanelProductos();
        cont.removeAll();

        List<Product> lista = new ArrayList<>(dao.obtenerTodosLosProductos());

        // sin stock al final
        lista.sort((a, b) -> {
            boolean aOk = a.getCant() > 0;
            boolean bOk = b.getCant() > 0;
            return Boolean.compare(!aOk, !bOk);
        });

        for (Product p : lista) {
            if (!categoriaActual.equalsIgnoreCase("Todos")
                    && !p.getCategory().equalsIgnoreCase(categoriaActual)) {
                continue;
            }

            if (!txt.isEmpty() && !p.getNameProduct().toLowerCase().contains(txt)) {
                continue;
            }

            ProductCard card = new ProductCard(p, () -> agregar(p));

            card.setBackground(p.getCant() <= 0 ? COLOR_MESA_LLENA : new java.awt.Color(80, 200, 120));
            card.setOpaque(true);

            cont.add(card);
        }

        cont.revalidate();
        cont.repaint();
    }

    private void agregar(Product p) {
        int stock = p.getCant();

        if (stock <= 0) {
            JOptionPane.showMessageDialog(vista, "No hay stock disponible de: " + p.getNameProduct());
            return;
        }

        for (ItemPedido it : carrito) {
            if (it.getPro().getIdProduct().equals(p.getIdProduct())) {
                if (it.getCant() >= stock) {
                    JOptionPane.showMessageDialog(vista,
                            "Stock máximo alcanzado (" + stock + ") para: " + p.getNameProduct());
                    return;
                }
                it.setCant(it.getCant() + 1);
                recargarResumen();
                return;
            }
        }

        carrito.add(new ItemPedido(p, 1));
        recargarResumen();
    }

    private void mas(String id) {
        for (ItemPedido it : carrito) {
            if (it.getPro().getIdProduct().equals(id)) {
                int stock = it.getPro().getCant();
                if (it.getCant() >= stock) {
                    JOptionPane.showMessageDialog(vista,
                            "No puede superar el stock (" + stock + ") de: " + it.getPro().getNameProduct());
                    return;
                }
                it.setCant(it.getCant() + 1);
                recargarResumen();
                return;
            }
        }
    }

    private void menos(String id) {
        for (int i = 0; i < carrito.size(); i++) {
            ItemPedido it = carrito.get(i);
            if (it.getPro().getIdProduct().equals(id)) {
                int n = it.getCant() - 1;
                if (n <= 0) carrito.remove(i);
                else it.setCant(n);

                recargarResumen();
                return;
            }
        }
    }

    private void eliminar(String id) {
        carrito.removeIf(it -> it.getPro().getIdProduct().equals(id));
        recargarResumen();
    }

    private int calcularSubtotalColones(List<ItemPedido> items) {
        int subtotal = 0;
        for (ItemPedido it : items) {
            int precioUnit = (int) Math.round(it.getPro().getPrice());
            subtotal += it.getCant() * precioUnit;
        }
        return subtotal;
    }

    private void recargarResumen() {
        JPanel cont = vista.getPanelPedidos();
        cont.removeAll();

        for (ItemPedido it : carrito) {
            Product p = it.getPro();
            int cant = it.getCant();

            cont.add(new OrderItemCard(
                    p,
                    cant,
                    () -> menos(p.getIdProduct()),
                    () -> mas(p.getIdProduct()),
                    () -> eliminar(p.getIdProduct())
            ));
            cont.add(Box.createVerticalStrut(10));
        }

        int subtotal = calcularSubtotalColones(carrito);
        int iva = (int) Math.round(subtotal * 0.13);
        int total = subtotal + iva;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
        vista.getTxtShowSub().setText("₡" + df.format(subtotal));
        vista.getTxtShowIva().setText("₡" + df.format(iva));
        vista.getTxtShowTotal().setText("₡" + df.format(total));

        cont.revalidate();
        cont.repaint();
    }

    /* ---------------------- cliente ---------------------- */

    private void buscarCliente() {
        String cedula = vista.getTxtCed().getText().trim();

        if (cedula.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingrese la cédula del cliente");
            ocultarCliente();
            return;
        }

        Clients c = clientsDAO.buscarPorCedula(cedula);

        if (c == null) {
            JOptionPane.showMessageDialog(vista, "Cliente no encontrado");
            ocultarCliente();
            return;
        }

        clienteActual = c;
        vista.getLblName().setText(c.getName());
        vista.getLblType().setText(c.getType());
        vista.getLblVisits().setText(String.valueOf(c.getVisits()));
        mostrarCliente();
    }

    private void mostrarCliente() {
        vista.getLblName().setVisible(true);
        vista.getLblType().setVisible(true);
        vista.getLblVisits().setVisible(true);

        vista.getLblNameTitle().setVisible(true);
        vista.getLblTypeTitle().setVisible(true);
        vista.getLblVisTitle().setVisible(true);
    }

    private void ocultarCliente() {
        vista.getLblNameTitle().setVisible(false);
        vista.getLblTypeTitle().setVisible(false);
        vista.getLblVisTitle().setVisible(false);

        // ✅ faltaba ocultar y limpiar valores
        vista.getLblName().setVisible(false);
        vista.getLblType().setVisible(false);
        vista.getLblVisits().setVisible(false);

        vista.getLblName().setText("");
        vista.getLblType().setText("");
        vista.getLblVisits().setText("");

        clienteActual = null;
    }

    /* ---------------------- pedido ---------------------- */

    private void realizarPedido() {
        if (clienteActual == null) {
            JOptionPane.showMessageDialog(vista, "Debe buscar un cliente primero");
            return;
        }

        if (mesaActual == null) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar una mesa o Para llevar");
            return;
        }

        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "El pedido está vacío");
            return;
        }

        String fecha = vista.getLblDay().getText();
        String hora = vista.getLblTime().getText();
        int idPedido = pedidosDAO.siguienteId();

        Clients clienteTemp = clienteActual;
        String mesaTemp = mesaActual;
        List<ItemPedido> carritoTemp = new ArrayList<>(carrito);

        int subtotalTemp = calcularSubtotalColones(carritoTemp);
        int ivaTemp = (int) Math.round(subtotalTemp * 0.13);
        int totalTemp = subtotalTemp + ivaTemp;

        // Validación de capacidad mesa
        if (!esLlevar(mesaActual)) {
            int idx = idxMesa(mesaActual);
            if (idx < 0) {
                JOptionPane.showMessageDialog(vista, "Mesa inválida.");
                return;
            }

            String ced = clienteActual.getCedula();
            java.util.List<String> lista = cedulasPorMesa[idx];
            boolean yaEsta = lista.contains(ced);

            if (!yaEsta && lista.size() >= 4) {
                JOptionPane.showMessageDialog(vista, "Mesa " + mesaActual + " está llena (máx 4 personas).");
                marcarMesaLlena(mesaActual);
                return;
            }

            if (!yaEsta) {
                lista.add(ced);
                if (lista.size() >= 4) marcarMesaLlena(mesaActual);
            }
        }

        // Construir ITEMS para el txt: id|cant|precio|total ; ...
        StringBuilder items = new StringBuilder();
        int subtotal = 0;

        for (int i = 0; i < carrito.size(); i++) {
            ItemPedido it = carrito.get(i);
            Product p = it.getPro();

            String idProd = p.getIdProduct();
            int cant = it.getCant();

            int precioUnit = (int) Math.round(p.getPrice());
            int totalProd = cant * precioUnit;

            subtotal += totalProd;

            items.append(idProd).append("|")
                    .append(cant).append("|")
                    .append(precioUnit).append("|")
                    .append(totalProd);

            if (i < carrito.size() - 1) items.append(";");
        }

        int iva = (int) Math.round(subtotal * 0.13);
        int total = subtotal + iva;

        String linea = idPedido + "," + fecha + "," + hora + "," + mesaActual + "," + clienteActual.getCedula() + ","
                + items + ","
                + subtotal + "," + iva + "," + total;

        pedidosDAO.guardarLinea(linea);

        // Descontar stock
        for (ItemPedido it : carrito) {
            Product p = it.getPro();
            p.setCant(p.getCant() - it.getCant());
            dao.actualizarProducto(p);
        }

        carrito.clear();
        recargarResumen();
        mesaActual = null;
        refrescarColoresMesas();

        JOptionPane.showMessageDialog(vista, "Pedido realizado con éxito\nID Pedido: " + idPedido);

        int respuesta = JOptionPane.showConfirmDialog(vista,
                "¿Desea generar la factura para este pedido?",
                "Facturar",
                JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            abrirFacturacion(idPedido, clienteTemp, mesaTemp, carritoTemp, subtotalTemp, ivaTemp, totalTemp);
        }
    }

    /* ---------------------- facturación ---------------------- */

    private void abrirFacturacion(int idPedido, Clients cliente, String mesa,
                                 List<ItemPedido> items, int subtotal, int iva, int total) {

        java.awt.Container parent = vista.getParent();
        while (parent != null && !(parent instanceof Vista.Principal)) {
            parent = parent.getParent();
        }

        if (!(parent instanceof Vista.Principal)) return;

        Vista.Principal principal = (Vista.Principal) parent;

        // Abre panel facturación
        Main.main.controllerPrincipal.activarPanelExterno("facturacion");

        // Buscar el panel de facturación
        java.awt.Component[] componentes = principal.getPanelContenido().getComponents();
        Vista.GestionFacturacion panelFact = null;

        for (java.awt.Component comp : componentes) {
            if (comp instanceof Vista.GestionFacturacion) {
                panelFact = (Vista.GestionFacturacion) comp;
                break;
            }
        }

        if (panelFact == null) return;

       
        ControllerFacturacion controllerFact = ControllerFacturacion.getFor(panelFact);
        if (controllerFact == null) {
            // si por alguna razón la vista no lo creó, lo creamos acá
            controllerFact = new ControllerFacturacion(panelFact);
        }

        
        controllerFact.cargarPedidoPorId(idPedido);
    }
}