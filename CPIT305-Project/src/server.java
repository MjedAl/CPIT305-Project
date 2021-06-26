
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

    public static connectionHandler theKitchen = null;
    public static db theDB;
    // create hashmap that will store all the connections
    public static HashMap<Integer, connectionHandler> tableConnections = new HashMap<Integer, connectionHandler>();
    public static ArrayList<connectionHandler> connections = new ArrayList<connectionHandler>();

    public static HashMap<Integer, Integer> ordersWithTableID = new HashMap<Integer, Integer>();
    public static int orderID = 0;

    //..
    public static void main(String[] args) throws IOException {
        //1-create server socket
        ServerSocket srv = new ServerSocket(1900);
        System.out.println("Server starting...");
        System.out.println("Getting DB...");
        theDB = db.getInstance();
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
    }

    // The issue iss .... what if client is waiting for some answer on something and at the same time we push update products ??? pfff FUCK THIS
    public static void updateProductsForALl() {
        // get the latest products
        // tell client to excpect updated products list comnig
        // send the products
        ArrayList<product> products;
        try {
            products = server.theDB.getProducts();
            ObjectOutputStream objectOutputStream;
            for (int i = 0; i < connections.size(); i++) {
                connections.get(i).wrt.println("updateProducts");
                try {
                    objectOutputStream = new ObjectOutputStream(connections.get(i).connection.getOutputStream());
                    objectOutputStream.writeObject(products);

                } catch (IOException ex) {
                    Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (dbNotSettedUpException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
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

    public boolean validateOrder(String order) {
        return true;
    }

    public void reserveOrderQuantites(String order) {

    }

    public void unreserverOrderQuantites(String order) {

    }

    public int sendOrderToKitchen(String order) {
        // check if order is valid first..
        // 
        if (validateOrder(order)) {
            // send order to the kitchen socket .....
            // order is valid:
            // add it to the server hash map of orders and table id
            int currentOrderNum = server.orderID;
            server.ordersWithTableID.put(currentOrderNum, this.tableNumber);
            server.orderID++;
            // table id # order # time # order ID
            PrintWriter wrt = server.theKitchen.wrt;
            wrt.println("newOrder#" + this.tableNumber + "#" + order.replace("order:", "") + "#" + "orderTime" + "#" + currentOrderNum);
            return currentOrderNum;
        }
        return -1;
    }

    public boolean sendUpdatedOrderToKitchen(String line) {
        String order = line.split(":")[1];
        String orderNumber = line.split(":")[2];
        if (validateOrder(order)) {
            PrintWriter wrt = server.theKitchen.wrt;
            wrt.println("updatedOrder#" + this.tableNumber + "#" + order.replace("order:", "") + "#" + "time" + "#" + orderNumber);
            return true;
        }
        return false;
    }

    public boolean RemoveOrderFromKitchen(String line) {
        String order = line.split(":")[1];
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
                        break;
                    } else if (line.startsWith("products")) {
                        try {
                            // client want to get the list of products
                            // get it throught the db
                            ArrayList<product> products = server.theDB.getProducts();
                            // send it via object stream
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.connection.getOutputStream());
                            objectOutputStream.writeObject(products);
                        } catch (dbNotSettedUpException ex) {
                            wrt.println("failed:0:server error");
                            // print log
                        } catch (SQLException ex) {
                            wrt.println("failed:0:server error");
                            // print log
                        }
                    } else {
                        // other command
                        if (!isKitchen) {
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
                            // send it to table???
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
                            } else {
                                // else kitchen might want to update something on the menu
                                // after we do the update
                                // we give the updated menu to everyone
                                //server.updateProductsForALl();
                            }
                            //wrt.println("recived"); // ??? WTF IS THIS
                        }
                    }
                }
            } else {
                this.connection.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(connectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
