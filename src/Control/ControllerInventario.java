/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import Modelo.Product;
import Modelo.productosDAO;
import Vista.GestionInventario;
import Vista.panelProduct;
import javax.swing.SwingUtilities;

/**
 *
 * @author Personal
 */
public class ControllerInventario {

    private GestionInventario vista;
    private productosDAO dao;

    public ControllerInventario(GestionInventario vista) {

        this.vista = vista;
        this.dao = productosDAO.getInstancia();
        
         this.dao.addStockChangeCallback(() -> {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Stock actualizado - Recargando inventario");
            filtrarPorCategoria();
        });
    });

        this.vista.getBtnAdd().addActionListener(e -> agregarProducto());
        this.vista.getCmbCategory().addActionListener(e -> filtrarPorCategoria());

       

        recargarInventario();

    }

    private void agregarProducto() {
        Vista.GestionProductos gp = new Vista.GestionProductos();

        new Control.controladorProductos(gp);

        gp.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        gp.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                filtrarPorCategoria();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                filtrarPorCategoria();
            }
        });

        gp.setVisible(true);
    }

    private void filtrarPorCategoria() {
        String cat = vista.getCmbCategory().getSelectedItem().toString();
        recargarInventario(cat);
    }

    public void recargarInventario() {
        recargarInventario("Todos los productos");
    }

    public void recargarInventario(String cat) {

        javax.swing.JPanel cont = vista.getJpanelProducts();
        cont.removeAll();

        for (Product p : dao.obtenerTodosLosProductos()) {

            if (!cat.equalsIgnoreCase("Todos los productos")) {
                if (!p.getCategory().equalsIgnoreCase(cat)) {
                    continue;
                }
            }

            cont.add(new panelProduct(p));
        }

        cont.revalidate();
        cont.repaint();
    }

}
