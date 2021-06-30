
import java.io.Serializable;

/**
 *
 * @author Mjed
 */
public class product implements Serializable {

    private int id;
    private String name;
    private double price;
    private int quantity;
    private int requiredQuantity = 1; // default is 1
    // make lock here????

    public product(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public product(int id, String name, double price, int quantity, int requiredQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.requiredQuantity = requiredQuantity;
    }
    
    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return this.id + "-" + this.name + "-" + this.price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    

}
