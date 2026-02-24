package Modelo;

import java.util.ArrayList;
import java.util.List;

public class Tables {

    public enum EstadoMesa {
        LIBRE, OCUPADA, LLENA
    }

    private String tableId;
    private int tableNumber;
    private int capacity;
    private EstadoMesa estado;
    private List<String> cedulas;

    public Tables(String tableId, int tableNumber, int capacity) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.estado = EstadoMesa.LIBRE;
        this.cedulas = new ArrayList<>();
    }

    public String getTableId() { return tableId; }
    public int getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
    public EstadoMesa getEstado() { return estado; }
    public void setEstado(EstadoMesa estado) { this.estado = estado; }
    public List<String> getCedulas() { return cedulas; }

    public int personasActuales() {
        return cedulas.size();
    }

    // ====== Persistencia en texto ======
    public String toDataString() {
        String personas = String.join(";", cedulas);
        return tableId + "," + tableNumber + "," + capacity + "," + estado + "," + personas;
    }

    public static Tables fromDataString(String linea) {
        String[] p = linea.split(",", -1);
        Tables t = new Tables(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        t.estado = EstadoMesa.valueOf(p[3]);

        if (p.length > 4 && !p[4].isEmpty()) {
            String[] personas = p[4].split(";");
            for (String c : personas) {
                t.cedulas.add(c);
            }
        }

        return t;
    }
}