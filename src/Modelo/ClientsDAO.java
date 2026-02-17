/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.ArrayList;

/**
 *
 * @author dh057
 */
public class ClientsDAO {
    private ArrayList<Clients> cliente = new ArrayList<>();

    public ClientsDAO() {
    }

    public ArrayList<Clients> getCliente() {
        return cliente;
    }

    public void setCliente(ArrayList<Clients> cliente) {
        this.cliente = cliente;
    }
    
    
}
