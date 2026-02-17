package Modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class productosDAO {

    private ArrayList<Products> list;
    private static final String ARCHIVO = "products.txt";

    public productosDAO() {
        list = new ArrayList<>();
        cargarProductos(); // Cargar productos del archivo al iniciar
    }

    /**
     * Metodo para agregar un producto al arrayList y al txt, se hace una
     * verificacion para que no se repitan los ID, si el producto está duplicado
     * se retorna un false de lo contrario, se agrega un producto al arrayList
     */
    public boolean insertarProducto(Products producto) {
        // Verificar si ya existe un producto con el mismo ID
        for (Products p : list) {
            if (p.getIdProduct().equals(producto.getIdProduct())) {
                System.out.println("Producto con ID " + producto.getIdProduct() + " ya existe.");
                return false;
            }
        }

        list.add(producto);
        guardarProductos();
        System.out.println("Producto agregado: " + producto.getNameProduct());
        return true;
    }

    /**
     * Retorna una copia de la lista de productos
     */
    public ArrayList<Products> obtenerTodosLosProductos() {
        return list;
    }

    /**
     * Busca un producto por su ID
     *
     * @param id ID del producto a buscar
     * @return El producto si existe, null si no
     */
    public Products buscarProductoPorId(String id) {
        for (Products p : list) {
            if (p.getIdProduct().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Actualiza un producto existente
     *
     * @param producto Producto con los nuevos datos
     * @return true si se actualizó, false si no existía
     */
    public boolean actualizarProducto(Products producto) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIdProduct().equals(producto.getIdProduct())) {
                list.set(i, producto);
                guardarProductos();
                System.out.println("Producto actualizado: " + producto.getNameProduct());
                return true;
            }
        }
        System.out.println("Producto con ID " + producto.getIdProduct() + " no encontrado.");
        return false;
    }

    /**
     * Elimina un producto por su ID
     *
     * @param id ID del producto a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminarProducto(String id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIdProduct().equals(id)) {
                Products eliminado = list.remove(i);
                guardarProductos();
                System.out.println("Producto eliminado: " + eliminado.getNameProduct());
                return true;
            }
        }

        return false;

    }

    private void guardarProductos() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Products p : list) {
                // Formato: id,nombre,categoria,precio,cantidad,estado
                String linea = p.getIdProduct() + ","
                        + p.getNameProduct() + ","
                        + p.getCategory() + ","
                        + p.getPrice() + ","
                        + p.getCant() + ","
                        + p.getStatus();
                bw.write(linea);
                bw.newLine();
            }
            System.out.println("Archivo guardado correctamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo.");
        }
    }

    private void cargarProductos() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length == 6) {
                    String id = partes[0].trim();
                    String nombre = partes[1].trim();
                    String categoria = partes[2].trim();
                    double precio = Double.parseDouble(partes[3].trim());
                    int cantidad = Integer.parseInt(partes[4].trim());
                    String estado = partes[5].trim();

                    list.add(new Products(id, nombre, categoria, precio, cantidad, estado));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar productos: " + e.getMessage());
        }
    }

}
