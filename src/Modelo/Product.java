package Modelo;

import javax.swing.ImageIcon;

/**
 *
 * Representa la entidad Producto del sistema.
 * Guarda los datos principales (id, nombre, categoría, precio, cantidad, estado, imagen)
 * y permite cargar/escalar la imagen para mostrarla en la interfaz.
 *
 * @author Jefferson Granados
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

    /**
     * Constructor completo: asigna valores y carga el ImageIcon si hay ruta de imagen.
     */
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

    /**
     * Constructor alterno: crea un producto sin ruta de imagen (vacía).
     */
    public Product(String idProduct, String nameProduct, String category,
            double price, int cant, String status) {
        this(idProduct, nameProduct, category, price, cant, status, "");
    }

    /**
     * Carga la imagen desde recursos del proyecto o desde una ruta en disco.
     * Si logra cargarla, la escala a 100x100 y guarda el ImageIcon.
     */
    private void cargarImageIcon() {
        if (image != null && !image.isEmpty()) {
            try {
                java.net.URL imgURL = getClass().getResource(image);
                if (imgURL == null) {
                    java.io.File file = new java.io.File(image);
                    if (file.exists()) {
                        imgURL = file.toURI().toURL();
                    }
                }

                if (imgURL != null) {
                    ImageIcon imgOriginal = new ImageIcon(imgURL);
                    java.awt.Image imgEscalada = imgOriginal.getImage()
                            .getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                    this.imageIcon = new ImageIcon(imgEscalada);
                }
            } catch (Exception e) {
                this.imageIcon = null;
            }
        }
    }

    /**
     * Retorna el ImageIcon del producto; si no está cargado y hay ruta, lo carga.
     */
    public ImageIcon getImageIcon() {
        if (imageIcon == null && image != null && !image.isEmpty()) {
            cargarImageIcon();
        }
        return imageIcon;
    }

    /**
     * Retorna un ImageIcon escalado al tamaño solicitado (si existe imagen).
     */
    public ImageIcon getImageIconScaled(int width, int height) {
        if (getImageIcon() != null) {
            java.awt.Image img = imageIcon.getImage();
            java.awt.Image imgEscalada = img.getScaledInstance(width, height,
                    java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(imgEscalada);
        }
        return null;
    }

    /**
     * Devuelve la ruta/valor de imagen guardado en el producto.
     */
    public String getImage() {
        return image;
    }

    /**
     * Actualiza la ruta de imagen, reinicia el ImageIcon y vuelve a cargarlo.
     */
    public void setImage(String image) {
        this.image = image;
        this.imageIcon = null;
        cargarImageIcon();
    }

    /**
     * Devuelve el ID del producto.
     */
    public String getIdProduct() {
        return idProduct;
    }

    /**
     * Actualiza el ID del producto.
     */
    public void setIdProduct(String idProduct) {
        this.idProduct = idProduct;
    }

    /**
     * Devuelve el nombre del producto.
     */
    public String getNameProduct() {
        return nameProduct;
    }

    /**
     * Actualiza el nombre del producto.
     */
    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    /**
     * Devuelve la categoría del producto.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Actualiza la categoría del producto.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Devuelve el precio del producto.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Actualiza el precio del producto.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Devuelve la cantidad/stock del producto.
     */
    public int getCant() {
        return cant;
    }

    /**
     * Actualiza la cantidad/stock del producto.
     */
    public void setCant(int cant) {
        this.cant = cant;
    }

    /**
     * Devuelve el estado del producto (por ejemplo ACTIVO/INACTIVO).
     */
    public String getStatus() {
        return status;
    }

    /**
     * Actualiza el estado del producto.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}