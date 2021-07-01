
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    ReentrantReadWriteLock L = new ReentrantReadWriteLock();
    Lock R = L.readLock();
    Lock W = L.writeLock();

    public boolean CheckIfQuantitesIsAvailable(int wantedQuantity) {
        System.out.println(this.name + "\tc" + wantedQuantity);

        try {
            R.lock();
            if (wantedQuantity > quantity) {
                return false;
            }
        } finally {
            R.unlock();
            return true;
        }
    }

    public void reserveQuantites(int wantedQuantity) {
        System.out.println(this.name + "\tR" + wantedQuantity);
        try {
            W.lock();
            if (wantedQuantity > quantity) {
                return;
            }
            System.out.println(quantity);
            this.quantity -= wantedQuantity;
            System.out.println(quantity);
            
        } finally {
            W.unlock();
        }
    }

    public void unreserveQuantites(int wantedQuantity) {
        System.out.println(this.name + "\tU" + wantedQuantity);

        try {
            W.lock();
            this.quantity += wantedQuantity;
        } finally {
            W.unlock();
        }
    }

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
