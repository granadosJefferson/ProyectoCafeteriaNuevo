package Control;

import Modelo.Tables;
import Modelo.TablesDAO;
import Vista.GestionMesas;
import Vista.ObjetoMesa;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Daniel Araya
 *
 * En esta clase se controla la lógica del módulo de gestión de mesas.
 * Se encarga de:
 * - Calcular cuántas personas hay por mesa (por cédulas únicas) leyendo "pedidos.txt".
 * - Determinar el estado de cada mesa (LIBRE, OCUPADA, LLENA) según capacidad y personas.
 * - Actualizar el color visual de cada panel de mesa en la vista GestionMesas.
 * - Registrar los eventos de clic en los paneles de mesa para abrir la vista ObjetoMesa.
 *
 * Funciona como controlador de la vista GestionMesas, manejando eventos y
 * actualizando componentes.
 */
public class ControllerGestionMesas {

    /**
     * Vista principal de gestión de mesas (panel con las mesas).
     */
    private GestionMesas vista;

    /**
     * DAO de mesas para acceder a la lista de mesas y persistir cambios de estado.
     */
    private TablesDAO tablesDAO;

    /**
     * Constructor: recibe la vista GestionMesas, carga el estado inicial de las mesas
     * y registra los listeners de clic en cada panel.
     *
     * @param vista panel GestionMesas
     */
    public ControllerGestionMesas(GestionMesas vista) {
        this.vista = vista;
        this.tablesDAO = TablesDAO.getInstancia();

        actualizarMesas();
        configurarClicks();
    }

    /**
     * Actualiza el estado y la apariencia de todas las mesas.
     *
     * Flujo:
     * - Recorre la lista de mesas desde TablesDAO.
     * - Calcula la cantidad de personas por mesa leyendo "pedidos.txt" (cédulas únicas).
     * - Determina estado dinámico:
     *   - LIBRE si personas == 0
     *   - LLENA si personas >= capacidad
     *   - OCUPADA en cualquier otro caso
     * - Persiste el estado calculado mediante TablesDAO.actualizarMesa(...)
     * - Asigna un color suave al panel según el estado.
     *
     * Nota: También actualiza (si existe) el label de personas en la mesa 2.
     */
    public void actualizarMesas() {

        for (Modelo.Tables mesa : tablesDAO.listar()) {

            int num = mesa.getTableNumber();
            String numeroMesa = String.valueOf(num);

            int personas = contarPersonasEnMesa(numeroMesa);
            int capacidad = mesa.getCapacity();

            // Estado dinámico
            Modelo.Tables.EstadoMesa estado;
            if (personas == 0) {
                estado = Modelo.Tables.EstadoMesa.LIBRE;
            } else if (personas >= capacidad) {
                estado = Modelo.Tables.EstadoMesa.LLENA;
            } else {
                estado = Modelo.Tables.EstadoMesa.OCUPADA;
            }

            mesa.setEstado(estado);
            tablesDAO.actualizarMesa(mesa);

            // Colores menos saturados
            java.awt.Color color;
            switch (estado) {
                case LIBRE:
                    color = new java.awt.Color(170, 214, 190); // verde suave
                    break;
                case OCUPADA:
                    color = new java.awt.Color(255, 210, 160); // naranja suave
                    break;
                case LLENA:
                    color = new java.awt.Color(245, 170, 170); // rojo suave
                    break;
                default:
                    color = new java.awt.Color(170, 214, 190);
            }

            if (num == 1) {
                vista.getjPanelMesa1().setBackground(color);
                // si agregas label de personas en mesa 1 aquí lo setéas
            }

            if (num == 2) {
                vista.getjPanelMesa2().setBackground(color);
                vista.getjLabelNumPersonas1().setText(String.valueOf(personas));
            }

            if (num == 3) {
                vista.getjPanelMesa3().setBackground(color);

            }

            if (num == 4) {
                vista.getjPanelMesa4().setBackground(color);

            }

            if (num == 5) {
                vista.getjPanelMesa5().setBackground(color);

            }
        }
    }

    /**
     * Cuenta la cantidad de personas distintas en una mesa leyendo "pedidos.txt".
     *
     * Lógica:
     * - Lee cada línea del archivo "pedidos.txt".
     * - Ignora líneas vacías.
     * - Valida que la línea tenga al menos 9 campos (formato CSV de pedidos).
     * - Compara el número de mesa (columna 3).
     * - Extrae la cédula del cliente (columna 4).
     * - Cuenta cédulas únicas usando una lista (sin HashSet).
     *
     * @param numeroMesa número de mesa en texto (por ejemplo "1", "2", "3"...)
     * @return cantidad de cédulas únicas encontradas para esa mesa
     */
    private int contarPersonasEnMesa(String numeroMesa) {

        java.util.ArrayList<String> cedulasUnicas = new java.util.ArrayList<>();

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("pedidos.txt"))) {

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

        } catch (Exception e) {
            System.out.println("Error contando personas: " + e.getMessage());
        }

        return cedulasUnicas.size();
    }

    /**
     * Registra los eventos de clic en cada panel de mesa y en el panel de "LLEVAR".
     *
     * Flujo:
     * - Asocia cada jPanelMesa# a un identificador de mesa ("M1"..."M5").
     * - Para el panel "LLEVAR", abre directamente la ventana con tableId = "LLEVAR".
     */
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

    /**
     * Asigna un MouseListener a un panel para abrir el detalle (ObjetoMesa)
     * con el identificador de mesa indicado.
     *
     * @param panel panel clickeable de la mesa
     * @param tableId identificador de mesa (por ejemplo "M1", "M2"...)
     */
    private void asignarClick(JPanel panel, String tableId) {

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                abrirVentanaMesa(tableId);
            }
        });
    }

    /**
     * Abre una ventana JFrame con la vista ObjetoMesa y su controlador asociado.
     *
     * Flujo:
     * - Crea el panel ObjetoMesa.
     * - Construye un JFrame modal simple (DISPOSE_ON_CLOSE).
     * - Instancia ControllerObjetoMesa para cargar la información.
     * - Ejecuta ctrl.cargar() para mostrar datos.
     *
     * @param tableId identificador de mesa a abrir ("M1"... o "LLEVAR")
     */
    private void abrirVentanaMesa(String tableId) {

        ObjetoMesa panelDetalle = new ObjetoMesa();

        JFrame ventana = new JFrame("Detalle de Mesa");
        ventana.setSize(450, 520);
        ventana.setLocationRelativeTo(vista);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setResizable(false);

        ventana.add(panelDetalle);
        ventana.setVisible(true);

        ControllerObjetoMesa ctrl
                = new ControllerObjetoMesa(panelDetalle, tableId, ventana, this);

        ctrl.cargar();
    }
}