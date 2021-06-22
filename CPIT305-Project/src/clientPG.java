
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/*
    We will need a class for the kitchen where it will have a socket that will always listen for orders
    and other one to send products etc...
/**
 *
 * @author Mjed
 */
public class clientPG {

    public static void main(String[] args) throws IOException {
        // 1-
        Socket soc = new Socket("localhost", 1900);
        //2- 
        InputStream is = soc.getInputStream();
        OutputStream os = soc.getOutputStream();
        Scanner scan = new Scanner(is);
        PrintWriter wrt = new PrintWriter(os, true);
        Scanner scankb = new Scanner(System.in);
        //3-read and write
        String line;

        while (true) {
            System.out.print("Msg : ");
            line = scankb.nextLine();
            wrt.println(line);
            if (line.equalsIgnoreCase("bye")) {
                break;
            }
            line = scan.nextLine();
            System.out.println("> " + line);
            // if the server is ending the connection close it from our end as well
            if (line.startsWith("rejected")) {
                if (line.split(":")[1].equalsIgnoreCase("1")) {
                    soc.close();
                    break;
                }
            }
        }
        //4
        soc.close();
    }
}
