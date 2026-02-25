package Modelo;

/**
 *
 * @author Daniel Araya
 *
 * Clase que representa a un cliente dentro del sistema.
 *
 * Hereda de la clase Persons, por lo que incluye los atributos básicos
 * de una persona (cédula y nombre).
 *
 * Además, agrega información específica del cliente:
 * - type: tipo de cliente (por ejemplo: INFRECUENTE o FRECUENTE).
 * - visits: cantidad de visitas realizadas.
 * - fecha: fecha de la última visita.
 * - total: total acumulado gastado por el cliente.
 *
 * Esta clase forma parte del modelo del patrón MVC y es utilizada
 * principalmente por ClientsDAO y ControllerClients.
 */
public class Clients extends Persons {

    /**
     * Tipo de cliente (INFRECUENTE / FRECUENTE).
     */
    private String type;

    /**
     * Cantidad de visitas registradas.
     */
    private int visits;

    /**
     * Fecha de la última visita del cliente.
     */
    private String fecha;

    /**
     * Total acumulado gastado por el cliente.
     */
    private double total;

    /**
     * Constructor principal de la clase Clients.
     *
     * @param type tipo de cliente
     * @param visits cantidad de visitas
     * @param fecha fecha de última visita
     * @param total total gastado
     * @param cedula cédula del cliente (heredado de Persons)
     * @param name nombre del cliente (heredado de Persons)
     */
    public Clients(String type, int visits, String fecha, double total, String cedula, String name) {
        super(cedula, name);
        this.type = type;
        this.visits = visits;
        this.fecha = fecha;
        this.total = total;
    }

    /**
     * Obtiene el tipo de cliente.
     *
     * @return tipo de cliente
     */
    public String getType() {
        return type;
    }

    /**
     * Define el tipo de cliente.
     *
     * @param type nuevo tipo de cliente
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Obtiene la cantidad de visitas.
     *
     * @return número de visitas
     */
    public int getVisits() {
        return visits;
    }

    /**
     * Define la cantidad de visitas.
     *
     * @param visits nueva cantidad de visitas
     */
    public void setVisits(int visits) {
        this.visits = visits;
    }

    /**
     * Obtiene la fecha de última visita.
     *
     * @return fecha en formato String
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * Define la fecha de última visita.
     *
     * @param fecha nueva fecha
     */
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el total acumulado gastado por el cliente.
     *
     * @return total gastado
     */
    public double getTotal() {
        return total;
    }

    /**
     * Define el total acumulado gastado por el cliente.
     *
     * @param total nuevo total
     */
    public void setTotal(double total) {
        this.total = total;
    }
}