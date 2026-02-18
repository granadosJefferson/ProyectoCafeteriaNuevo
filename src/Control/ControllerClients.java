/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Control;

import Modelo.Clients;
import Vista.ClientsPanel;
import Vista.GestionCliente;
import Modelo.ClientsDAO;
import java.util.ArrayList;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 *
 * @author dh057
 */
public class ControllerClients {

    private final ClientsPanel clientp;
    private final ClientsDAO cdao;
    private final GestionCliente gestioncliente;

    public ControllerClients(ClientsPanel clientp, ClientsDAO cdao, GestionCliente gestioncliente) {
        this.clientp = clientp;
        this.cdao = cdao;
        this.gestioncliente = gestioncliente;
        loadTabledata();
        this.clientp.getBtnNewClient().addActionListener(e -> openGestionCliente());
    }

    private void accionBotonNewClient() {
        GestionCliente GC = new GestionCliente();
        GC.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        GC.setLocationRelativeTo(null);
        gestioncliente.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        GC.setVisible(true);

    }

    private void limpiarCampos(GestionCliente gc) {
        gc.getTxtId().setText("");
        gc.getTxtNombre().setText("");
        gc.getTxtTipo().setText("");
        gc.getTxtVisitas().setText("");
        gc.getTxtUltimaVisita().setText("");
        gc.getTxtTotalGastado().setText("");
    }

    private void loadTabledata() {
        DefaultTableModel model = new DefaultTableModel(); // String type, int visits, String fecha, double total, String cedula, String name

        model.addColumn("Cedula");
        model.addColumn("Nombre");
        model.addColumn("Tipo");
        model.addColumn("Visitas");
        model.addColumn("Fecha");
        model.addColumn("Total");
        ArrayList<Clients> ListCliente = cdao.getAll();

        for (Clients c : ListCliente) {
            model.addRow(new Object[]{
                c.getCedula(),
                c.getName(),
                c.getType(),
                c.getVisits(),
                c.getFecha(),
                c.getTotal()

            });
        }
        clientp.getJtbMostrarCliente().setModel(model);

    }
    // *************************************************************************

    private void openGestionCliente() {

        gestioncliente.getBtnCancelar().addActionListener(e -> cerrarVentana(gestioncliente));
        gestioncliente.getBtnGuardar().addActionListener(e -> guardarCliente(gestioncliente));

        gestioncliente.setLocationRelativeTo(null);
        gestioncliente.setVisible(true);
    }

    private void cerrarVentana(GestionCliente gc) {
        limpiarCampos(gc);
        gc.dispose();
    }

    public void guardarCliente(GestionCliente gestioncliente) {
        String cedula = gestioncliente.getTxtId().getText().trim();
        String name = gestioncliente.getTxtNombre().getText().trim();
        String type = gestioncliente.getTxtTipo().getText().trim();
        int visitas = Integer.parseInt(gestioncliente.getTxtVisitas().getText().trim());
        String ultimaVisita = gestioncliente.getTxtUltimaVisita().getText().trim();
        double total = Double.parseDouble(gestioncliente.getTxtTotalGastado().getText().trim());
//String type, int visits, String fecha, double total, String cedula, String name

        cdao.addLista(type, visitas, ultimaVisita, total, cedula, name);

        loadTabledata();
        limpiarCampos(gestioncliente);
        cerrarVentana(gestioncliente);

    }

}
