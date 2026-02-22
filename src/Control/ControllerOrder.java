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

    private orders vista;

    // DAO
    private java.util.List<String>[] cedulasPorMesa = new java.util.ArrayList[5];

    private ClientsDAO clientsDAO;
    private productosDAO dao;
    private final List<ItemPedido> carrito = new ArrayList<>();
    private String categoriaActual = "Todos";
    private Clients clienteActual = null;
    private pedidosDAO pedidosDAO = new pedidosDAO();
    private String mesaActual = null;
    private java.awt.Color COLOR_MESA_NORMAL = new java.awt.Color(153, 153, 153); // gris
    private java.awt.Color COLOR_MESA_SEL = new java.awt.Color(0, 140, 255);   // azul
    private java.util.List<JButton> botonesMesa = new java.util.ArrayList<>();

    public ControllerOrder(orders vista) {

        for (int i = 0; i < cedulasPorMesa.length; i++) {
            cedulasPorMesa[i] = new java.util.ArrayList<>();
        }
        this.vista = vista;
        this.clientsDAO = new ClientsDAO();
        this.dao = productosDAO.getInstancia();
        this.vista.getBtnSearch().addActionListener(e -> buscarCliente());
        this.vista.getBtnPedir().addActionListener(e -> realizarPedido());
        vista.getTxtBuscarProducto().setForeground(java.awt.Color.BLACK);
        vista.getBtnClean().addActionListener(e -> {
            carrito.clear();
            recargarResumen();
            mesaActual = null;

            // deja las llenas rojas y las otras grises
            vista.getBtnTable1().setBackground(mesaLlena("1") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
            vista.getBtnTable2().setBackground(mesaLlena("2") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
            vista.getBtnTable3().setBackground(mesaLlena("3") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
            vista.getBtnTable4().setBackground(mesaLlena("4") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
            vista.getBtnTable5().setBackground(mesaLlena("5") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
            vista.getBtnLlevar().setBackground(COLOR_MESA_NORMAL);
        });

        vista.getTxtBuscarProducto().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                cargarProductos();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                cargarProductos();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                cargarProductos();
            }
        });

        configurarMesas();
        configurarProductos();
        configurarPedidos();
        // filtros
        vista.getBtnAll().addActionListener(e -> {
            categoriaActual = "Todos";
            cargarProductos();
        });
        vista.getBtnCafe().addActionListener(e -> {
            categoriaActual = "Cafe";
            cargarProductos();
        });
        vista.getBtnDrinks().addActionListener(e -> {
            categoriaActual = "Lacteos";
            cargarProductos();
        });
        vista.getBtnPostres().addActionListener(e -> {
            categoriaActual = "Postres";
            cargarProductos();
        });

        vista.getBtnCheeseCake().addActionListener(e -> {
            categoriaActual = "CheeseCake";
            cargarProductos();
        });

        cargarProductos();
        recargarResumen();
    }

    private int idxMesa(String mesa) {
        return Integer.parseInt(mesa) - 1;
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

    private void seleccionarMesa(JButton btn, String mesa) {
        // Si la mesa está llena → no se selecciona
        if (mesaLlena(mesa)) {
            marcarMesaLlena(mesa);
            JOptionPane.showMessageDialog(vista,
                    "Mesa " + mesa + " está llena (máx 4 personas).");
            return;
        }

        mesaActual = mesa;

        // Resetear colores, respetando mesas llenas
        vista.getBtnTable1().setBackground(mesaLlena("1") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
        vista.getBtnTable2().setBackground(mesaLlena("2") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
        vista.getBtnTable3().setBackground(mesaLlena("3") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
        vista.getBtnTable4().setBackground(mesaLlena("4") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
        vista.getBtnTable5().setBackground(mesaLlena("5") ? new java.awt.Color(220, 60, 60) : COLOR_MESA_NORMAL);
        vista.getBtnLlevar().setBackground(COLOR_MESA_NORMAL);

        // Mesa seleccionada → azul
        btn.setBackground(COLOR_MESA_SEL);
    }

    private void configurarMesas() {

        botonesMesa.clear();
        botonesMesa.add(vista.getBtnTable1());
        botonesMesa.add(vista.getBtnTable2());
        botonesMesa.add(vista.getBtnTable3());
        botonesMesa.add(vista.getBtnTable4());
        botonesMesa.add(vista.getBtnTable5());
        botonesMesa.add(vista.getBtnLlevar()); // “para llevar”

        for (JButton b : botonesMesa) {
            estiloMesaNormal(b);
        }

        vista.getBtnTable1().addActionListener(e -> seleccionarMesa(vista.getBtnTable1(), "1"));
        vista.getBtnTable2().addActionListener(e -> seleccionarMesa(vista.getBtnTable2(), "2"));
        vista.getBtnTable3().addActionListener(e -> seleccionarMesa(vista.getBtnTable3(), "3"));
        vista.getBtnTable4().addActionListener(e -> seleccionarMesa(vista.getBtnTable4(), "4"));
        vista.getBtnTable5().addActionListener(e -> seleccionarMesa(vista.getBtnTable5(), "5"));
        vista.getBtnLlevar().addActionListener(e -> seleccionarMesa(vista.getBtnLlevar(), "LLEVAR"));
    }

    private boolean mesaLlena(String mesa) {
        if ("LLEVAR".equals(mesa)) {
            return false; // llevar no se llena
        }
        int idx = idxMesa(mesa);
        return cedulasPorMesa[idx].size() >= 4;
    }

    private void realizarPedido() {

        // 1. Validaciones
        if (clienteActual == null) {
            JOptionPane.showMessageDialog(vista, "Debe buscar un cliente primero");
            return;
        }

        if (mesaActual == null) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar una mesa o Para llevar");
            return;
        }

        // Si es mesa (no llevar), aplicar regla de 4 personas
        if (!"LLEVAR".equals(mesaActual)) {

            int idx = idxMesa(mesaActual);
            String ced = clienteActual.getCedula();

            java.util.List<String> lista = cedulasPorMesa[idx];

            boolean yaEsta = lista.contains(ced);

            // Si NO está y ya hay 4, no dejar
            if (!yaEsta && lista.size() >= 4) {
                JOptionPane.showMessageDialog(vista, "Mesa " + mesaActual + " está llena (máx 4 personas).");
                marcarMesaLlena(mesaActual);
                return;
            }

            // Si NO está y hay espacio, entonces agrega la cédula (sube pax)
            if (!yaEsta) {
                lista.add(ced);
                if (lista.size() >= 4) {
                    marcarMesaLlena(mesaActual); // se vuelve roja si ya llegó a 4
                }
            }
        }

        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "El pedido está vacío");
            return;
        }

        // 2. Fecha y hora (labels)
        String fecha = vista.getLblDay().getText();
        String hora = vista.getLblTime().getText();

        // 3. ID del pedido (ejemplo simple)
        int idPedido = pedidosDAO.siguienteId(); // o como lo tengas

        StringBuilder items = new StringBuilder();
        double subtotal = 0;

        for (int i = 0; i < carrito.size(); i++) {
            ItemPedido it = carrito.get(i);
            Product p = it.getPro();

            String idProd = p.getIdProduct();
            int cant = it.getCant();
            int precioUnit = (int) p.getPrice();
            int totalProd = cant * precioUnit;

            subtotal += totalProd;

            items.append(idProd).append("|")
                    .append(cant).append("|")
                    .append(precioUnit).append("|")
                    .append(totalProd);

            if (i < carrito.size() - 1) {
                items.append(";");
            }
        }

        double iva = subtotal * 0.13;
        double total = subtotal + iva;

        // 4. Línea final del pedido
        String linea = idPedido + "," + fecha + "," + hora + "," + mesaActual + "," + clienteActual.getCedula() + ","
                + items.toString() + ","
                + (int) subtotal + "," + (int) iva + "," + (int) total;

        // 5. Guardar en TXT
        pedidosDAO.guardarLinea(linea);

        // 6. Descontar stock
        for (ItemPedido it : carrito) {
            Product p = it.getPro();
            p.setCant(p.getCant() - it.getCant());
            dao.actualizarProducto(p);
        }

        // 7. Limpiar
        carrito.clear();
        recargarResumen();
        mesaActual = null;
        for (JButton b : botonesMesa) {
            b.setBackground(COLOR_MESA_NORMAL);
        }
        JOptionPane.showMessageDialog(vista, "Pedido realizado con éxito");
    }

    private void marcarMesaLlena(String mesa) {
        JButton b = null;

        switch (mesa) {
            case "1":
                b = vista.getBtnTable1();
                break;
            case "2":
                b = vista.getBtnTable2();
                break;
            case "3":
                b = vista.getBtnTable3();
                break;
            case "4":
                b = vista.getBtnTable4();
                break;
            case "5":
                b = vista.getBtnTable5();
                break;
            default:
                return; // LLEVAR no aplica
        }

        b.setBackground(new java.awt.Color(220, 60, 60)); // rojo
    }

    private void configurarProductos() {
        JPanel cont = vista.getPanelProductos();
        for (ItemPedido it : carrito) {
            if (it.getCant() > it.getPro().getCant()) {
                JOptionPane.showMessageDialog(vista,
                        "El producto " + it.getPro().getNameProduct()
                        + " supera el stock. Stock: " + it.getPro().getCant());
                return;
            }
        }
        // Grid tipo “cards” que bajan
        cont.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 14, 14));
        cont.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JScrollPane sp = vista.getScrollProductos();
        sp.setViewportView(cont);

        // solo vertical
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        //CLAVE: que el panel interno use el ancho visible del scroll
        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = sp.getViewport().getWidth();
                int h = cont.getPreferredSize().height;
                cont.setPreferredSize(new java.awt.Dimension(w, h));
                cont.revalidate();
            }
        });
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

    private void cargarProductos() {
        String txt = vista.getTxtBuscarProducto().getText().trim().toLowerCase();

        JPanel cont = vista.getPanelProductos();
        cont.removeAll();

        for (Product p : dao.obtenerTodosLosProductos()) {

            if (!categoriaActual.equalsIgnoreCase("Todos")
                    && !p.getCategory().equalsIgnoreCase(categoriaActual)) {
                continue;
            }

            if (!txt.isEmpty() && !p.getNameProduct().toLowerCase().contains(txt)) {
                continue;
            }

            cont.add(new ProductCard(p, () -> agregar(p)));
        }

        cont.revalidate();
        java.awt.Dimension pref = cont.getPreferredSize();
        int w = vista.getScrollProductos().getViewport().getWidth();
        cont.setPreferredSize(new java.awt.Dimension(w, pref.height));
        cont.revalidate();
        cont.repaint();

    }

    private void agregar(Product p) {

        int stock = p.getCant();

        // No hay stock
        if (stock <= 0) {
            JOptionPane.showMessageDialog(vista, "No hay stock disponible de: " + p.getNameProduct());
            return;
        }

        // Ya existe en carrito -> sumar, pero sin pasar stock
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

        // No existe en carrito → agregar con 1
        carrito.add(new ItemPedido(p, 1));
        recargarResumen();
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
        double subtotal = 0;
        for (ItemPedido it : carrito) {
            subtotal += it.getCant() * it.getPro().getPrice();
        }
        double iva = subtotal * 0.13;
        double total = subtotal + iva;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

        vista.getTxtShowSub().setText("₡" + df.format(subtotal));
        vista.getTxtShowIva().setText("₡" + df.format(iva));
        vista.getTxtShowTotal().setText("₡" + df.format(total));
        cont.revalidate();
        cont.repaint();
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
                if (n <= 0) {
                    carrito.remove(i);
                } else {
                    it.setCant(n);
                }
                recargarResumen();
                return;
            }
        }
    }

    private void eliminar(String id) {
        carrito.removeIf(it -> it.getPro().getIdProduct().equals(id));
        recargarResumen();
    }

    //CLIENTE
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
        clienteActual = null;
    }

}
