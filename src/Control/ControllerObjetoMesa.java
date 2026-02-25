package Control;

import Modelo.Tables;
import Modelo.TablesDAO;
import Vista.ObjetoMesa;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Daniel Araya
 *
 * En esta clase se controla la lógica del detalle de mesa (ObjetoMesa).
 * Se encarga de:
 * - Cargar en la tabla TableInformacionMesa los pedidos asociados a una mesa desde "pedidos.txt".
 * - Construir y mostrar un panel resumen (copia visual) en jPanelCargarMesa con:
 *   - Título (MESA # o PARA LLEVAR), estado (LIBRE/OCUPADA/LLENA) y personas/capacidad.
 * - Gestionar acciones de mesa:
 *   - Cerrar mesa: elimina todos los registros de la mesa en "pedidos.txt".
 *   - Sacar persona: elimina un registro específico (mesa + cédula) en "pedidos.txt".
 * - Refrescar la vista de Gestión de Mesas (colores/estado) después de cambios.
 *
 * Funciona como controlador de la vista ObjetoMesa, manejando eventos y
 * actualizando componentes.
 */
public class ControllerObjetoMesa {

    /**
     * Vista ObjetoMesa que muestra detalle de pedidos y resumen de mesa.
     */
    private final ObjetoMesa vista;

    /**
     * Identificador de mesa usado en la navegación (por ejemplo "M1"..."M5" o "LLEVAR").
     */
    private final String tableId;

    /**
     * Ventana que contiene el panel ObjetoMesa.
     */
    private final JFrame ventana;

    /**
     * Referencia al controlador de Gestión de Mesas para refrescar colores/estado.
     */
    private final ControllerGestionMesas controllerMesas;

    /**
     * DAO de mesas para buscar capacidad/metadata por tableId.
     */
    private final TablesDAO tablesDAO;

    /**
     * Archivo base donde se almacenan los pedidos (formato CSV).
     */
    private static final String ARCHIVO_PEDIDOS = "pedidos.txt";

    /**
     * Constructor: recibe la vista ObjetoMesa, el identificador de mesa, la ventana
     * y el controlador de Gestión de Mesas.
     *
     * Registra eventos:
     * - jBtnCerrarMesa -> cerrarMesa()
     * - jBtnSacarMesa  -> sacarPersona()
     *
     * @param vista panel ObjetoMesa
     * @param tableId identificador de mesa ("M1"... o "LLEVAR")
     * @param ventana JFrame contenedor
     * @param controllerMesas controlador de Gestión de Mesas para refresco
     */
    public ControllerObjetoMesa(ObjetoMesa vista, String tableId, JFrame ventana, ControllerGestionMesas controllerMesas) {
        this.vista = vista;
        this.tableId = tableId;
        this.ventana = ventana;
        this.controllerMesas = controllerMesas;
        this.tablesDAO = TablesDAO.getInstancia();

        // Eventos botones
        this.vista.getjBtnCerrarMesa().addActionListener(e -> cerrarMesa());
        this.vista.getjBtnSacarMesa().addActionListener(e -> sacarPersona());
    }

    /**
     * Carga la información de la mesa en la vista:
     * - Panel resumen (jPanelCargarMesa).
     * - Tabla con pedidos (TableInformacionMesa).
     */
    public void cargar() {
        cargarPanelMesa();
        cargarTablaInformacionMesa();
    }

    /**
     * Carga SOLO lo importante en la tabla TableInformacionMesa:
     * - ID Pedido
     * - Fecha (fecha + hora)
     * - Cliente (cédula)
     * - Total con IVA
     *
     * Flujo:
     * - Asegura que exista el archivo pedidos.txt.
     * - Lee cada línea del archivo.
     * - Ignora líneas vacías.
     * - Valida que existan al menos 9 columnas.
     * - Filtra por la mesa correspondiente (numeroMesa).
     * - Agrega filas al DefaultTableModel y lo asigna a la JTable.
     */
    private void cargarTablaInformacionMesa() {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID Pedido", "Fecha", "Cliente (Cédula)", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        String filtroMesa = obtenerFiltroMesa();

        asegurarArchivoPedidos();

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PEDIDOS))) {
            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) {
                    continue;
                }

                String numeroMesaTxt = partes[3].trim();
                if (!numeroMesaTxt.equalsIgnoreCase(filtroMesa)) {
                    continue;
                }

                String idPedido = partes[0].trim();
                String fecha = (partes[1].trim() + " " + partes[2].trim()).trim();
                String cedula = partes[4].trim();
                String totalConIva = partes[8].trim();

                modelo.addRow(new Object[]{idPedido, fecha, cedula, "₡" + totalConIva});
            }

        } catch (IOException e) {
            System.out.println("Error leyendo pedidos.txt: " + e.getMessage());
        }

        vista.getTableInformacionMesa().setModel(modelo);
    }

    /**
     * Construye y carga un panel resumen dentro de jPanelCargarMesa.
     *
     * Lo que muestra:
     * - Título: "MESA X" o "PARA LLEVAR"
     * - Estado: LIBRE / OCUPADA / LLENA (calculado por personas/capacidad)
     * - Personas: cantidad de cédulas únicas / capacidad
     *
     * Flujo:
     * - Limpia el contenedor y crea una tarjeta (JPanel) con BoxLayout.
     * - Obtiene filtro de mesa y cuenta personas (cédulas únicas).
     * - Obtiene la capacidad desde TablesDAO (default 4 si no existe).
     * - Calcula estado y define un color suave según estado.
     * - Agrega etiquetas y repinta el contenedor.
     */
    private void cargarPanelMesa() {

        // Asegúrate que ObjetoMesa tenga getter:
        // public JPanel getjPanelCargarMesa()
        javax.swing.JPanel contenedor = vista.getjPanelCargarMesa();
        contenedor.removeAll();
        contenedor.setLayout(new java.awt.BorderLayout());

        String filtroMesa = obtenerFiltroMesa();
        int personas = contarCedulasUnicasPorMesa(filtroMesa);

        int capacidad = 4;
        Tables mesaObj = tablesDAO.buscarPorTableId(tableId);
        if (mesaObj != null) {
            capacidad = mesaObj.getCapacity();
        }

        Tables.EstadoMesa estado;
        if (personas == 0) {
            estado = Tables.EstadoMesa.LIBRE;
        } else if (personas >= capacidad) {
            estado = Tables.EstadoMesa.LLENA;
        } else {
            estado = Tables.EstadoMesa.OCUPADA;
        }

        java.awt.Color colorSuave;
        switch (estado) {
            case LIBRE:
                colorSuave = new java.awt.Color(170, 214, 190); // verde suave
                break;
            case OCUPADA:
                colorSuave = new java.awt.Color(255, 210, 160); // naranja suave
                break;
            case LLENA:
                colorSuave = new java.awt.Color(245, 170, 170); // rojo suave
                break;
            default:
                colorSuave = new java.awt.Color(170, 214, 190);
        }

        // Tarjeta "copia"
        javax.swing.JPanel tarjeta = new javax.swing.JPanel();
        tarjeta.setBackground(colorSuave);
        tarjeta.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tarjeta.setLayout(new javax.swing.BoxLayout(tarjeta, javax.swing.BoxLayout.Y_AXIS));

        String titulo = filtroMesa.equalsIgnoreCase("LLEVAR") ? "PARA LLEVAR" : "MESA " + filtroMesa;

        javax.swing.JLabel lblTitulo = new javax.swing.JLabel(titulo);
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        lblTitulo.setForeground(java.awt.Color.BLACK);

        javax.swing.JLabel lblEstado = new javax.swing.JLabel("Estado: " + estado);
        lblEstado.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        lblEstado.setForeground(java.awt.Color.DARK_GRAY);

        javax.swing.JLabel lblPersonas = new javax.swing.JLabel("Personas: " + personas + " / " + capacidad);
        lblPersonas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        lblPersonas.setForeground(java.awt.Color.DARK_GRAY);

        tarjeta.add(lblTitulo);
        tarjeta.add(javax.swing.Box.createVerticalStrut(6));
        tarjeta.add(lblEstado);
        tarjeta.add(javax.swing.Box.createVerticalStrut(4));
        tarjeta.add(lblPersonas);

        contenedor.add(tarjeta, java.awt.BorderLayout.CENTER);

        contenedor.revalidate();
        contenedor.repaint();
    }

    /**
     * Cuenta la cantidad de cédulas únicas asociadas a una mesa en pedidos.txt.
     *
     * Lógica:
     * - Lee cada línea de pedidos.txt.
     * - Filtra por la mesa (columna 3).
     * - Toma la cédula (columna 4).
     * - Cuenta cédulas únicas usando una lista (sin HashSet).
     *
     * @param numeroMesa valor de mesa del archivo (por ejemplo "1", "2", ... o "LLEVAR")
     * @return cantidad de cédulas únicas en esa mesa
     */
    private int contarCedulasUnicasPorMesa(String numeroMesa) {

        java.util.ArrayList<String> cedulasUnicas = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_PEDIDOS))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) {
                    continue;
                }

                String mesaTxt = partes[3].trim();
                String cedula = partes[4].trim();

                if (mesaTxt.equalsIgnoreCase(numeroMesa)) {
                    if (!cedula.isEmpty() && !cedulasUnicas.contains(cedula)) {
                        cedulasUnicas.add(cedula);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error contando cédulas únicas: " + e.getMessage());
        }

        return cedulasUnicas.size();
    }

    /**
     * Acción del botón Cerrar Mesa:
     * - Elimina del archivo pedidos.txt todos los registros asociados a esa mesa.
     * - Recarga la tabla y el panel resumen (quedarán vacíos por lógica si borró).
     * - Actualiza el estado/colores en Gestión de Mesas.
     *
     * Confirmación:
     * - Muestra un diálogo YES/NO antes de borrar.
     */
    private void cerrarMesa() {

        String filtroMesa = obtenerFiltroMesa();

        int op = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea cerrar la mesa y borrar los pedidos asociados?",
                "Cerrar mesa",
                JOptionPane.YES_NO_OPTION
        );
        if (op != JOptionPane.YES_OPTION) {
            return;
        }

        asegurarArchivoPedidos();

        boolean seBorroAlgo = reescribirPedidosExcluyendoMesa(filtroMesa);

        cargarTablaInformacionMesa();
        cargarPanelMesa();

        if (controllerMesas != null) {
            controllerMesas.actualizarMesas();
        }

        JOptionPane.showMessageDialog(
                vista,
                seBorroAlgo ? "Mesa cerrada. Pedidos eliminados." : "No había pedidos para esa mesa."
        );
    }

    /**
     * Acción del botón Sacar Mesa:
     * - Requiere que el usuario seleccione una fila en la tabla (cliente/cédula).
     * - Elimina del archivo pedidos.txt el registro que coincida con:
     *   - Mesa (columna 3)
     *   - Cédula (columna 4)
     * - Recarga la tabla y el panel resumen.
     * - Actualiza el estado/colores en Gestión de Mesas.
     *
     * Confirmación:
     * - Muestra un diálogo YES/NO antes de borrar la persona.
     */
    private void sacarPersona() {

        int row = vista.getTableInformacionMesa().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione una persona (fila) en la tabla.");
            return;
        }

        String filtroMesa = obtenerFiltroMesa();

        // Columna 2 = "Cliente (Cédula)" según el modelo que cargamos
        String cedula = String.valueOf(vista.getTableInformacionMesa().getValueAt(row, 2)).trim();

        if (cedula.isEmpty() || cedula.equals("-")) {
            JOptionPane.showMessageDialog(vista, "Cédula inválida en la fila seleccionada.");
            return;
        }

        int op = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea eliminar a esta persona de la mesa?\nCédula: " + cedula,
                "Sacar persona",
                JOptionPane.YES_NO_OPTION
        );
        if (op != JOptionPane.YES_OPTION) {
            return;
        }

        asegurarArchivoPedidos();

        boolean seBorro = reescribirPedidosExcluyendoMesaYCedula(filtroMesa, cedula);

        cargarTablaInformacionMesa();
        cargarPanelMesa();

        if (controllerMesas != null) {
            controllerMesas.actualizarMesas();
        }

        JOptionPane.showMessageDialog(
                vista,
                seBorro ? "Persona eliminada de la mesa." : "No se encontró esa persona en esta mesa."
        );
    }

    /**
     * Reescribe pedidos.txt excluyendo todas las líneas de la mesa indicada.
     *
     * Implementación:
     * - Lee pedidos.txt y escribe a un temporal (pedidos_tmp.txt).
     * - Omite las líneas donde la mesa coincide.
     * - Reemplaza el archivo original por el temporal.
     *
     * @param filtroMesa mesa a eliminar (por ejemplo "1", "2"... o "LLEVAR")
     * @return true si borró al menos una línea; false en caso contrario
     */
    private boolean reescribirPedidosExcluyendoMesa(String filtroMesa) {

        File original = new File(ARCHIVO_PEDIDOS);
        File temp = new File("pedidos_tmp.txt");

        boolean borro = false;

        try (BufferedReader br = new BufferedReader(new FileReader(original)); BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) {
                    continue;
                }

                String mesa = partes[3].trim();
                if (mesa.equalsIgnoreCase(filtroMesa)) {
                    borro = true;
                    continue; // no escribir
                }

                bw.write(linea);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error reescribiendo pedidos: " + e.getMessage());
        }

        reemplazarArchivo(original, temp);
        return borro;
    }

    /**
     * Reescribe pedidos.txt excluyendo una sola persona:
     * - Omite líneas donde mesa == filtroMesa y cliente == cedula.
     *
     * @param filtroMesa mesa objetivo
     * @param cedula cédula a eliminar de la mesa
     * @return true si borró al menos una línea; false en caso contrario
     */
    private boolean reescribirPedidosExcluyendoMesaYCedula(String filtroMesa, String cedula) {

        File original = new File(ARCHIVO_PEDIDOS);
        File temp = new File("pedidos_tmp.txt");

        boolean borro = false;

        try (BufferedReader br = new BufferedReader(new FileReader(original)); BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {

            String linea;
            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",", -1);
                if (partes.length < 9) {
                    continue;
                }

                String mesa = partes[3].trim();
                String cliente = partes[4].trim();

                if (mesa.equalsIgnoreCase(filtroMesa) && cliente.equalsIgnoreCase(cedula)) {
                    borro = true;
                    continue;
                }

                bw.write(linea);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error reescribiendo pedidos: " + e.getMessage());
        }

        reemplazarArchivo(original, temp);
        return borro;
    }

    /**
     * Reemplaza el archivo original por el temporal.
     * Se usa delete + renameTo para evitar fallos en Windows al sobrescribir.
     *
     * @param original archivo original (pedidos.txt)
     * @param temp archivo temporal (pedidos_tmp.txt)
     */
    private void reemplazarArchivo(File original, File temp) {
        if (original.exists()) {
            original.delete();
        }
        temp.renameTo(original);
    }

    /**
     * Verifica/crea el archivo pedidos.txt si no existe.
     */
    private void asegurarArchivoPedidos() {
        try {
            File f = new File(ARCHIVO_PEDIDOS);
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("No se pudo crear pedidos.txt: " + e.getMessage());
        }
    }

    /**
     * Obtiene el identificador real de mesa que viene guardado en pedidos.txt.
     *
     * Reglas:
     * - Si tableId == "LLEVAR" retorna "LLEVAR".
     * - Si es mesa normal ("M1"...), busca en TablesDAO y retorna tableNumber.
     * - Si no existe en DAO, retorna tableId como fallback.
     *
     * @return texto de mesa usado en pedidos.txt
     */
    private String obtenerFiltroMesa() {

        if (tableId.equalsIgnoreCase("LLEVAR")) {
            return "LLEVAR";
        }

        Tables mesa = tablesDAO.buscarPorTableId(tableId);

        if (mesa == null) {
            return tableId;
        }

        return String.valueOf(mesa.getTableNumber());
    }
}