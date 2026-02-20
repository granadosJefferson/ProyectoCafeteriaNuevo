package Control;

import Modelo.productosDAO;
import Vista.GestionProductos;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import Vista.Products;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class controladorProductosPanel {

    private final Products vista;
    private final productosDAO dao;
    private final ControllerInventario invCtrl;

    private boolean cargandoTabla = false;
    private boolean cambioProgramatico = false;

    public controladorProductosPanel(Products vista, ControllerInventario invCtrl) {
        this.vista = vista;
        this.dao = productosDAO.getInstancia();
        this.invCtrl = invCtrl;

        this.vista.getBtnNuevoProducto().addActionListener(this::nuevoProducto);
        this.vista.getBtnModificarProducto().addActionListener(this::modificarProducto);
        this.vista.getBtnEliminarProducto().addActionListener(this::eliminarProducto);

        cargandoTabla = true;

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Nombre", "Categoría", "Precio", "Cantidad", "Estado", "Imagen"}
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 6) ? Icon.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        vista.getTableProductos().setModel(modelo);
        vista.getTableProductos().setFont(new Font("Segoe UI", Font.BOLD, 14));
        vista.getTableProductos().setRowHeight(80);

        cargarProductosEnTabla();

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        centrado.setFont(new Font("Segoe UI", Font.BOLD, 14));

        for (int i = 0; i <= 5; i++) {
            vista.getTableProductos().getColumnModel().getColumn(i).setCellRenderer(centrado);
        }

        JTableHeader header = vista.getTableProductos().getTableHeader();
        header.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer
                = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        cambioProgramatico = true;
        vista.getTxtProductoSeleccionado().setText("");
        cambioProgramatico = false;

        cargandoTabla = false;

        vista.getTxtProductoSeleccionado().getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarPorId();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarPorId();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarPorId();
            }
        }
        );
    }

   
    private void nuevoProducto(ActionEvent e) {
        GestionProductos gp = new GestionProductos();
        new controladorProductos(gp);

        gp.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        gp.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                recargarSinFiltro();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                recargarSinFiltro();
            }
        });

        gp.setVisible(true);
    }

    private void modificarProducto(ActionEvent e) {
        int fila = vista.getTableProductos().getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(vista,
                    "Por favor, seleccione un producto de la tabla para modificar",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel modelo = (DefaultTableModel) vista.getTableProductos().getModel();
        String id = modelo.getValueAt(fila, 0).toString();

        Modelo.Product producto = dao.buscarProductoPorId(id);

        if (producto != null) {
            GestionProductos gp = new GestionProductos();

            gp.getTxtIDProduct().setText(producto.getIdProduct());
            gp.getTxtNombre().setText(producto.getNameProduct());
            gp.getComboCategoria().setSelectedItem(producto.getCategory());
            gp.getTxtPrecio().setText(String.valueOf(producto.getPrice()));
            gp.getTxtCantidad().setText(String.valueOf(producto.getCant()));
            gp.getTxtImagen().setText(producto.getImage());

            URL imgUrl = gp.getClass().getResource("/img/" + producto.getImage());
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image img = icon.getImage().getScaledInstance(135, 135, Image.SCALE_SMOOTH);
                gp.getLblPreviewImg().setIcon(new ImageIcon(img));
            } else {
                gp.getLblPreviewImg().setIcon(null);
            }

            new controladorProductos(gp);

            gp.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            gp.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    recargarSinFiltro();
                }

                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    recargarSinFiltro();
                }
            });
            gp.setVisible(true);
        }
    }

    private void eliminarProducto(ActionEvent e) {
        int fila = vista.getTableProductos().getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(vista,
                    "Por favor, seleccione un producto de la tabla para eliminar",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(vista,
                "¿Está seguro que desea eliminar este producto?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            DefaultTableModel modelo = (DefaultTableModel) vista.getTableProductos().getModel();
            String id = modelo.getValueAt(fila, 0).toString();
            String nombre = modelo.getValueAt(fila, 1).toString();

            boolean eliminado = dao.eliminarProducto(id);

            if (eliminado) {
                JOptionPane.showMessageDialog(vista,
                        "Producto '" + nombre + "' eliminado correctamente",
                        "Eliminado",
                        JOptionPane.INFORMATION_MESSAGE);

                recargarSinFiltro();
            } else {
                JOptionPane.showMessageDialog(vista,
                        "No se pudo eliminar el producto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void recargarTabla() {
        cargarProductosEnTabla();
    }

    private void cargarProductosEnTabla() {
        cargandoTabla = true;

        DefaultTableModel modelo = (DefaultTableModel) vista.getTableProductos().getModel();
        modelo.setRowCount(0);

        for (Modelo.Product p : dao.obtenerTodosLosProductos()) {
            modelo.addRow(new Object[]{
                p.getIdProduct(),
                p.getNameProduct(),
                p.getCategory(),
                p.getPrice(),
                p.getCant(),
                p.getStatus(),
                cargarIcono(p.getImage())
            });
        }

        modelo.fireTableDataChanged();
        vista.getTableProductos().clearSelection();
        vista.getTableProductos().revalidate();
        vista.getTableProductos().repaint();

        cargandoTabla = false;

    }

    private Icon cargarIcono(String nombreImg) {
        if (nombreImg == null || nombreImg.isBlank()) {
            return null;
        }

        URL url = getClass().getResource("/img/" + nombreImg);
        if (url == null) {
            return null;
        }

        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(80, 80, Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }

    private void filtrarPorId() {

        if (cargandoTabla) {
            return;
        }
        if (cambioProgramatico) {
            return;
        }

        String texto = vista.getTxtProductoSeleccionado().getText().trim();
        DefaultTableModel modelo
                = (DefaultTableModel) vista.getTableProductos().getModel();

        modelo.setRowCount(0);

        if (texto.isEmpty()) {
            for (Modelo.Product p : dao.obtenerTodosLosProductos()) {
                modelo.addRow(new Object[]{
                    p.getIdProduct(),
                    p.getNameProduct(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getCant(),
                    p.getStatus(),
                    cargarIcono(p.getImage())
                });
            }
            return;
        }

        for (Modelo.Product p : dao.obtenerTodosLosProductos()) {
            if (p.getIdProduct().contains(texto)) {
                modelo.addRow(new Object[]{
                    p.getIdProduct(),
                    p.getNameProduct(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getCant(),
                    p.getStatus(),
                    cargarIcono(p.getImage())
                });
            }
        }
    }

    private void recargarSinFiltro() {

        cargandoTabla = true;
        cambioProgramatico = true;
        vista.getTxtProductoSeleccionado().setText("");
        cambioProgramatico = false;
        cargandoTabla = false;

        cargarProductosEnTabla();

        if (invCtrl != null) {
            invCtrl.recargarInventario();
        }

    }

    public void setFiltroProgramatico(String txt) {
        cambioProgramatico = true;
        vista.getTxtProductoSeleccionado().setText(txt);
        cambioProgramatico = false;
    }

}
