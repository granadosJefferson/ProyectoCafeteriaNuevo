package Modelo;

import java.util.List;
import java.util.ArrayList;

public class productosDAO {

    private static ArrayList<Products> productosDB = new ArrayList<>();

    /**
     * Metodo para agregar un producto al arrayList, se hace una verificacion
     * para que no se repitan los ID, si el producto está duplicado se retorna
     * un false de lo contrario, se agrega un producto al arrayList
     */
    public boolean insertarProducto(Products producto) {

        for (Products p : productosDB) {
            if (p.getIdProduct().equals(producto.getIdProduct())) {
                return false;
            }
        }

        productosDB.add(producto);
        return true;
    }

    /**
     * Retorna una copia de la lista de productos
     */
    public List<Products> obtenerTodosLosProductos() {
        return new ArrayList<>(productosDB);
    }

    /**
     * Busca un producto por su ID
     *
     * @param id ID del producto a buscar
     * @return El producto si existe, null si no
     */
    public Products buscarProductoPorId(String id) {
        for (Products p : productosDB) {
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
        for (int i = 0; i < productosDB.size(); i++) {
            if (productosDB.get(i).getIdProduct().equals(producto.getIdProduct())) {
                productosDB.set(i, producto);
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina un producto por su ID
     *
     * @param id ID del producto a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminarProducto(String id) {
        return productosDB.removeIf(p -> p.getIdProduct().equals(id));
    }

    /**
     * Retorna la cantidad de productos en la lista
     */
    public int contarProductos() {
        return productosDB.size();
    }

    /**
     * Elimina todos los productos (útil para pruebas)
     */
    public void limpiarTodo() {
        productosDB.clear();
    }

}
