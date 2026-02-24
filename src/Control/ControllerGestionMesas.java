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

        ControllerObjetoMesa ctrl
                = new ControllerObjetoMesa(panelDetalle, tableId, ventana, this);

        ctrl.cargar();
    }
}
