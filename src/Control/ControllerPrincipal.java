package Control;

import Vista.Principal;
import java.awt.CardLayout;
import javax.swing.JButton;

public class ControllerPrincipal {

    private Principal vista;

    public ControllerPrincipal(Principal vista) {
        this.vista = vista;

        conectar(vista.getBtnProducts(), "products");
        conectar(vista.getBtnClients(), "clients");
        conectar(vista.getBtnInventario(), "inventario");
        conectar(vista.getBtnPedidos(), "order");
        conectar(vista.getBtnMesas(), "mesas");
        conectar(vista.getBtnReportes(), "reportes");
        conectar(vista.getBtnStart(), "vacio");
        conectar(vista.getBtnFacturación(), "facturacion");
    }

    private void conectar(JButton boton, String panel) {
            boton.addActionListener(e -> activarBoton(boton, panel));
    }

    private void activarBoton(JButton active, String panel) {

        desactive(vista.getBtnProducts());
        desactive(vista.getBtnClients());
        desactive(vista.getBtnInventario());
        desactive(vista.getBtnPedidos());
        desactive(vista.getBtnMesas());
        desactive(vista.getBtnFacturación());
        desactive(vista.getBtnStart());

        active.setOpaque(true);
        active.repaint();

        CardLayout cl = (CardLayout) vista.getPanelContenido().getLayout();
        cl.show(vista.getPanelContenido(), panel);
    }

    private void desactive(JButton b) {
        b.setOpaque(false);
        b.repaint();
    }
}
