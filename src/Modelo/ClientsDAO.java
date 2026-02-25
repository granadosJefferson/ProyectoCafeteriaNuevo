package Modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Daniel Araya
 *
 * Clase DAO (Data Access Object) encargada de la persistencia de clientes.
 *
 * Se encarga de:
 * - Cargar los clientes desde el archivo "Clients.txt".
 * - Agregar nuevos clientes al archivo y a la lista en memoria.
 * - Modificar clientes existentes.
 * - Eliminar clientes.
 * - Reescribir el archivo completo cuando hay cambios.
 *
 * Implementa una persistencia simple basada en archivo de texto
 * con formato CSV:
 *
 * cedula,nombre,tipo,visitas,fecha,total
 *
 * Forma parte del Modelo dentro del patrón MVC.
 */
public class ClientsDAO {

    /**
     * Lista en memoria que almacena los clientes cargados desde el archivo.
     */
    private ArrayList<Clients> ListCliente;

    /**
     * Nombre del archivo donde se almacenan los clientes.
     */
    private final String FILE_NAME = "Clients.txt";

    /**
     * Constructor:
     * - Inicializa la lista en memoria.
     * - Carga los datos desde el archivo.
     */
    public ClientsDAO() {
        ListCliente = new ArrayList<>();
        cargarDesdeTxt();
    }

    /**
     * Carga todos los clientes desde el archivo "Clients.txt" hacia la lista en memoria.
     *
     * Flujo:
     * - Limpia la lista actual.
     * - Lee cada línea del archivo.
     * - Separa por coma (CSV).
     * - Crea objetos Clients y los agrega a la lista.
     */
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

                    try { 
                        visits = Integer.parseInt(parts[3]); 
                    } catch (Exception e) { 
                        visits = 0; 
                    }

                    String fecha = parts[4];

                    try { 
                        total = Double.parseDouble(parts[5]); 
                    } catch (Exception e) { 
                        total = 0; 
                    }

                    ListCliente.add(new Clients(type, visits, fecha, total, cedula, name));
                }
            }
        } catch (java.io.FileNotFoundException e) {
            // Si el archivo no existe aún, no se lanza error
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Retorna la lista completa de clientes en memoria.
     *
     * @return lista de clientes
     */
    public ArrayList<Clients> getAll() {
        return ListCliente;
    }

    /**
     * Busca un cliente por cédula leyendo directamente el archivo.
     *
     * @param cedulaBuscada cédula del cliente
     * @return objeto Clients si lo encuentra; null en caso contrario
     */
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

    /**
     * Verifica si una cédula ya existe en la lista en memoria.
     *
     * @param cedula cédula a validar
     * @return true si existe; false si no
     */
    public boolean cedulaExist(String cedula) {
        try {
            for (Clients c : ListCliente) {
                if (c != null && c.getCedula().equals(cedula)) {
                    return true;
                }
            }
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * Agrega un nuevo cliente.
     *
     * Flujo:
     * - Verifica que la cédula no exista.
     * - Escribe el nuevo cliente al final del archivo.
     * - Lo agrega también a la lista en memoria.
     *
     * @return true si se agregó correctamente; false si hubo error o cédula repetida
     */
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

    /**
     * Modifica un cliente existente en la lista y reescribe el archivo completo.
     *
     * @return true si se modificó correctamente; false si no se encontró
     */
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

    /**
     * Elimina un cliente de la lista en memoria y reescribe el archivo.
     *
     * @param cedula cédula del cliente a eliminar
     * @return true si fue eliminado correctamente
     */
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

    /**
     * Reescribe completamente el archivo "Clients.txt"
     * usando la lista en memoria.
     *
     * @return true si la operación fue exitosa
     */
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