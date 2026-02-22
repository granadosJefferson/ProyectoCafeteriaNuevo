package Modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ClientsDAO {

    private ArrayList<Clients> ListCliente;

    private final String FILE_NAME = "Clients.txt";

    public ClientsDAO() {
        ListCliente = new ArrayList<>();
        cargarDesdeTxt();
    }

    public void cargarDesdeTxt() {
        ListCliente.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {

                
                String[] parts = line.split(",");
                if (parts.length == 6) {

                    String cedula = parts[0];
                    String name = parts[1];
                    String type = parts[2];

                    int visits = 0;
                    double total = 0;

                    try { visits = Integer.parseInt(parts[3]); } catch (Exception e) { visits = 0; }
                    String fecha = parts[4];
                    try { total = Double.parseDouble(parts[5]); } catch (Exception e) { total = 0; }

                    ListCliente.add(new Clients(type, visits, fecha, total, cedula, name));
                }
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public ArrayList<Clients> getAll() {
        return ListCliente;
    }

    public Clients buscarPorCedula(String cedulaBuscada) {
  
         try (BufferedReader br = new BufferedReader(new FileReader("Clients.txt"))) {
            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) continue;

                String[] p = linea.split(",");

                if (p.length < 6) continue;

                String cedula = p[0].trim();
                if (!cedula.equals(cedulaBuscada)) continue;

                String name   = p[1].trim();
                String type   = p[2].trim();
                int visits    = Integer.parseInt(p[3].trim());
                String fecha  = p[4].trim();
                double total  = Double.parseDouble(p[5].trim());

              
                return new Clients(type, visits, fecha, total, cedula, name);
            }

        } catch (Exception e) {
            System.out.println("Error leyendo Clients.txt: " + e.getMessage());
        }

        return null;
    }
    
    public boolean cedulaExist(String cedula) {
        try {
            for (Clients c : ListCliente) {
                if (c != null && c.getCedula().equals(cedula)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            // no reventar
        }
        return false;
    }

    public boolean addLista(String type, int visits, String fecha, double total, String cedula, String name) {

        if (cedulaExist(cedula)) {
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(cedula + "," + name + "," + type + "," + visits + "," + fecha + "," + total);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
            return false;
        }

        ListCliente.add(new Clients(type, visits, fecha, total, cedula, name));
        return true;
    }


    public boolean modificarEnLista(String type, int visits, String fecha, double total, String cedula, String name) {

        boolean found = false;

        try {
            for (Clients c : ListCliente) {
                if (c != null && c.getCedula().equals(cedula)) {
                    c.setName(name);
                    c.setType(type);
                    c.setVisits(visits);
                    c.setFecha(fecha);
                    c.setTotal(total);
                    found = true;
                    break;
                }
            }
        } catch (Exception ex) {
            return false;
        }

        if (!found) return false;

        return reescribirTxt();
    }

    public boolean eliminarDeLista(String cedula) {
        boolean removed = false;

        try {
            for (int i = 0; i < ListCliente.size(); i++) {
                Clients c = ListCliente.get(i);
                if (c != null && c.getCedula().equals(cedula)) {
                    ListCliente.remove(i);
                    removed = true;
                    break;
                }
            }
        } catch (Exception ex) {
            return false;
        }

        if (!removed) return false;

        return reescribirTxt();
    }


    private boolean reescribirTxt() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, false))) {

            for (Clients c : ListCliente) {
                if (c == null) continue;

                bw.write(c.getCedula() + "," + c.getName() + "," + c.getType() + ","
                        + c.getVisits() + "," + c.getFecha() + "," + c.getTotal());
                bw.newLine();
            }

            return true;

        } catch (IOException e) {
            System.out.println("Error rewriting file: " + e.getMessage());
            return false;
        }
    }
}
