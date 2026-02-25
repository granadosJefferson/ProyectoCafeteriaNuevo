package Modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * DAO para administrar productos usando archivo de texto (products.txt) y una lista en memoria.
 * Implementa Singleton para mantener una sola instancia y ofrece CRUD + recarga desde archivo.
 * También permite registrar callbacks para notificar cambios de stock.
 *
 * @author Jefferson Granados
 */
public class productosDAO {

    private static productosDAO instancia;
    private final ArrayList<Product> list;
    private static final String ARCHIVO = "products.txt";

    private final List<Runnable> stockChangeCallbacks = new ArrayList<>();

    /**
     * Constructor privado: inicializa la lista en memoria y carga productos desde archivo.
     */
    private productosDAO() {
        list = new ArrayList<>();
        cargarProductos();
    }

    /**
     * Retorna la única instancia del DAO (Singleton).
     */
    public static productosDAO getInstancia() {
        if (instancia == null) instancia = new productosDAO();
        return instancia;
    }

    /**
     * Registra un callback que se ejecuta cuando cambia el stock (insert/update/delete/recarga).
     */
    public void addStockChangeCallback(Runnable callback) {
        if (callback != null && !stockChangeCallbacks.contains(callback)) {
            stockChangeCallbacks.add(callback);
        }
    }

    /**
     * Elimina un callback previamente registrado.
     */
    public void removeStockChangeCallback(Runnable callback) {
        stockChangeCallbacks.remove(callback);
    }

    /**
     * Ejecuta todos los callbacks registrados para notificar que hubo cambios.
     */
    private void notificarStockCambiado() {
        for (Runnable callback : stockChangeCallbacks) {
            if (callback != null) callback.run();
        }
    }

    /**
     * Lee productos directamente desde el archivo y retorna una lista nueva (no usa la lista en memoria).
     */
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

    /**
     * Inserta un producto si no existe otro con el mismo ID (comparación ignorando mayúsculas/minúsculas).
     * Guarda en archivo y notifica cambios.
     */
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

    /**
     * Retorna una copia de la lista en memoria de productos.
     */
    public ArrayList<Product> obtenerTodosLosProductos() {
        return new ArrayList<>(list);
    }

    /**
     * Busca un producto por ID dentro de la lista en memoria.
     */
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

    /**
     * Actualiza un producto existente (por ID) reemplazándolo en la lista.
     * Guarda en archivo y notifica cambios.
     */
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

    /**
     * Elimina un producto por ID de la lista en memoria.
     * Guarda en archivo y notifica cambios.
     */
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

    /**
     * Guarda la lista en memoria completa en el archivo, reescribiéndolo desde cero.
     */
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

    /**
     * Carga productos desde archivo y los agrega a la lista en memoria.
     */
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

    /**
     * Recarga el inventario desde el archivo:
     * limpia la lista, vuelve a cargar y notifica a los listeners.
     */
    public void recargarDesdeArchivo() {
        list.clear();
        cargarProductos();
        notificarStockCambiado();
    }
}