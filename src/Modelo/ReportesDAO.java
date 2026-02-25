package Modelo;

import java.io.*;
import java.nio.file.*;

/**
 *
 * DAO encargado de guardar reportes en un archivo de texto (Reportes.txt).
 * Se asegura de crear el archivo si no existe y permite agregar nuevas líneas
 * con la información del reporte.
 *
 * @author Jefferson
 */
public class ReportesDAO {

    private static final String ARCHIVO = "Reportes.txt";

    /**
     * Constructor: inicializa el archivo de reportes si aún no existe.
     */
    public ReportesDAO() {
        inicializarArchivo();
    }

    /**
     * Verifica si Reportes.txt existe; si no, lo crea.
     * Si ocurre un error, lo muestra por consola.
     */
    private void inicializarArchivo() {
        try {
            Path path = Paths.get(ARCHIVO);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
     
        }
    }

    /**
     * Guarda un reporte agregándolo al final del archivo.
     * El formato es CSV simple separado por comas: fecha, mesa, cliente, productos, total.
     */
    public void guardarReporte(String fecha, String mesa, String cliente,
                               String productos, String total) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(fecha + "," + mesa + "," + cliente + "," + productos + "," + total);
            bw.newLine();
        } catch (IOException e) {
          
        }
    }
}