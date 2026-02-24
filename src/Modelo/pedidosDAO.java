package Modelo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class pedidosDAO {

    private static final String ARCHIVO = "pedidos.txt";

    private static final String SEP = ",";
    private static final int COLS_MIN = 9;

    private static final int IDX_ID = 0;
    private static final int IDX_MESA = 3;
    

    public String rutaArchivoEnUso() {
        return Paths.get(ARCHIVO).toAbsolutePath().toString();
    }

    public void imprimirRutaArchivoEnUso() {
        System.out.println("USANDO pedidos.txt EN: " + rutaArchivoEnUso());
    }

    public int siguienteId() {
        Path path = Paths.get(ARCHIVO);
        if (!Files.exists(path)) {
            return 1;
        }

        int maxId = 0;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = safeTrim(line);
                if (line.isEmpty()) {
                    continue;
                }

                Optional<Integer> idOpt = tryParseIdFromLine(line);
                if (idOpt.isPresent()) {
                    int id = idOpt.get();
                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo pedidos: " + e.getMessage());
            return maxId > 0 ? (maxId + 1) : 1;
        }

        return maxId + 1;
    }

    public boolean guardarLinea(String linea) {
        linea = safeTrim(linea);

        if (linea.isEmpty()) {
            System.out.println("guardarLinea: línea vacía, no se guarda.");
            return false;
        }

        if (!isLineaPedidoValidaMinima(linea)) {
            System.out.println("guardarLinea: formato inválido, no se guarda. Linea=" + linea);
            return false;
        }

        try (BufferedWriter bw = Files.newBufferedWriter(
                Paths.get(ARCHIVO),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            bw.write(linea);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error guardando pedido: " + e.getMessage());
            return false;
        }
    }

    public String obtenerPedidoLineaPorId(int idPedido) {
        Path path = Paths.get(ARCHIVO);
        if (!Files.exists(path)) {
            return null;
        }

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = safeTrim(linea);
                if (linea.isEmpty()) {
                    continue;
                }

                Optional<Integer> idOpt = tryParseIdFromLine(linea);
                if (idOpt.isPresent() && idOpt.get() == idPedido) {
                    return linea;
                }
            }
        } catch (IOException e) {
            System.out.println("Error buscando pedido: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado buscando pedido: " + e.getMessage());
        }

        return null;
    }

    public List<String> obtenerPedidosLineasPorMesa(int mesa) {
        
        
         System.out.println("USANDO pedidos.txt EN: " + Paths.get(ARCHIVO).toAbsolutePath());
        
        Path path = Paths.get(ARCHIVO);
        if (!Files.exists(path)) {
            return List.of();
        }

        List<String> res = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = safeTrim(linea);
                if (linea.isEmpty()) {
                    continue;
                }

                String[] p = linea.split(SEP, -1);
                if (p.length < COLS_MIN) {
                    continue;
                }

                String mesaStr = safeTrim(p[IDX_MESA]);
                if (mesaStr.isEmpty()) {
                    continue;
                }

                try {
                    int mesaLinea = Integer.parseInt(mesaStr.replaceAll("[^0-9]", ""));
                    if (mesaLinea == mesa) {
                        res.add(linea);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            System.out.println("Error buscando pedidos por mesa: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado buscando pedidos por mesa: " + e.getMessage());
        }

        return res;
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static Optional<Integer> tryParseIdFromLine(String line) {
        try {
            String[] parts = line.split(SEP, 2);
            if (parts.length == 0) {
                return Optional.empty();
            }

            String idStr = safeTrim(parts[0]);
            if (idStr.isEmpty()) {
                return Optional.empty();
            }

            int id = Integer.parseInt(idStr);
            if (id <= 0) {
                return Optional.empty();
            }

            return Optional.of(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static boolean isLineaPedidoValidaMinima(String line) {
        String[] p = line.split(SEP, -1);
        if (p.length < COLS_MIN) {
            return false;
        }

        try {
            int id = Integer.parseInt(safeTrim(p[IDX_ID]));
            if (id <= 0) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}