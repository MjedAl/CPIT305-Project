
import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        //create folder to store all log Files
        File f = new File("LogFiles");
        f.mkdir();

        //create folder to store server log File
        File file = new File("LogFiles\\Server LogFiles");
        file.mkdir();

        //create new file for all server command
        FileOutputStream serverCommand = new FileOutputStream("LogFiles\\Server LogFiles\\Server Command.txt", true);
        //create new file for all server error
        FileOutputStream serverError = new FileOutputStream("LogFiles\\Server LogFiles\\Server Error.txt");
        //set the default output to new file to store all the command
        System.setOut(new PrintStream(serverCommand));
        //set the default error to new file to store all the error message
        System.setErr(new PrintStream(serverError));
        //Write the current date on top of the file
        LocalDateTime d = LocalDateTime.now();
        String formattedDate = d.format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"));
        System.out.println("/////////////////////////////////////////");
        System.out.println("Current Date: " + formattedDate + "\n");

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
                System.err.println();
            }
        } catch (dbNotSettedUpException ex) {
            System.err.println("Error " + ex);
        } catch (SQLException ex) {
            System.err.println("Error " + ex);
        }
    }

    // something changed in the db so first we update the list for our self before telling everyone to update their list
    public static void updateProductsForALl() {
        try {
            server.products = server.theDB.getProducts();
        } catch (dbNotSettedUpException ex) {
            System.err.println("Error " + ex);
        } catch (SQLException ex) {
            System.err.println("Error " + ex);
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
            System.err.println("Error " + ex);
        }
    }

    // This method will take an order and check if ALL quantites is found it will continue
    // if not it will not reserve ANYTHING and it will return
    // if quantity for all is found it will reserve from other method reserveOrderQuantites
    public String validateOrder(String order) {
        // order format (x*y + x*y)
        // x == quantity
        // y == name of the order
        String[] orders = order.split("\\+");
        // first validate all the orders with quantities
        // if ALL are okay only then go back and reserve it
        for (int i = 0; i < orders.length; i++) {
            // orders[i] == x*y
            // find the object of the prodcut
            for (int j = 0; j < server.products.size(); j++) {
                if (server.products.get(i).getName().equalsIgnoreCase(orders[i].split("\\*")[1].trim())) {
                    if (server.products.get(i).getQuantity() < Integer.parseInt(orders[i].split("\\*")[0].trim())) {
                        return "error:Quantity is not available in product " + server.products.get(i).getName();
                    }
                }
            }
        }
        // if we reached until here it means that the order is valid and all quantites is found. so we reserve them.
        return (reserveOrderQuantites(order));
    }

    // this method will reserver the quantites it will do it one order by one. if one product quantity is not enough it will skip it and not cancle the whole order
    public String reserveOrderQuantites(String order) {

        String quantityNotEnough = "Missing:";

        // reserve all orders.
        // order format (x*y + x*y)
        // x == quantity
        // y == name of the order
        String[] orders = order.split("\\+");

        for (int i = 0; i < orders.length; i++) {
            // orders[i] == x*y
            // find the object of the prodcut
            for (int j = 0; j < server.products.size(); j++) {
                if (server.products.get(j).getName().equalsIgnoreCase(orders[i].split("\\*")[1].trim())) {
                    if (server.products.get(j).reserveQuantites(Integer.parseInt(orders[i].split("\\*")[0].trim()))) {
                        // quantity available and we did reserve it.
                        try {
                            server.theDB.updateProductQuantity(server.products.get(j).getId(), server.products.get(j).getQuantity());
                        } catch (SQLException ex) {
                            System.err.println("Error " + ex);
                        }
                    } else {
                        quantityNotEnough += server.products.get(j).getName() + " ";
                    }
                    break;
                }
            }
        }
        // something IS missing return what it was.
        if (!quantityNotEnough.equalsIgnoreCase("Missing:")) {
            return quantityNotEnough;
        }
        // else return ok.
        return "Ok";
    }

    // this method will unreserver the quantites
    // order will be like this
    public void unreserverOrderQuantites(String order) {
        String[] orders = order.split("\\+");
        for (int i = 0; i < orders.length; i++) {
            // orders[i] == x*y
            // find the object of the prodcut
            for (int j = 0; j < server.products.size(); j++) {
                if (server.products.get(j).getName().equalsIgnoreCase(orders[i].split("\\*")[1].trim())) {
                    server.products.get(j).unreserveQuantites(Integer.parseInt(orders[i].split("\\*")[0].trim()));
                    try {
                        server.theDB.updateProductQuantity(server.products.get(j).getId(), server.products.get(j).getQuantity());
                    } catch (SQLException ex) {
                        System.err.println("Error " + ex);
                    }
                }
            }
        }
    }

    public String sendOrderToKitchen(String order) {
        // check if order is valid first
        String orderReserveStatus = validateOrder(order);
        if (orderReserveStatus.startsWith("Ok")) {
            // send order to the kitchen socket
            // add the order to the server hash map of orders and table id
            int currentOrderNum = server.orderID;
            server.ordersWithTableID.put(currentOrderNum, this.tableNumber);
            server.orderID++;
            // send the order details to the kitchen print writer
            // table id # order # time # order ID
            PrintWriter wrt = server.theKitchen.wrt;
            LocalDateTime d = LocalDateTime.now();
            String formattedDate = d.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            wrt.println("newOrder#" + this.tableNumber + "#" + order + "#" + formattedDate + "#" + currentOrderNum);
            return "accepted:" + currentOrderNum;
        }
        return orderReserveStatus;
    }
    // some order got updated so send it's information to the kitchen

    // this method will recevie a line of prodcut whitch the user want's to update it will update it in the db in return the newly updated line so the kitchen can recevie it
    // information format as follows
    // X:Y:X
    // X == old quantity
    // Y == new quantity
    // Z == product name
    // only send the udpated quantity
    // example:
    // user order 5 pices and it was reserved for him
    // now user adjusted the quantity to 1
    // so only 1-5 will be sent == -4
    // so we can readd 4 pices to the quantity    
    public String updateOrder(String order) {

        // reserve all orders.
        // order format (x*y + x*y)
        // x == quantity
        // y == name of the order
        String[] orders = order.split("\\+");
        String newOrder = "Ok:";
        String missingSomething = "missing:";
        for (int i = 0; i < orders.length; i++) {
            // orders[i] == X:Y:X
            // find the object of the prodcut
            for (int j = 0; j < server.products.size(); j++) {
                if (server.products.get(j).getName().equalsIgnoreCase(orders[i].split("\\*")[2].trim())) {
                    int oldQuantity = Integer.parseInt(orders[i].split("\\*")[0]);
                    int newQuantity = Integer.parseInt(orders[i].split("\\*")[1]);
                    int newReserve = newQuantity - oldQuantity;
                    if (server.products.get(j).reserveQuantites(newReserve)) {
                        // quantity available and we did reserve it.
                        try {
                            server.theDB.updateProductQuantity(server.products.get(j).getId(), server.products.get(j).getQuantity());
                        } catch (SQLException ex) {
                            System.err.println("Error " + ex);
                        }
                    } else {
                        missingSomething += server.products.get(j).getName();
                    }
                    newOrder += newQuantity + "*" + server.products.get(j).getName() + "+";
                    break;
                }
            }
        }
        if (!missingSomething.equalsIgnoreCase("missing:")) {
            return missingSomething;
        }
        return newOrder;
    }

    public String sendUpdatedOrderToKitchen(String line) {
        String order = line.split(":")[0];
        String orderNumber = line.split(":")[1];
        String updateStatus = updateOrder(order);
        if (updateStatus.startsWith("Ok")) {
            PrintWriter wrt = server.theKitchen.wrt;
            wrt.println("updateOrder#" + this.tableNumber + "#" + updateStatus.replaceAll("Ok:", "") + "#" + "time" + "#" + orderNumber);
            return "accepted";
        }
        return updateStatus;
    }

    // client want's to delete some order. send the order to the kitchen
    public boolean RemoveOrderFromKitchen(String line) {
        String order = line.split(":")[0];
        String orderNumber = line.split(":")[1];
        unreserverOrderQuantites(order);
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
                    // check if table already registred with same id
                    if (server.tableConnections.get(tableNumber) != null) {
                        wrt.println("rejected:1:Table with the same number already exists!");
                        rejected = true;
                    }
                    wrt.println("accepted");
                } catch (NumberFormatException e) {
                    System.err.println("Error " + e);
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
                        // closing kitchen or table windows is handled here by closing the connection and removing it from the hashmap and array lists
                        server.connections.remove(this);
                        this.connection.close();
                        if (isKitchen) {
                            //stopped here
                            server.theKitchen = null;
                        } else {
                            server.tableConnections.remove(tableNumber);
                        }
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
                                String orderStatus = sendOrderToKitchen(line.replace("order:", ""));
                                if (orderStatus.startsWith("accepted:")) {
                                    wrt.println("accepted:" + orderStatus.split(":")[1]);
                                } else {
                                    // order was rejected for some reason
                                    wrt.println("rejected:" + orderStatus.split(":")[1]);
                                }
                                // tell all to update menu qunatites changed
                                server.updateProductsForALl();
                            } else if (line.startsWith("orderUpdate:")) {
                                // client want to update his order before it got approved
                                String status = sendUpdatedOrderToKitchen(line.replace("orderUpdate:", ""));
//                                if (status.startsWith("accepted:")) {
//                                    wrt.println("accepted:");
//                                } else {
//                                    // order was rejected for some reason
//                                    wrt.println("failed:" + status.split(":")[1]);
//                                }
                                // tell all to update menu qunatites changed
                                server.updateProductsForALl();
                            } else if (line.startsWith("orderDelete:")) {
                                // client want to remove his order before it got approved
                                RemoveOrderFromKitchen(line.replace("orderDelete:", ""));
                                // tell all to update menu qunatites changed
                                server.updateProductsForALl();
                            } else {
                                wrt.println("rejected:0:unknown operation");
                            }

                        } else {
                            // kitchen recevied some order and they want to change it's status
                            if (line.startsWith("orderStatusUpdate")) {
                                // orderStatusUpdate:X:Y
                                // X == status
                                // status : 0 means rejected
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
                                int time = Integer.parseInt(line.split(":")[4]);
                                server.theDB.addProduct(name, price, quantity, time);
                                // tell everyone to update their list
                                server.updateProductsForALl();
                            } else if (line.startsWith("updateProduct")) {
                                int id = Integer.parseInt(line.split(":")[1]);
                                String name = (line.split(":")[2]);
                                double price = Double.parseDouble(line.split(":")[3]);
                                int quantity = Integer.parseInt(line.split(":")[4]);
                                int time = Integer.parseInt(line.split(":")[5]);
                                server.theDB.updateProduct(id, name, price, quantity, time);
                                // tell everyone to update their list
                                server.updateProductsForALl();
                            } else if (line.startsWith("deleteProduct")) {
                                int id = Integer.parseInt(line.split(":")[1]);
                                server.theDB.removeProduct(id);
                                // tell everyone to update their list
                                server.updateProductsForALl();
                            }
                        }
                    }
                }
            } else {
                this.connection.close();
            }
        } catch (IOException ex) {
            System.err.println("Error " + ex);
        } catch (SQLException ex) {
            System.err.println("Error " + ex);
        }

    }

}
