package Modelo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;

public class pedidosDAO {

    private static final String ARCHIVO = "pedidos.txt";

    // === Contrato del archivo (mínimo) ===
    // ID,FECHA,HORA,MESA,CEDULA,ITEMS,SUBTOTAL,IVA,TOTAL  => 9 columnas
    private static final String SEP = ",";
    private static final int COLS_MIN = 9;

    // Índices (documentados para no depender de “magia”)
    private static final int IDX_ID = 0;

    // Genera el siguiente ID de pedido (robusto: max(id)+1)
    public int siguienteId() {
        Path path = Paths.get(ARCHIVO);
        if (!Files.exists(path)) return 1;

        int maxId = 0;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = safeTrim(line);
                if (line.isEmpty()) continue;

                Optional<Integer> idOpt = tryParseIdFromLine(line);
                if (idOpt.isPresent()) {
                    int id = idOpt.get();
                    if (id > maxId) maxId = id;
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo pedidos: " + e.getMessage());
            // Falla segura: si no puedo leer, al menos no reviento.
            // Pero OJO: esto podría causar colisión si el archivo sí existe.
            // Si querés ser más estricto: lanzar RuntimeException aquí.
            return maxId > 0 ? (maxId + 1) : 1;
        }

        return maxId + 1;
    }

    // Guarda una línea completa de pedido en el archivo (con validación mínima)
    public boolean guardarLinea(String linea) {
        linea = safeTrim(linea);

        if (linea.isEmpty()) {
            System.out.println("guardarLinea: línea vacía, no se guarda.");
            return false;
        }

        // Validación mínima del formato para no envenenar pedidos.txt
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

    // Busca un pedido por ID y devuelve la línea completa (defensivo)
    public String obtenerPedidoLineaPorId(int idPedido) {
        Path path = Paths.get(ARCHIVO);
        if (!Files.exists(path)) return null;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = safeTrim(linea);
                if (linea.isEmpty()) continue;

                Optional<Integer> idOpt = tryParseIdFromLine(linea);
                if (idOpt.isPresent() && idOpt.get() == idPedido) {
                    // Si quisieras, podrías validar más aquí antes de devolver.
                    return linea;
                }
            }
        } catch (IOException e) {
            System.out.println("Error buscando pedido: " + e.getMessage());
        } catch (Exception e) {
            // Catch-all para asegurar “no explota nunca”
            System.out.println("Error inesperado buscando pedido: " + e.getMessage());
        }

        return null;
    }

    // ================= Helpers =================

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static Optional<Integer> tryParseIdFromLine(String line) {
        try {
            // split con límite para no romper por comas extras al final o futuras columnas.
            // Igual seguimos esperando 9 columnas mínimas.
            String[] parts = line.split(SEP, 2); // solo necesitamos ID; no partimos todo
            if (parts.length == 0) return Optional.empty();
            String idStr = safeTrim(parts[0]);
            if (idStr.isEmpty()) return Optional.empty();
            int id = Integer.parseInt(idStr);
            if (id <= 0) return Optional.empty();
            return Optional.of(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static boolean isLineaPedidoValidaMinima(String line) {
        // Validación mínima: 9 columnas y ID numérico > 0.
        // Usamos split con -1 para conservar vacíos (importante si algún campo viene vacío).
        String[] p = line.split(SEP, -1);
        if (p.length < COLS_MIN) return false;

        try {
            int id = Integer.parseInt(safeTrim(p[IDX_ID]));
            if (id <= 0) return false;
        } catch (Exception e) {
            return false;
        }

        // Opcional: podrías validar también subtotal/iva/total numéricos
        // o que ITEMS no esté vacío, etc.
        return true;
    }
}