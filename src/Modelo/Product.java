package Modelo;

import javax.swing.ImageIcon;

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

    private transient ImageIcon imageIcon;

    public Product(String idProduct, String nameProduct, String category, double price, int cant, String status, String image) {
        this.idProduct = idProduct;
        this.nameProduct = nameProduct;
        this.category = category;
        this.price = price;
        this.cant = cant;
        this.status = status;
        this.image = image;
        cargarImageIcon();
    }

    public Product(String idProduct, String nameProduct, String category,
            double price, int cant, String status) {
        this(idProduct, nameProduct, category, price, cant, status, "");
    }

    // Método para cargar la imagen desde la ruta
    private void cargarImageIcon() {
        if (image != null && !image.isEmpty()) {
            try {
                // Intentar cargar desde recursos
                java.net.URL imgURL = getClass().getResource(image);
                if (imgURL == null) {
                    // Intentar como ruta absoluta
                    java.io.File file = new java.io.File(image);
                    if (file.exists()) {
                        imgURL = file.toURI().toURL();
                    }
                }

                if (imgURL != null) {
                    ImageIcon imgOriginal = new ImageIcon(imgURL);
                    // Escalar a tamaño estándar para vista previa (ej: 100x100)
                    java.awt.Image imgEscalada = imgOriginal.getImage()
                            .getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                    this.imageIcon = new ImageIcon(imgEscalada);
                }
            } catch (Exception e) {
                System.err.println("Error cargando imagen: " + image);
                this.imageIcon = null;
            }
        }
    }

    // Getter para el ImageIcon
    public ImageIcon getImageIcon() {
        if (imageIcon == null && image != null && !image.isEmpty()) {
            cargarImageIcon(); // Reintentar si es necesario
        }
        return imageIcon;
    }

    // Getter para imagen escalada a tamaño específico
    public ImageIcon getImageIconScaled(int width, int height) {
        if (getImageIcon() != null) {
            java.awt.Image img = imageIcon.getImage();
            java.awt.Image imgEscalada = img.getScaledInstance(width, height,
                    java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(imgEscalada);
        }
        return null;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        this.imageIcon = null;
        cargarImageIcon();
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
