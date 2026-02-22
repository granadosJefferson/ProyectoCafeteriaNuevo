/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Personal
 */
public class pedidosDAO {

    private static String ARCHIVO = "pedidos.txt";

    public int siguienteId() {
        File f = new File(ARCHIVO);
        if (!f.exists()) {
            return 1;
        }

        String ultima = null;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    ultima = line;
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo pedidos: " + e.getMessage());
            return 1;
        }

        if (ultima == null) {
            return 1;
        }

        try {
            String[] parts = ultima.split(",");
            return Integer.parseInt(parts[0].trim()) + 1; // idPedido es la 1era columna
        } catch (Exception ex) {
            return 1;
        }
    }
public boolean guardarLinea(String linea) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(linea);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error guardando pedido: " + e.getMessage());
            return false;
        }
    }
}
