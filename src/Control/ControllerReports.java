package Control;

import Vista.Reports;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;

public class ControllerReports {

    private Reports vista;

    public ControllerReports(Reports vista) {
        this.vista = vista;

        vista.getBtnGenerarReporte().addActionListener(e -> cargarReportes());
    }

    private void cargarReportes() {

        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Fecha", "Mesa", "Cliente", "Total"}, 0);

        try (BufferedReader br = new BufferedReader(new FileReader("pedidos.txt"))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) continue;

                String[] p = linea.split(",", -1);

                if (p.length < 9) continue;

                modelo.addRow(new Object[]{
                    p[1] + " " + p[2],
                    p[3],
                    p[4],
                    "₡" + p[8]
                });
            }

        } catch (IOException e) {
            System.out.println("Error leyendo pedidos.txt: " + e.getMessage());
        }

        vista.getjTableReceptorReportes().setModel(modelo);
    }
}