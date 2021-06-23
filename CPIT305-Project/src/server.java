
import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
            System.out.println("Connection recevied and thread was made for it...");
        }
    }
}

class connectionHandler extends Thread {

    Socket connection;
    boolean isKitchen;
    int tableNumber;

    public connectionHandler(Socket connection) {
        this.connection = connection;
    }

    public boolean sendOrderToKitchen(String order) {
        // send order to the socket .....
        // table id # order # time
        OutputStream os;
        try {
            os = server.theKitchen.connection.getOutputStream();
            PrintWriter wrt = new PrintWriter(os, true);
            wrt.println(this.tableNumber + "#" + order.replace("order:", "") + "#" + "time");
            return true;
        } catch (IOException ex) {
            System.out.println(ex);
            //Logger.getLogger(connectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public void run() {
        System.out.println("Recevied new connection");
        // start reading from the client

        // check the type of client.
        // if is kitchen and kitchen exists already reject
        // else accept
        // if is normal user and kitchen exist accept else reject or maybe but on hold.
        InputStream is;
        try {
            boolean rejected = false;
            is = connection.getInputStream();
            OutputStream os = connection.getOutputStream();
            Scanner scan = new Scanner(is);
            PrintWriter wrt = new PrintWriter(os, true);

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
                            //
                            // client want to get the list of products
                            // get it throught the db
                            ArrayList<product> products = server.theDB.getProducts();
                            // convert the products to something that we can send to the socket
                            // send it via object stream?
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
                            objectOutputStream.writeObject(products);

                        } catch (dbNotSettedUpException ex) {
                            wrt.println("failed:0:server error");
                            // print log
                        } catch (SQLException ex) {
                            wrt.println("failed:0:server error");
                            // print log
                        }

                    } else {
                        if (!isKitchen) {
                            if (line.startsWith("order")) {
                                if (sendOrderToKitchen(line)) {
                                    wrt.println("accepted");
                                } else {
                                    wrt.println("failed");
                                }
                            } else {
                                wrt.println("rejected:0:unknown operation");
                            }
                            // line is order to be sent to kitchen
                            // tables can send orders and expects answer from kitchen....
                            // read order
                            // send to kitchen?
                            // recevie answer from kitchen
                            // send it to table???
                        } else {
                            System.out.println("aaa");
                            // accepted, continue
                            //...
                            // kitchen will only recevie orders and products from us.
                            // something coming from kitchen socket... adding, edit, delete product
                            // critical section maybe?
                            wrt.println("recived");
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
