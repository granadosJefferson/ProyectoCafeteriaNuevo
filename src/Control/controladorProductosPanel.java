package Control;

import Modelo.productosDAO;
import Vista.GestionProductos;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import Vista.Products;

public class controladorProductosPanel {

    private final Products vista;
    private final productosDAO dao;

    // REFERENCIA ESTÁTICA (SINGLETON)
    private static controladorProductosPanel instancia;

    public controladorProductosPanel(Products vista) {
        this.vista = vista;
        this.dao = productosDAO.getInstancia();

        // GUARDAR LA INSTANCIA
        instancia = this;

        // Registrar eventos
        this.vista.getBtnNuevoProducto().addActionListener(this::nuevoProducto);
        this.vista.getBtnModificarProducto().addActionListener(this::modificarProducto);
        this.vista.getBtnEliminarProducto().addActionListener(this::eliminarProducto);

        // Cargar la tabla al iniciar
        cargarProductosEnTabla();
    }

    public static controladorProductosPanel getInstancia() {
        return instancia;
    }

    private void nuevoProducto(ActionEvent e) {
        GestionProductos gp = new GestionProductos();
        new controladorProductos(gp);

        gp.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        gp.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                cargarProductosEnTabla();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cargarProductosEnTabla();
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

            new controladorProductos(gp);

            gp.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            gp.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    cargarProductosEnTabla();
                }

                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cargarProductosEnTabla();
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

                cargarProductosEnTabla();
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
        DefaultTableModel modelo = (DefaultTableModel) vista.getTableProductos().getModel();
        modelo.setRowCount(0);

        for (Modelo.Product p : dao.obtenerTodosLosProductos()) {
            modelo.addRow(new Object[]{
                p.getIdProduct(),
                p.getNameProduct(),
                p.getCategory(),
                p.getPrice(),
                p.getCant(),
                p.getStatus()
            });
        }

        modelo.fireTableDataChanged();
        vista.getTableProductos().revalidate();
        vista.getTableProductos().repaint();
    }
}
