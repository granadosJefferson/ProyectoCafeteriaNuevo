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
        conectar(vista.getBtnFacturacion(), "facturacion");
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
        desactive(vista.getBtnFacturacion());
        desactive(vista.getBtnStart());
        desactive(vista.getBtnReportes());

        active.setOpaque(true);
        active.repaint();

        CardLayout cl = (CardLayout) vista.getPanelContenido().getLayout();
        cl.show(vista.getPanelContenido(), panel);
    }

    private void desactive(JButton b) {
        b.setOpaque(false);
        b.repaint();
    }
    
      public void activarPanelExterno(String panel) {
        desactive(vista.getBtnProducts());
        desactive(vista.getBtnClients());
        desactive(vista.getBtnInventario());
        desactive(vista.getBtnPedidos());
        desactive(vista.getBtnMesas());
        desactive(vista.getBtnFacturacion());
        desactive(vista.getBtnStart());
        desactive(vista.getBtnReportes());
        
        // Activar el botón correspondiente
        JButton botonActivo = null;
        switch(panel) {
            case "products": botonActivo = vista.getBtnProducts(); break;
            case "clients": botonActivo = vista.getBtnClients(); break;
            case "inventario": botonActivo = vista.getBtnInventario(); break;
            case "order": botonActivo = vista.getBtnPedidos(); break;
            case "mesas": botonActivo = vista.getBtnMesas(); break;
            case "facturacion": botonActivo = vista.getBtnFacturacion(); break;
            case "reportes": botonActivo = vista.getBtnReportes(); break;
            case "vacio": botonActivo = vista.getBtnStart(); break;
        }
        
        if (botonActivo != null) {
            botonActivo.setOpaque(true);
            botonActivo.repaint();
        }
        
        CardLayout cl = (CardLayout) vista.getPanelContenido().getLayout();
        cl.show(vista.getPanelContenido(), panel);
    }
      
}
