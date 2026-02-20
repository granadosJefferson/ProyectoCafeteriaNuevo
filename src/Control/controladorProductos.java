package Control;

import Modelo.Product;
import Modelo.productosDAO;
import Vista.GestionProductos;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

/**
 *
 * @author Jefferson granados
 */
public class controladorProductos {

    private GestionProductos vista;
    private productosDAO dao;

    public controladorProductos(GestionProductos vista) {
        this.vista = vista;
        this.dao = productosDAO.getInstancia();

        // Registrar eventos
        this.vista.getBtnGuardar().addActionListener(this::guardarProducto);
        this.vista.getBtnCancelar().addActionListener(e -> vista.dispose());

    }

    
    private void guardarProducto(ActionEvent e) {
        
        try {
            // Validar campos obligatorios
            if (vista.getTxtIDProduct().getText().trim().isEmpty()
                    || vista.getTxtNombre().getText().trim().isEmpty()
                    || vista.getTxtPrecio().getText().trim().isEmpty()
                    || vista.getTxtCantidad().getText().trim().isEmpty()
                    || vista.getTxtImagen().getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(vista,
                        "Complete todos los campos obligatorios",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener datos de la vista
            String id = vista.getTxtIDProduct().getText().trim();
            String nombre = vista.getTxtNombre().getText().trim();
            String categoria = vista.getComboCategoria().getSelectedItem().toString();
            double precio = Double.parseDouble(vista.getTxtPrecio().getText().trim());
            int cantidad = Integer.parseInt(vista.getTxtCantidad().getText().trim());
            //String descripcion = vista.getTxtDescripcion().getText().trim();
            String imagen = vista.getTxtImagen().getText().trim();
            String estado = (cantidad > 0) ? "Activo" : "Inactivo";

            Product nuevo = new Product(id, nombre, categoria, precio, cantidad, estado, imagen);
            // Verificar si el producto ya existe
            Product existente = dao.buscarProductoPorId(id);

            if (existente != null) {
                // Producto existe → preguntar si desea modificar
                int confirmacion = JOptionPane.showConfirmDialog(vista,
                        "Ya existe un producto con ID: " + id + "\n"
                        + "Producto actual: " + existente.getNameProduct() + "\n"
                        + "¿Desea modificar este producto con los nuevos datos?",
                        "Producto existente",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    // Actualizar producto
                    boolean actualizado = dao.actualizarProducto(nuevo);

                    if (actualizado) {
                        JOptionPane.showMessageDialog(vista,
                                "Producto modificado correctamente",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);

                        vista.limpiarFormulario();
                        vista.dispose();

                    } else {

                        JOptionPane.showMessageDialog(vista,
                                "No se pudo modificar el producto",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            } else {
       
                boolean guardado = dao.insertarProducto(nuevo);

                if (guardado) {
                    JOptionPane.showMessageDialog(vista,
                            "Producto guardado correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);

                    vista.limpiarFormulario();
                    vista.dispose();

                } else {
                    JOptionPane.showMessageDialog(vista,
                            "Error al guardar el producto",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista,
                    "Precio y cantidad deben ser números válidos",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
}

