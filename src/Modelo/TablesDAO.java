package Modelo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TablesDAO {

    private static final String ARCHIVO = "Tables.txt";
    private static TablesDAO instancia;
    private List<Tables> mesas;

    private TablesDAO() {
        mesas = new ArrayList<>();
        inicializarArchivo();
        cargar();
    }

    public static synchronized TablesDAO getInstancia() {
        if (instancia == null) {
            instancia = new TablesDAO();
        }
        return instancia;
    }

    
    private void inicializarArchivo() {
        try {
            Path path = Paths.get(ARCHIVO);

            if (!Files.exists(path)) {
                Files.createFile(path);

                // Crear 5 mesas por defecto
                List<String> lineas = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    Tables t = new Tables("M" + i, i, 4);
                    lineas.add(t.toDataString());
                }

                Files.write(path, lineas);
            }

        } catch (IOException e) {
            System.out.println("Error creando archivo Tables.txt: " + e.getMessage());
        }
    }

    private void cargar() {
        mesas.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    mesas.add(Tables.fromDataString(linea));
                }
            }

        } catch (IOException e) {
            System.out.println("Error cargando mesas: " + e.getMessage());
        }
    }

    private void guardar() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Tables t : mesas) {
                bw.write(t.toDataString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error guardando mesas: " + e.getMessage());
        }
    }

    public List<Tables> listar() {
        return mesas;
    }

    public Tables buscarPorTableId(String id) {
        for (Tables t : mesas) {
            if (t.getTableId().equals(id)) return t;
        }
        return null;
    }

    public void actualizarMesa(Tables mesa) {
        guardar();
    }

    public void liberarMesa(String tableId) {
        Tables mesa = buscarPorTableId(tableId);
        if (mesa != null) {
            mesa.getCedulas().clear();
            mesa.setEstado(Tables.EstadoMesa.LIBRE);
            guardar();
        }
    }
}