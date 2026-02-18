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

    private ArrayList<Clients> ListCliente = new ArrayList<>();

    public ClientsDAO() {
    }

    public ArrayList<Clients> getCliente() {
        return ListCliente;
    }

    public void setCliente(ArrayList<Clients> cliente) {
        this.ListCliente = ListCliente;
    }

    public ArrayList<Clients> getAll() {
        return ListCliente;
    }
    
    public ArrayList<Clients> addLista(String type, int visits, String fecha, double total, String cedula, String name){
        
        ListCliente.add(new Clients(type,visits,fecha,total,cedula,name));
        
        return ListCliente;
    }

}
