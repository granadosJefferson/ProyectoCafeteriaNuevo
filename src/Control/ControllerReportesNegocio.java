package Control;

import Modelo.ReportesNegocioDAO;
import Vista.Mensajes;
import Vista.ReportesNegocio;
import java.util.ArrayList;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Daniel Araya
 *
 * En esta clase se controla la lógica del módulo ReportesNegocio.
 * Se encarga de:
 * - Cargar en la tabla jTableCargarReportes los registros de pedidos obtenidos desde ReportesNegocioDAO.
 * - Permitir dos formas de ordenamiento:
 *   - Filtrar general: ordenar por fecha.
 *   - Filtrar por mesas: ordenar por ID Mesa y luego por fecha.
 * - Implementar un buscador en vivo (txtIdBuscador) que filtra la tabla mientras el usuario escribe.
 *
 * Funciona como controlador de la vista ReportesNegocio, manejando eventos y
 * actualizando componentes.
 */
public class ControllerReportesNegocio {

    /**
     * Vista ReportesNegocio que contiene la tabla, botones y buscador.
     */
    private final ReportesNegocio vista;

    /**
     * DAO para obtener los reportes desde el archivo de pedidos.
     */
    private final ReportesNegocioDAO dao;

    /**
     * Clase para mostrar mensajes emergentes al usuario.
     */
    private final Mensajes msj = new Mensajes();

    /**
     * Modelo de la tabla donde se cargan los reportes.
     */
    private DefaultTableModel modelo;

    /**
     * Ordenador/filtrador de tabla (permite ordenar por columnas y filtrar por texto).
     */
    private TableRowSorter<DefaultTableModel> sorter;

    /**
     * Constructor: recibe la vista ReportesNegocio y configura el módulo.
     *
     * Flujo:
     * - Crea el DAO.
     * - Configura el modelo de tabla y el sorter.
     * - Carga los datos desde pedidos.txt (a través del DAO).
     * - Registra eventos para botones y buscador.
     *
     * @param vista panel ReportesNegocio
     */
    public ControllerReportesNegocio(ReportesNegocio vista) {
        this.vista = vista;
        this.dao = new ReportesNegocioDAO();

        configurarTabla();
        cargarTabla();
        configurarBotones();
        configurarBuscador();
    }

    /**
     * Configura la tabla jTableCargarReportes:
     * - Define columnas: ID, Fecha, ID Mesa, Cédula, Total.
     * - Bloquea edición de celdas.
     * - Instala TableRowSorter para ordenar y filtrar.
     */
    private void configurarTabla() {
        modelo = new DefaultTableModel(
                new String[]{"ID", "Fecha", "ID Mesa", "Cédula", "Total"}, 0
        ) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        vista.getjTableCargarReportes().setModel(modelo);

        sorter = new TableRowSorter<>(modelo);
        vista.getjTableCargarReportes().setRowSorter(sorter);
    }

    /**
     * Carga los datos en la tabla desde ReportesNegocioDAO.
     *
     * Flujo:
     * - Limpia el modelo actual.
     * - Obtiene filas con el formato: [ID, Fecha, ID Mesa, Cédula, Total].
     * - Agrega cada fila al modelo (formatea total con "₡").
     * - Aplica el orden general por fecha al finalizar.
     */
    public final void cargarTabla() {
        try {
            modelo.setRowCount(0);

            ArrayList<String[]> filas = dao.obtenerReportesDesdePedidos();
            for (String[] f : filas) {
                String total = f[4] == null ? "" : ("₡" + f[4]);
                modelo.addRow(new Object[]{f[0], f[1], f[2], f[3], total});
            }

            ordenarPorFecha();

        } catch (Exception e) {
            msj.message("Error cargando reportes.");
        }
    }

    /**
     * Registra eventos para los botones:
     * - jbtnFiltrarGeneral: ordena por fecha.
     * - jbtnFiltrarPorMesas: ordena por mesa y luego fecha.
     */
    private void configurarBotones() {
        vista.getJbtnFiltrarGeneral().addActionListener(e -> ordenarPorFecha());
        vista.getJbtnFiltrarPorMesas().addActionListener(e -> ordenarPorMesa());
    }

    /**
     * Registra el buscador en vivo en txtIdBuscador.
     *
     * Flujo:
     * - Cada vez que el usuario escribe o borra, se llama a filtrar().
     * - El filtrado se aplica directamente al TableRowSorter.
     */
    private void configurarBuscador() {
        vista.getTxtIdBuscador().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });
    }

    /**
     * Filtra la tabla según el texto escrito en txtIdBuscador.
     *
     * Reglas:
     * - Si el texto está vacío, elimina el filtro y muestra todo.
     * - Si tiene texto, aplica un filtro case-insensitive buscando coincidencias.
     *
     * Nota:
     * - En esta implementación, el filtro se aplica a todas las columnas
     *   (porque no se especifica índice de columna).
     */
    private void filtrar() {
        try {
            String texto = vista.getTxtIdBuscador().getText().trim();

            if (texto.isEmpty()) {
                sorter.setRowFilter(null);
                return;
            }

            sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));

        } catch (Exception e) {
            sorter.setRowFilter(null);
        }
    }

    /**
     * Ordena la tabla por ID Mesa y luego por Fecha.
     *
     * Orden aplicado:
     * - Columna 2: ID Mesa (ascendente)
     * - Columna 1: Fecha (ascendente)
     */
    private void ordenarPorMesa() {
        try {
            java.util.List<javax.swing.RowSorter.SortKey> keys = new java.util.ArrayList<>();
            keys.add(new javax.swing.RowSorter.SortKey(2, javax.swing.SortOrder.ASCENDING));
            keys.add(new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
            sorter.sort();
        } catch (Exception e) {
            msj.message("No se pudo ordenar por mesas.");
        }
    }

    /**
     * Ordena la tabla de forma general por Fecha (ascendente).
     *
     * Orden aplicado:
     * - Columna 1: Fecha (ascendente)
     */
    private void ordenarPorFecha() {
        try {
            java.util.List<javax.swing.RowSorter.SortKey> keys = new java.util.ArrayList<>();
            keys.add(new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
            sorter.sort();
        } catch (Exception e) {
            msj.message("No se pudo ordenar por fecha.");
        }
    }
}