/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import Modelo.Products;
import Modelo.productosDAO;
import Vista.GestionProductos;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jefferson granados
 */
public class controladorProductos {

    private GestionProductos vista;
    private productosDAO dao;

    public controladorProductos(GestionProductos vista) {
        this.vista = vista;
        this.dao = new productosDAO();

        // Registrar eventos
        this.vista.getBtnGuardar().addActionListener(this::guardarProducto);
        this.vista.getBtnCancelar().addActionListener(e -> vista.dispose());

     
    }

    private void guardarProducto(ActionEvent e) {
        try {
            // 1. Validar campos obligatorios
            if (vista.getTxtIDProduct().getText().trim().isEmpty()
                    || vista.getTxtNombre().getText().trim().isEmpty()
                    || vista.getTxtPrecio().getText().trim().isEmpty()
                    || vista.getTxtCantidad().getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(vista,
                        "Complete todos los campos obligatorios",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Obtener datos de la vista
            String id = vista.getTxtIDProduct().getText().trim();
            String nombre = vista.getTxtNombre().getText().trim();
            String categoria = vista.getComboCategoria().getSelectedItem().toString();
            double precio = Double.parseDouble(vista.getTxtPrecio().getText().trim());
            int cantidad = Integer.parseInt(vista.getTxtCantidad().getText().trim());
            String descripcion = vista.getTxtDescripcion().getText().trim();

            // 3. Crear objeto Producto
            Products nuevo = new Products(id, nombre, categoria, precio, cantidad, "Activo");

            // 4. Guardar usando DAO
            boolean guardado = dao.insertarProducto(nuevo);

            if (guardado) {

                JOptionPane.showMessageDialog(vista,
                        "Producto guardado correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);

                // Limpiar formulario
                vista.limpiarFormulario();

            } else {
                JOptionPane.showMessageDialog(vista,
                        "Ya existe un producto con ese ID",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista,
                    "Precio y cantidad deben ser números válidos",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
