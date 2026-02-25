package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jefferson Granados
 * En esta clase se gestiona la persistencia y administración de productos.
 * Se implementa el patrón Singleton para garantizar una única instancia.
 * 
 * Funciones principales:
 * - Insertar productos
 * - Actualizar productos
 * - Eliminar productos
 * - Buscar productos por ID
 * - Listar productos
 * - Cargar y guardar productos en archivo (products.txt)
 * - Notificar cambios de stock mediante callbacks
 * 
 * La información se almacena en un archivo de texto y se mantiene
 * una lista en memoria para las operaciones CRUD.
 */
public class productosDAO {

    private static productosDAO instancia;
    private final ArrayList<Product> list;
    private static final String ARCHIVO = "products.txt";

    private final List<Runnable> stockChangeCallbacks = new ArrayList<>();

    private productosDAO() {
        list = new ArrayList<>();
        cargarProductos();
    }

    public static productosDAO getInstancia() {
        if (instancia == null) instancia = new productosDAO();
        return instancia;
    }

    public void addStockChangeCallback(Runnable callback) {
        if (callback != null && !stockChangeCallbacks.contains(callback)) {
            stockChangeCallbacks.add(callback);
        }
    }

    public void removeStockChangeCallback(Runnable callback) {
        stockChangeCallbacks.remove(callback);
    }

    private void notificarStockCambiado() {
        for (Runnable callback : stockChangeCallbacks) {
            if (callback != null) callback.run();
        }
    }

    // Lee productos desde archivo (lista nueva)
    public List<Product> listar() {
        List<Product> lista = new ArrayList<>();
        File f = new File(ARCHIVO);
        if (!f.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 6) continue;

                String id = p[0].trim();
                String nombre = p[1].trim();
                String categoria = p[2].trim();
                double precio = Double.parseDouble(p[3].trim());
                int cantidad = Integer.parseInt(p[4].trim());
                String estado = p[5].trim();
                String imagen = (p.length >= 7) ? p[6].trim() : "";

                lista.add(new Product(id, nombre, categoria, precio, cantidad, estado, imagen));
            }
        } catch (Exception e) {
        }

        return lista;
    }

    public boolean insertarProducto(Product producto) {
        if (producto == null || producto.getIdProduct() == null) return false;

        String nuevoId = producto.getIdProduct().trim();

        for (Product p : list) {
            if (p.getIdProduct() != null && p.getIdProduct().trim().equalsIgnoreCase(nuevoId)) {
                return false;
            }
        }

        list.add(producto);
        guardarProductos();
        notificarStockCambiado();
        return true;
    }

    public ArrayList<Product> obtenerTodosLosProductos() {
        return new ArrayList<>(list);
    }

    public Product buscarProductoPorId(String id) {
        if (id == null) return null;
        String buscar = id.trim();

        for (Product p : list) {
            if (p.getIdProduct() != null && p.getIdProduct().trim().equalsIgnoreCase(buscar)) {
                return p;
            }
        }
        return null;
    }

    public boolean actualizarProducto(Product producto) {
        if (producto == null || producto.getIdProduct() == null) return false;

        String id = producto.getIdProduct().trim();

        for (int i = 0; i < list.size(); i++) {
            Product actual = list.get(i);
            if (actual.getIdProduct() != null && actual.getIdProduct().trim().equalsIgnoreCase(id)) {
                list.set(i, producto);
                guardarProductos();
                notificarStockCambiado();
                return true;
            }
        }

        return false;
    }

    public boolean eliminarProducto(String id) {
        if (id == null) return false;
        String target = id.trim();

        for (int i = 0; i < list.size(); i++) {
            Product p = list.get(i);
            if (p.getIdProduct() != null && p.getIdProduct().trim().equalsIgnoreCase(target)) {
                list.remove(i);
                guardarProductos();
                notificarStockCambiado();
                return true;
            }
        }

        return false;
    }

    private void guardarProductos() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Product p : list) {
                String linea = (p.getIdProduct() == null ? "" : p.getIdProduct()) + ","
                        + (p.getNameProduct() == null ? "" : p.getNameProduct()) + ","
                        + (p.getCategory() == null ? "" : p.getCategory()) + ","
                        + p.getPrice() + ","
                        + p.getCant() + ","
                        + (p.getStatus() == null ? "" : p.getStatus()) + ","
                        + (p.getImage() == null ? "" : p.getImage());

                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
        }
    }

    private void cargarProductos() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 6) {
                    String id = partes[0].trim();
                    String nombre = partes[1].trim();
                    String categoria = partes[2].trim();
                    double precio = Double.parseDouble(partes[3].trim());
                    int cantidad = Integer.parseInt(partes[4].trim());
                    String estado = partes[5].trim();
                    String imagen = (partes.length >= 7) ? partes[6].trim() : "";

                    list.add(new Product(id, nombre, categoria, precio, cantidad, estado, imagen));
                }
            }
        } catch (IOException | NumberFormatException e) {
        }
    }

    public void recargarDesdeArchivo() {
        list.clear();
        cargarProductos();
        notificarStockCambiado();
    }
}