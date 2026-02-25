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

/**
 *
 * @author Daniel Araya
 *
 * En esta clase se controla la lógica del módulo de clientes.
 * Se encarga de:
 * - Cargar y mostrar la lista de clientes en la tabla del panel ClientsPanel.
 * - Abrir la ventana GestionCliente para crear (NUEVO) o modificar (MODIFICAR) clientes.
 * - Guardar, modificar y eliminar clientes usando ClientsDAO.
 * - Aplicar filtros de entrada (DocumentFilter) en los campos del formulario:
 *   - ID: solo números.
 *   - Visitas: solo números.
 *   - Total Gastado: números con decimales.
 *   - Nombre: solo letras y espacios.
 *
 * Funciona como controlador de la vista ClientsPanel y la ventana GestionCliente,
 * manejando eventos y actualizando componentes.
 */
public class ControllerClients {

    /**
     * Vista principal del módulo de clientes (tabla y botones).
     */
    private final ClientsPanel clientp;

    /**
     * DAO para persistencia y lectura de clientes desde Clients.txt.
     */
    private final ClientsDAO cdao;

    /**
     * Ventana de gestión de cliente (nuevo/modificar).
     */
    private final GestionCliente gestioncliente;

    /**
     * Clase para mostrar mensajes emergentes al usuario.
     */
    private final Mensajes msj = new Mensajes();

    /**
     * Bandera para identificar si se está creando (false) o modificando (true).
     */
    private boolean editando = false;

    /**
     * Cédula del cliente que se está modificando.
     */
    private String cedulaEditando = "";

    /**
     * Constructor: recibe la vista ClientsPanel, el DAO y la ventana GestionCliente.
     * Carga la tabla inicial, aplica filtros de entrada y registra listeners
     * para los botones de Nuevo, Modificar y Eliminar.
     *
     * @param clientp panel ClientsPanel
     * @param cdao DAO de clientes
     * @param gestioncliente ventana GestionCliente
     */
    public ControllerClients(ClientsPanel clientp, ClientsDAO cdao, GestionCliente gestioncliente) {
        this.clientp = clientp;
        this.cdao = cdao;
        this.gestioncliente = gestioncliente;

        // Aplicar filtros de entrada para restringir datos inválidos.
        aplicarFiltros(gestioncliente);

        // Carga inicial de datos en tabla.
        loadTabledata();

        // Eventos del panel principal.
        this.clientp.getBtnNewClient().addActionListener(e -> openGestionClienteNuevo());
        this.clientp.getBtnModificar().addActionListener(e -> openGestionClienteModificar());
        this.clientp.getBtnEliminar().addActionListener(e -> eliminarCliente());
    }

    /**
     * Limpia los campos del formulario GestionCliente y reinicia valores por defecto.
     *
     * @param gc ventana GestionCliente
     */
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

    /**
     * Carga la lista de clientes desde el DAO y la muestra en la JTable del panel.
     *
     * Flujo:
     * - Crea un DefaultTableModel con columnas: Cedula, Nombre, Tipo, Visitas, Fecha, Total.
     * - Obtiene la lista desde ClientsDAO.getAll().
     * - Recorre y agrega filas al modelo.
     * - Asigna el modelo a la tabla de ClientsPanel.
     */
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

    /**
     * Abre la ventana GestionCliente en modo NUEVO.
     *
     * Configuración:
     * - editando = false
     * - Cédula editable
     * - Tipo por defecto INFRECUENTE y bloqueado
     * - Visitas por defecto 1 y no editable
     * - Última visita se llena automáticamente con la fecha actual y no editable
     * - Registra listeners de Guardar/Cancelar evitando duplicados
     */
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

    /**
     * Abre la ventana GestionCliente en modo MODIFICAR.
     *
     * Flujo:
     * - Obtiene el cliente seleccionado desde la tabla.
     * - editando = true y guarda la cédula original en cedulaEditando.
     * - Llena el formulario con los datos del cliente.
     * - Cédula NO editable.
     * - Tipo, visitas y fecha quedan editables.
     * - Registra listeners de Guardar/Cancelar evitando duplicados.
     */
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

    /**
     * Coloca en el formulario GestionCliente los valores del cliente seleccionado.
     *
     * @param gc ventana GestionCliente
     * @param c cliente seleccionado
     */
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

    /**
     * Elimina el cliente seleccionado de la lista y actualiza la tabla.
     *
     * Flujo:
     * - Obtiene el cliente seleccionado.
     * - Llama ClientsDAO.eliminarDeLista(cedula).
     * - Si elimina, recarga la tabla.
     */
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

    /**
     * Obtiene el cliente seleccionado en la tabla del panel.
     *
     * Reglas:
     * - Debe haber una fila seleccionada.
     * - La cédula (columna 0) no puede ser null ni vacía.
     * - Busca el cliente en la lista del DAO y lo retorna.
     *
     * @return cliente encontrado o null si hay error/no existe
     */
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

    /**
     * Guarda un cliente nuevo o modifica uno existente, según el estado editando.
     *
     * Flujo:
     * - Lee los datos del formulario.
     * - Convierte visitas a entero y total a double.
     * - Si editando=false: agrega con ClientsDAO.addLista(...)
     * - Si editando=true: modifica con ClientsDAO.modificarEnLista(...)
     * - Recarga la tabla y cierra ventana.
     *
     * @param gestioncliente ventana GestionCliente
     */
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

    /**
     * Cierra la ventana GestionCliente y limpia sus campos.
     *
     * @param gc ventana GestionCliente
     */
    private void cerrarVentana(GestionCliente gc) {
        limpiarCampos(gc);
        gc.dispose();
    }

    /**
     * Elimina ActionListeners previos de los botones Guardar y Cancelar en la ventana,
     * para evitar duplicación de eventos al abrir varias veces el mismo formulario.
     */
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

    /**
     * Retorna la fecha actual en formato yyyy-MM-dd.
     *
     * @return fecha actual formateada
     */
    private String obtenerFechaActual() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    /**
     * Aplica filtros (DocumentFilter) a los campos del formulario para validar
     * entrada en tiempo real.
     *
     * - txtId: solo números.
     * - txtVisitas: solo números.
     * - txtTotalGastado: números con decimales.
     * - txtNombre: solo letras y espacios.
     *
     * @param gc ventana GestionCliente
     */
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