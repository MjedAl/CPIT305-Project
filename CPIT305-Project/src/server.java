
import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
    This class will be our server, it will accept clients (Kitchen and customers devices)
    It will also connect to the DB. and store log in files as well
 */
/**
 *
 * @author Mjed
 */

public class server {
    // the kitchen socker
    public static connectionHandler theKitchen = null;
    // the db
    public static db theDB;
    // a hashmap that will store all the tables connections 
    public static HashMap<Integer, connectionHandler> tableConnections = new HashMap<Integer, connectionHandler>();
    // an array list that will store all the connections, wether table or kitchen.
    public static ArrayList<connectionHandler> connections = new ArrayList<connectionHandler>();
    // a hashmap that will keep track of all order ids with table ids. (so we can get the table id from the order id)
    public static HashMap<Integer, Integer> ordersWithTableID = new HashMap<Integer, Integer>();
    // incrmental number
    public static int orderID = 1;
    // an array list of products
    public static ArrayList<product> products;

    public static void main(String[] args) throws IOException {
        try {
            //1-create server socket
            ServerSocket srv = new ServerSocket(1900);
            System.out.println("Server starting...");
            System.out.println("Getting DB...");
            theDB = db.getInstance();
            System.out.println("Getting list of products...");
            server.products = theDB.getProducts();
            //
            connectionHandler handler;
            while (true) {
                System.out.println("Waiting for connection...");
                // everytime we have new connection make new thread for it.
                Socket soc = srv.accept();
                handler = new connectionHandler(soc);
                handler.start();
                connections.add(handler);
                System.out.println("Connection recevied and thread was made for it...");
            }
        } catch (dbNotSettedUpException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // TODO add a method that will create a folder logs, and the date of today.
    // then create two txt file inside them. one for normal logs and other for errors

    // something changed in the db so first we update the list for our self before telling everyone to update their list
    public static void updateProductsForALl() {
        try {
            server.products = server.theDB.getProducts();
        } catch (dbNotSettedUpException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        }
        // tell all clients to update their products list
        for (int i = 0; i < connections.size(); i++) {
            connections.get(i).wrt.println("updateProducts");
        }

    }
}

class connectionHandler extends Thread {

    Socket connection;
    boolean isKitchen;
    int tableNumber;
    PrintWriter wrt;
    Scanner scan;

    public connectionHandler(Socket connection) {
        this.connection = connection;
        InputStream is;
        try {
            is = connection.getInputStream();
            OutputStream os = connection.getOutputStream();
            this.scan = new Scanner(is);
            this.wrt = new PrintWriter(os, true);
        } catch (IOException ex) {
            Logger.getLogger(connectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // TODO
    public boolean validateOrder(String order) {
        return true;
    }
    // TODO

    public void reserveOrderQuantites(String order) {

    }
    // TODO

    public void unreserverOrderQuantites(String order) {

    }

    public int sendOrderToKitchen(String order) {
        // TODO check if order is valid first..
        if (validateOrder(order)) {
            // send order to the kitchen socket
            // add the order to the server hash map of orders and table id
            int currentOrderNum = server.orderID;
            server.ordersWithTableID.put(currentOrderNum, this.tableNumber);
            server.orderID++;
            // send the order details to the kitchen print writer
            // table id # order # time # order ID
            PrintWriter wrt = server.theKitchen.wrt;
            wrt.println("newOrder#" + this.tableNumber + "#" + order.replace("order:", "") + "#" + "orderTime" + "#" + currentOrderNum);
            return currentOrderNum;
        }
        return -1;
    }
    // some order got updated so send it's information to the kitchen

    public boolean sendUpdatedOrderToKitchen(String line) {
        String order = line.split(":")[1];
        String orderNumber = line.split(":")[2];
        if (validateOrder(order)) {
            PrintWriter wrt = server.theKitchen.wrt;
            wrt.println("updateOrder#" + this.tableNumber + "#" + order.replace("order:", "") + "#" + "time" + "#" + orderNumber);
            return true;
        }
        return false;
    }
    // client want's to delete some order. send the order to the kitchen

    public boolean RemoveOrderFromKitchen(String line) {
        String order = line.split(":")[1];
        // TODO re add the reserved order quantites
        String orderNumber = line.split(":")[2];
        //unreserverOrderQuantites(order);
        PrintWriter wrt = server.theKitchen.wrt;
        wrt.println("removeOrder#" + orderNumber);
        return true;
    }

    @Override
    public void run() {
        System.out.println("Recevied new connection");
        // start reading from the client

        // check the type of client.
        // if is kitchen and kitchen exists already reject
        // else accept
        // if is normal user and kitchen exist accept else reject or maybe but on hold.
        try {
            boolean rejected = false;
            String line;
            // first read for the type of the client
            line = scan.nextLine();
            if (line.equalsIgnoreCase("kitchen") && server.theKitchen != null) {
                wrt.println("rejected:1:kitchen already exists");
                System.out.println("Duplicate kitchen");
                rejected = true;
            } else if (line.equalsIgnoreCase("Kitchen") && server.theKitchen == null) {
                // I'm the kitchen
                wrt.println("accepted");
                System.out.println("Kitchen accepted");
                server.theKitchen = this;
                this.isKitchen = true;
            } else if (line.startsWith("table") && server.theKitchen == null) {
                wrt.println("rejected:1:kitchen is not registerd yet");
                System.out.println("table coming early");
                rejected = true;
            } else if (line.startsWith("table") && server.theKitchen != null) {
                // I'm table and there's kitchen
                try {
                    this.tableNumber = Integer.parseInt(line.split(":")[1]);
                    wrt.println("accepted");
                } catch (NumberFormatException e) {
                    wrt.println("rejected:1:Invalid table number");
                    System.out.println("table rejected");
                    rejected = true;
                }
            } else {
                wrt.println("rejected:1:unknown command");
                System.out.println("table accepted");
                rejected = true;
            }
            if (!rejected) {
                // store the connection in the server hash table
                server.tableConnections.put(tableNumber, this);
                System.out.println("Connection stored in the server hashmap");

                while (true) {
                    // waiting for commands.
                    line = scan.nextLine();
                    System.out.println("Recevied command from : -"
                            + (this.isKitchen ? "Kitchen" : " table number:" + this.tableNumber)
                            + "- commands is :" + line);

                    if (line.equalsIgnoreCase("exit")) {
                        // TODO
                        break;
                    } else if (line.startsWith("products")) {
                        // client want to get the list of products
                        // send it via object stream
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.connection.getOutputStream());
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(server.products);
                    } else {
                        // other commands
                        if (!isKitchen) {
                            // Table
                            if (line.startsWith("order:")) {
                                int orderID = sendOrderToKitchen(line);
                                if (orderID != -1) {
                                    wrt.println("accepted:" + orderID);
                                } else {
                                    wrt.println("failed");
                                }
                            } else if (line.startsWith("orderUpdate:")) {
                                // client want to update his order before it got approved
                                boolean status = sendUpdatedOrderToKitchen(line);
                                // brbr
                            } else if (line.startsWith("orderDelete:")) {
                                // client want to remove his order before it got approved
                                boolean status = RemoveOrderFromKitchen(line);
                                // brbr
                            } else {
                                wrt.println("rejected:0:unknown operation");
                            }
                        } else {
                            // kitchen recevied some order and they want to change it's status
                            if (line.startsWith("orderStatusUpdate")) {
                                // orderStatusUpdate:X:Y
                                // X == status
                                // status : 1 means aproved and they are working on it
                                // status : 2 means that the order is on it's way
                                // Y == order number
                                // Send the order update to the connection of the table
                                int status = Integer.parseInt(line.split(":")[1]);
                                int orderID = Integer.parseInt(line.split(":")[2]);
                                // first get the table ID from the order ID
                                int tableID = server.ordersWithTableID.get(orderID);
                                // get the connection from the table id
                                connectionHandler tableCon = server.tableConnections.get(tableID);
                                // send the update to the table
                                tableCon.wrt.println("orderStatusUpdate:" + status + ":" + orderID);
                            } else if (line.startsWith("addProduct")) {
                                String name = (line.split(":")[1]);
                                double price = Double.parseDouble(line.split(":")[2]);
                                int quantity = Integer.parseInt(line.split(":")[3]);
                                server.theDB.addProduct(name, price, quantity);
                                // tell everyone to update their list
                                server.updateProductsForALl();
                            } else if (line.startsWith("updateProduct")) {
                                int id = Integer.parseInt(line.split(":")[1]);
                                String name = (line.split(":")[2]);
                                double price = Double.parseDouble(line.split(":")[3]);
                                int quantity = Integer.parseInt(line.split(":")[4]);
                                server.theDB.updateProduct(id, name, price, quantity);
                                // tell everyone to update their list
                                server.updateProductsForALl();
                            } else if (line.startsWith("deleteProduct")) {
                                int id = Integer.parseInt(line.split(":")[1]);
                                server.theDB.removeProduct(id);
                                // tell everyone to update their list
                                server.updateProductsForALl();
                            } else {
                                // TODO add more cases?
                            }
                        }
                    }
                }
            } else {
                this.connection.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(connectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(connectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
