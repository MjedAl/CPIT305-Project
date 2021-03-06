// this class that will deal with our db to send and recevie commands
// (Singleton design pattern)

/**
 *
 * @author Mjed
 */
import com.mysql.cj.protocol.Resultset;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class db {

    private static db theDB = new db();
    private boolean setup = false;
    private Connection con;
    private Statement stat;

    private db() {
        setupDB();
        System.out.println("connection is ready...");
    }

    // this way we get ensure that we can only have one instance from this class. so only one connection with the db will be 
    // established
    public static db getInstance() {
        System.out.println("Getting connection with db...");
        return theDB;
    }

    public ArrayList<product> getProducts() throws dbNotSettedUpException, SQLException {
        if (!setup) {
            throw new dbNotSettedUpException();
        }
        ResultSet resultSet = stat.executeQuery("select * from products");
        ArrayList<product> productsObj = new ArrayList<product>();
        while (resultSet.next()) {
            productsObj.add(new product(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("minitues"), resultSet.getDouble("price"), resultSet.getInt("quantity")));
        }
        return productsObj;
    }

    public void addProduct(String name, double price, int quantity, int minitues) throws SQLException {
        PreparedStatement pstat = con.prepareStatement("insert into products (name,price,quantity,minitues) values (?,?,?,?)");
        pstat.setString(1, name);
        pstat.setDouble(2, price);
        pstat.setInt(3, quantity);
        pstat.setInt(4, minitues);
        pstat.execute();
    }

    public void updateProduct(int id, String name, double price, int quantity, int minitues) throws SQLException {
        PreparedStatement pstat = con.prepareStatement("update products set name=?, price=?, quantity=?, minitues=? where id=?");
        pstat.setString(1, name);
        pstat.setDouble(2, price);
        pstat.setInt(3, quantity);
        pstat.setInt(4, minitues);
        pstat.setInt(5, id);
        pstat.execute();
    }

    public void updateProductQuantity(int id, int quantity) throws SQLException {
        PreparedStatement pstat = con.prepareStatement("update products set quantity=? where id=?");
        pstat.setInt(1, quantity);
        pstat.setInt(2, id);
        pstat.execute();
    }

    public void removeProduct(int id) throws SQLException {
        PreparedStatement pstat = con.prepareStatement("delete from products where id=?");
        pstat.setInt(1, id);
        pstat.execute();
    }

    private boolean setupDB() {
        if (setup) {
            return true;
        } else {
            try {
                this.setup = true;
                Class.forName("com.mysql.cj.jdbc.Driver");
                // local db
//                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/resturantSystem?useSSL=false", "root", "");
                // online db
                con = DriverManager.getConnection("jdbc:mysql://sql11.freemysqlhosting.net:3306/sql11422105?useSSL=false", "sql11422105", "E8GkB4LIX2");

                stat = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stat.execute("create table if not exists products (ID int primary key AUTO_INCREMENT, name char(20),price double, quantity int, minitues int)");
                return true;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(db.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (SQLException ex) {
                Logger.getLogger(db.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

    }

}
