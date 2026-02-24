package Modelo;

import java.io.*;
import java.nio.file.*;

public class ReportesDAO {

    private static final String ARCHIVO = "Reportes.txt";

    public ReportesDAO() {
        inicializarArchivo();
    }

    private void inicializarArchivo() {
        try {
            Path path = Paths.get(ARCHIVO);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.out.println("Error creando Reportes.txt: " + e.getMessage());
        }
    }

    public void guardarReporte(String fecha, String mesa, String cliente,
                                String productos, String total) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(fecha + "," + mesa + "," + cliente + "," + productos + "," + total);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error guardando reporte: " + e.getMessage());
        }
    }
}