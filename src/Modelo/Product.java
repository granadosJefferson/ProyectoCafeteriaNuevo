package Modelo;

/**
 *
 * @author dh057
 */
public class Product {

    private String idProduct;
    private String nameProduct;
    private String category;
    private double price;
    private int cant;
    private String status;
    private String image;

    public Product(String idProduct, String nameProduct, String category, double price, int cant, String status, String image) {
        this.idProduct = idProduct;
        this.nameProduct = nameProduct;
        this.category = category;
        this.price = price;
        this.cant = cant;
        this.status = status;
        this.image = image;
    }

    public Product(String idProduct, String nameProduct, String category,
            double price, int cant, String status) {
        this(idProduct, nameProduct, category, price, cant, status, "");
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    

    public String getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(String idProduct) {
        this.idProduct = idProduct;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCant() {
        return cant;
    }

    public void setCant(int cant) {
        this.cant = cant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
