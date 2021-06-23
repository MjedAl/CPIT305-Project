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
    private String dbConnectionString = "";
    private Connection con;
    private Statement stat;

    private db() {
        System.out.println("first time connecting. first time settings...");
        setupDB();
        System.out.println("connection is ready...");
    }

    // this way we get ensure that we can only have one instance from this class. so only one connection with the db will be 
    // established
    public static db getInstance() {
        System.out.println("Getting connection with db...");
        return theDB;
    }

    // TODO replace with array of objects
    public ArrayList<product> getProducts() throws dbNotSettedUpException, SQLException {
        if (!setup) {
            throw new dbNotSettedUpException();
        }

        ResultSet products = stat.executeQuery("select * from products");
        
        ArrayList<product> productsObj = new ArrayList<product>();
        while (products.next()) {
            productsObj.add(new product(products.getInt("id"), products.getString("name"), products.getDouble("price")));
        }
        return productsObj;
    }

    public void addProduct(String name, double price) throws SQLException {
        PreparedStatement pstat = con.prepareStatement("insert into products values (?,?)");
        pstat.setString(0, name);
        pstat.setDouble(1, price);
        pstat.execute();
    }

    public void updateProduct(int id, String name, double price) throws SQLException {
        PreparedStatement pstat = con.prepareStatement("update products set name=?, price=? where id=?");
        pstat.setString(0, name);
        pstat.setDouble(1, price);
        pstat.setInt(2, id);
        pstat.execute();
    }

    public void removeProduct(int id) throws SQLException {
        PreparedStatement pstat = con.prepareStatement("delete from products where id=?");
        pstat.setInt(0, id);
        pstat.execute();
    }

    private boolean setupDB() {
        if (setup) {
            return true;
        } else {
            try {
                // TODO.. set up db
                this.setup = true;

                //1-load mySql Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                //2-create connection
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/resturantSystem?useSSL=false", "root", "");
                //3-create statement
                stat = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                //4- use statement to execute any sql commands
                stat.execute("create table if not exists products (ID int primary key, name char(20),price double)");
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
