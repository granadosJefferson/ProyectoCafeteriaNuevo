package Control;

import Modelo.Tables;
import Modelo.TablesDAO;
import Vista.GestionMesas;
import Vista.ObjetoMesa;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ControllerGestionMesas {

    private GestionMesas vista;
    private TablesDAO tablesDAO;

    public ControllerGestionMesas(GestionMesas vista) {
        this.vista = vista;
        this.tablesDAO = TablesDAO.getInstancia();
        

        actualizarMesas();
        configurarClicks();
    }

    public void actualizarMesas() {

        for (Tables mesa : tablesDAO.listar()) {

            Color color = mesa.getEstado() == Tables.EstadoMesa.LIBRE
                    ? new Color(38, 169, 93)
                    : new Color(230, 126, 34);

            int num = mesa.getTableNumber();

            if (num == 1) vista.getjPanelMesa1().setBackground(color);
            if (num == 2) vista.getjPanelMesa2().setBackground(color);
            if (num == 3) vista.getjPanelMesa3().setBackground(color);
            if (num == 4) vista.getjPanelMesa4().setBackground(color);
            if (num == 5) vista.getjPanelMesa5().setBackground(color);
        }
    }

    private void configurarClicks() {

        asignarClick(vista.getjPanelMesa1(), "M1");
        asignarClick(vista.getjPanelMesa2(), "M2");
        asignarClick(vista.getjPanelMesa3(), "M3");
        asignarClick(vista.getjPanelMesa4(), "M4");
        asignarClick(vista.getjPanelMesa5(), "M5");

        vista.getjPanelLLevar().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                abrirVentanaMesa("LLEVAR");
            }
        });
    }

    private void asignarClick(JPanel panel, String tableId) {

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                abrirVentanaMesa(tableId);
            }
        });
    }

    private void abrirVentanaMesa(String tableId) {

        ObjetoMesa panelDetalle = new ObjetoMesa();

        JFrame ventana = new JFrame("Detalle de Mesa");
        ventana.setSize(450, 520);
        ventana.setLocationRelativeTo(vista);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setResizable(false);

        ventana.add(panelDetalle);
        ventana.setVisible(true);

        ControllerObjetoMesa ctrl =
                new ControllerObjetoMesa(panelDetalle, tableId, ventana, this);

        ctrl.cargar();
    }
}