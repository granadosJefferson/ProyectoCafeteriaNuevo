package Control;

import Modelo.Clients;
import Vista.ClientsPanel;
import Vista.GestionCliente;
import Vista.Mensajes;
import Modelo.ClientsDAO;
import java.util.ArrayList;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import Utils.SoloNumerosFilter;
import Utils.SoloNumerosDecimalesFilter;
import Utils.SoloLetrasEspaciosFilter;

public class ControllerClients {

    private final ClientsPanel clientp;
    private final ClientsDAO cdao;
    private final GestionCliente gestioncliente;

    private final Mensajes msj = new Mensajes();

    private boolean editando = false;
    private String cedulaEditando = "";

    public ControllerClients(ClientsPanel clientp, ClientsDAO cdao, GestionCliente gestioncliente) {
        this.clientp = clientp;
        this.cdao = cdao;
        this.gestioncliente = gestioncliente;

        // ✅ solo números en ID y Visitas
        aplicarFiltros(gestioncliente);

        loadTabledata();

        this.clientp.getBtnNewClient().addActionListener(e -> openGestionClienteNuevo());
        this.clientp.getBtnModificar().addActionListener(e -> openGestionClienteModificar());
        this.clientp.getBtnEliminar().addActionListener(e -> eliminarCliente());
    }

    private void limpiarCampos(GestionCliente gc) {
        try {
            gc.getTxtId().setText("");
            gc.getTxtNombre().setText("");

            if (gc.getCmbTipo() != null && gc.getCmbTipo().getItemCount() > 0) {
                gc.getCmbTipo().setSelectedItem("INFRECUENTE");
            }

            gc.getTxtVisitas().setText("");
            gc.getTxtUltimaVisita().setText("");
            gc.getTxtTotalGastado().setText("");
        } catch (Exception ex) {
        }
    }

    private void loadTabledata() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Cedula");
        model.addColumn("Nombre");
        model.addColumn("Tipo");
        model.addColumn("Visitas");
        model.addColumn("Fecha");
        model.addColumn("Total");

        ArrayList<Clients> ListCliente;

        try {
            ListCliente = cdao.getAll();
        } catch (Exception ex) {
            ListCliente = new ArrayList<>();
            msj.message("Error leyendo clientes.");
        }

        try {
            for (Clients c : ListCliente) {
                if (c == null) {
                    continue;
                }
                model.addRow(new Object[]{
                    c.getCedula(),
                    c.getName(),
                    c.getType(),
                    c.getVisits(),
                    c.getFecha(),
                    c.getTotal()
                });
            }
        } catch (Exception ex) {
            msj.message("Error cargando tabla.");
        }

        try {
            clientp.getJtbMostrarCliente().setModel(model);
        } catch (Exception ex) {
            msj.message("Error mostrando datos.");
        }
    }

    // ==========================
    // NUEVO
    // ==========================
    private void openGestionClienteNuevo() {
        try {
            editando = false;
            cedulaEditando = "";

            limpiarCampos(gestioncliente);

            // ID editable en nuevo
            gestioncliente.getTxtId().setEditable(true);

            // Tipo por defecto INFRECUENTE y bloqueado en NUEVO
            gestioncliente.getCmbTipo().setSelectedItem("INFRECUENTE");
            gestioncliente.getCmbTipo().setEnabled(false);

            // Visitas = 1 por defecto y NO editable en NUEVO
            gestioncliente.getTxtVisitas().setText("1");
            gestioncliente.getTxtVisitas().setEditable(false);

            // Fecha automática y NO editable en NUEVO
            gestioncliente.getTxtUltimaVisita().setText(obtenerFechaActual());
            gestioncliente.getTxtUltimaVisita().setEditable(false);

            limpiarListenersVentana();

            gestioncliente.getBtnCancelar().addActionListener(e -> cerrarVentana(gestioncliente));
            gestioncliente.getBtnGuardar().addActionListener(e -> guardarCliente(gestioncliente));

            gestioncliente.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            gestioncliente.setLocationRelativeTo(null);
            gestioncliente.setVisible(true);

        } catch (Exception ex) {
            msj.message("Error abriendo ventana.");
        }
    }

    // ==========================
    // MODIFICAR
    // ==========================
    private void openGestionClienteModificar() {
        Clients c = seleccionarClienteDeTabla();
        if (c == null) {
            return;
        }

        try {
            editando = true;
            cedulaEditando = c.getCedula();

            setValoresModificar(gestioncliente, c);

            // ID NO editable en modificar
            gestioncliente.getTxtId().setEditable(false);

            // En modificar sí se puede editar tipo y visitas
            gestioncliente.getCmbTipo().setEnabled(true);
            gestioncliente.getTxtVisitas().setEditable(true);

            // En modificar sí puede cambiar fecha si quiere
            gestioncliente.getTxtUltimaVisita().setEditable(true);

            limpiarListenersVentana();

            gestioncliente.getBtnCancelar().addActionListener(e -> cerrarVentana(gestioncliente));
            gestioncliente.getBtnGuardar().addActionListener(e -> guardarCliente(gestioncliente));

            gestioncliente.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            gestioncliente.setLocationRelativeTo(null);
            gestioncliente.setVisible(true);

        } catch (Exception ex) {
            msj.message("Error abriendo modificar.");
        }
    }

    private void setValoresModificar(GestionCliente gc, Clients c) {
        try {
            gc.getTxtId().setText(c.getCedula());
            gc.getTxtNombre().setText(c.getName());

            String tipo = (c.getType() == null) ? "INFRECUENTE" : c.getType().trim();
            gc.getCmbTipo().setSelectedItem(tipo);

            gc.getTxtVisitas().setText(String.valueOf(c.getVisits()));
            gc.getTxtUltimaVisita().setText(c.getFecha());
            gc.getTxtTotalGastado().setText(String.valueOf(c.getTotal()));
        } catch (Exception ex) {
            msj.message("Error cargando datos.");
        }
    }

    // ==========================
    // ELIMINAR
    // ==========================
    private void eliminarCliente() {
        Clients c = seleccionarClienteDeTabla();
        if (c == null) {
            return;
        }

        try {
            boolean ok = cdao.eliminarDeLista(c.getCedula());
            if (!ok) {
                msj.message("No se pudo eliminar.");
                return;
            }

            loadTabledata();
            msj.message("Cliente eliminado.");
        } catch (Exception ex) {
            msj.message("Error eliminando.");
        }
    }

    private Clients seleccionarClienteDeTabla() {
        try {
            int row = clientp.getJtbMostrarCliente().getSelectedRow();
            if (row == -1) {
                msj.message("Seleccione un cliente en la tabla.");
                return null;
            }

            Object val = clientp.getJtbMostrarCliente().getValueAt(row, 0);
            if (val == null) {
                msj.message("Selección inválida.");
                return null;
            }

            String cedula = String.valueOf(val).trim();
            if (cedula.isEmpty()) {
                msj.message("Cédula inválida.");
                return null;
            }

            ArrayList<Clients> ListCliente = cdao.getAll();
            for (Clients c : ListCliente) {
                if (c != null && cedula.equals(c.getCedula())) {
                    return c;
                }
            }

            msj.message("Cliente no encontrado.");
            return null;

        } catch (Exception ex) {
            msj.message("Error seleccionando.");
            return null;
        }
    }

    public void guardarCliente(GestionCliente gestioncliente) {
        try {
            String cedula = gestioncliente.getTxtId().getText().trim();
            String name = gestioncliente.getTxtNombre().getText().trim();
            String type = String.valueOf(gestioncliente.getCmbTipo().getSelectedItem()).trim();

            if (cedula.isEmpty() || name.isEmpty() || type.isEmpty()) {
                msj.message("Cédula, Nombre y Tipo son obligatorios.");
                return;
            }

            int visitas = 0;
            double total = 0;

            try {
                String v = gestioncliente.getTxtVisitas().getText().trim();
                if (!v.isEmpty()) {
                    visitas = Integer.parseInt(v);
                }
            } catch (Exception ex) {
                msj.message("Visitas debe ser número entero.");
                return;
            }

            String ultimaVisita = gestioncliente.getTxtUltimaVisita().getText().trim();

            try {
                String t = gestioncliente.getTxtTotalGastado().getText().trim();
                if (!t.isEmpty()) {
                    total = Double.parseDouble(t);
                }
            } catch (Exception ex) {
                msj.message("Total debe ser número.");
                return;
            }

            boolean ok;

            if (!editando) {
                ok = cdao.addLista(type, visitas, ultimaVisita, total, cedula, name);
                if (!ok) {
                    msj.message("Cédula repetida o error guardando.");
                    return;
                }
                msj.message("Cliente guardado.");
            } else {
                ok = cdao.modificarEnLista(type, visitas, ultimaVisita, total, cedulaEditando, name);
                if (!ok) {
                    msj.message("No se pudo modificar.");
                    return;
                }
                msj.message("Cliente modificado.");
            }

            loadTabledata();
            cerrarVentana(gestioncliente);

            editando = false;
            cedulaEditando = "";

        } catch (Exception ex) {
            msj.message("Error guardando.");
        }
    }

    private void cerrarVentana(GestionCliente gc) {
        limpiarCampos(gc);
        gc.dispose();
    }

    private void limpiarListenersVentana() {
        try {
            for (var al : gestioncliente.getBtnGuardar().getActionListeners()) {
                gestioncliente.getBtnGuardar().removeActionListener(al);
            }
            for (var al : gestioncliente.getBtnCancelar().getActionListeners()) {
                gestioncliente.getBtnCancelar().removeActionListener(al);
            }
        } catch (Exception ex) {
        }
    }

    private String obtenerFechaActual() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    // ✅ Filtros para ID y Visitas
    private void aplicarFiltros(GestionCliente gc) {

        try {
            AbstractDocument docId = (AbstractDocument) gc.getTxtId().getDocument();
            docId.setDocumentFilter(new SoloNumerosFilter());
        } catch (Exception ex) {
        }

        try {
            AbstractDocument docVisitas = (AbstractDocument) gc.getTxtVisitas().getDocument();
            docVisitas.setDocumentFilter(new SoloNumerosFilter());
        } catch (Exception ex) {
        }

        try {
            AbstractDocument docTotal = (AbstractDocument) gc.getTxtTotalGastado().getDocument();
            docTotal.setDocumentFilter(new SoloNumerosDecimalesFilter());
        } catch (Exception ex) {
        }

        try {
            AbstractDocument docNombre = (AbstractDocument) gc.getTxtNombre().getDocument();
            docNombre.setDocumentFilter(new SoloLetrasEspaciosFilter());
        } catch (Exception ex) {
        }
    }
}
