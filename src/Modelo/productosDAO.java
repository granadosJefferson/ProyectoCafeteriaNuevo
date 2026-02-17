package Modelo;

import java.awt.List;
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


    public List<Products> obtenerTodosLosProductos() {
        return new ArrayList<>(productosDB);
    }

    

}
