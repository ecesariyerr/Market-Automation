
package marketautomation;


public class Product {
    private String name;
    private String description;
    private  double price;
    private int stock;
    private String unit;

    public Product(String name, String description, double price, int stock,String unit) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.unit= unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    
    
}
