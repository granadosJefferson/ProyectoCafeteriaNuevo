package Control;

import Modelo.Product;
import Modelo.productosDAO;
import Vista.GestionProductos;
import Vista.Mensajes;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

/**
 *
 * @author Jefferson Granados
 * En esta clase se controla la lógica de guardado/modificación de productos.
 * Se validan campos, se consulta el DAO y se muestran mensajes usando la clase Mensajes.
 */
public class controladorProductos {

    private final GestionProductos vista;
    private final productosDAO dao;
    private final Mensajes mensajes;

    /**
     * Constructor: recibe la vista, obtiene la instancia del DAO y registra eventos.
     *
     * @param vista formulario GestionProductos
     */
    public controladorProductos(GestionProductos vista) {
        this.vista = vista;
        this.dao = productosDAO.getInstancia();
        this.mensajes = new Mensajes();

        // Registrar eventos
        this.vista.getBtnGuardar().addActionListener(this::guardarProducto);
        this.vista.getBtnCancelar().addActionListener(e -> vista.dispose());

        // Filtrar imágenes cuando cambia la categoría
        this.vista.getComboCategoria().addActionListener(ev -> {
            String cat = this.vista.getComboCategoria().getSelectedItem().toString();
            this.vista.filtrarImagenesPorCategoria(cat);
        });
    }

    /**
     * Guarda un producto nuevo o modifica uno existente.
     * Valida campos obligatorios, convierte tipos y usa el DAO para persistencia.
     *
     * @param e evento del botón Guardar
     */
    private void guardarProducto(ActionEvent e) {

        try {

            dao.recargarDesdeArchivo();

            // Validar campos obligatorios
            if (vista.getTxtIDProduct().getText().trim().isEmpty()
                    || vista.getTxtNombre().getText().trim().isEmpty()
                    || vista.getTxtPrecio().getText().trim().isEmpty()
                    || vista.getTxtCantidad().getText().trim().isEmpty()
                    || vista.getTxtImagen().getText().trim().isEmpty()) {

                mensajes.message("Complete todos los campos obligatorios");
                return;
            }

            // Obtener datos de la vista
            String id = vista.getTxtIDProduct().getText().trim();
            String nombre = vista.getTxtNombre().getText().trim();
            String categoria = vista.getComboCategoria().getSelectedItem().toString();
            double precio = Double.parseDouble(vista.getTxtPrecio().getText().trim());
            int cantidad = Integer.parseInt(vista.getTxtCantidad().getText().trim());
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
                        mensajes.message("Producto modificado correctamente");
                        vista.limpiarFormulario();
                        vista.dispose();
                    } else {
                        mensajes.message("No se pudo modificar el producto");
                    }
                }

            } else {

                boolean guardado = dao.insertarProducto(nuevo);

                if (guardado) {
                    mensajes.message("Producto guardado correctamente");
                    vista.limpiarFormulario();
                    vista.dispose();
                } else {
                    mensajes.message("Error al guardar el producto");
                }
            }

        } catch (NumberFormatException ex) {
            mensajes.message("Precio y cantidad deben ser números válidos");
        }
    }

}