package Control;

import Modelo.productosDAO;
import Vista.GestionProductos;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Controlador para el panel Products que contiene la tabla de productos
 *
 * @author Jefferson granados
 */
public class controladorProductosPanel {

    private Vista.Products vista;
    private Modelo.productosDAO dao;

    //REFERENCIA ESTÁTICA (SINGLETON)
    private static controladorProductosPanel instancia;

    public controladorProductosPanel(Vista.Products vista) {
        this.vista = vista;
        this.dao = Modelo.productosDAO.getInstancia();

        //GUARDAR LA INSTANCIA
        instancia = this;

        // Registrar eventos 
        this.vista.getBtnNuevoProducto().addActionListener(this::nuevoProducto);
        this.vista.getBtnModificarProducto().addActionListener(this::modificarProducto);
        this.vista.getBtnEliminarProducto().addActionListener(this::eliminarProducto);

        // Cargar la tabla al iniciar
        cargarProductosEnTabla();
    }

    //MÉTODO ESTÁTICO PARA OBTENER LA INSTANCIA
    public static controladorProductosPanel getInstancia() {
        return instancia;

    }

    /**
     * Abre el formulario para crear un nuevo producto
     */
    private void nuevoProducto(ActionEvent e) {
        GestionProductos gp = new GestionProductos();
        new controladorProductos(gp);

        gp.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // Recargar tabla cuando se cierre el formulario
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

    /**
     * Abre el formulario con los datos del producto seleccionado para modificar
     */
    private void modificarProducto(ActionEvent e) {
        int fila = vista.getTableProductos().getSelectedRow();

        // Validar que haya una fila seleccionada
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista,
                    "Por favor, seleccione un producto de la tabla para modificar",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener ID del producto seleccionado
        DefaultTableModel modelo = (DefaultTableModel) vista.getTableProductos().getModel();
        String id = modelo.getValueAt(fila, 0).toString();

        // Buscar el producto en el DAO
        Modelo.Products producto = dao.buscarProductoPorId(id);

        if (producto != null) {
            // Abrir formulario con los datos cargados
            GestionProductos gp = new GestionProductos();

            gp.getTxtIDProduct().setText(producto.getIdProduct());
            gp.getTxtNombre().setText(producto.getNameProduct());
            gp.getComboCategoria().setSelectedItem(producto.getCategory());
            gp.getTxtPrecio().setText(String.valueOf(producto.getPrice()));
            gp.getTxtCantidad().setText(String.valueOf(producto.getCant()));
            // La descripción no está en la tabla, se deja vacía

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

    /**
     * Elimina el producto seleccionado después de confirmar
     */
    private void eliminarProducto(ActionEvent e) {
        int fila = vista.getTableProductos().getSelectedRow();

        // Validar que haya una fila seleccionada
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista,
                    "Por favor, seleccione un producto de la tabla para eliminar",
                    "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar eliminación
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

                cargarProductosEnTabla(); // Recargar tabla
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

    /**
     * Carga todos los productos desde el DAO a la tabla
     */
    private void cargarProductosEnTabla() {
        DefaultTableModel modelo = (DefaultTableModel) vista.getTableProductos().getModel();
        modelo.setRowCount(0); // Limpiar tabla

        for (Modelo.Products p : dao.obtenerTodosLosProductos()) {
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
