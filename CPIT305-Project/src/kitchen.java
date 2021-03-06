
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mjed
 */
class listenForOrders extends Thread {

    private Scanner scanner;
    private PrintWriter writer;
    private kitchen kitchenGUI;

    public listenForOrders(kitchen kitchenGUI, Scanner scanner, PrintWriter writer) {
        this.kitchenGUI = kitchenGUI;
        this.scanner = scanner;
        this.writer = writer;
    }

    @Override
    public void run() {

        File f = new File("LogFiles\\Kitchen LogFile");
        f.mkdir();

        try {
            //create new file for all kitchen command
            FileOutputStream kitchenCommand = new FileOutputStream("LogFiles\\Kitchen LogFile\\Kitchen Command.txt", true);

            //create new file for all kitchen Error
            FileOutputStream kitchenError = new FileOutputStream("LogFiles\\Kitchen LogFile\\Kitchen Error.txt", true);

            //set the default output to new file to store all the command
            System.setOut(new PrintStream(kitchenCommand));

            //set the default output to new file to store all the Error message
            System.setErr(new PrintStream(kitchenError));
        } catch (FileNotFoundException ex) {
            System.err.println("Error " + ex);
        }
        String line;
        System.out.println("/////////////////////////////////////////");
        System.out.println("Kitchen is lisining for commands.");
        while (true) {
            if (!scanner.hasNextLine()) {
                break;
            }
            line = scanner.nextLine();
            System.out.println("Command is : " + line);
            String[] orderDetails = line.split("\\#");
            DefaultTableModel model = (DefaultTableModel) kitchenGUI.ordersTable.getModel();

            if (orderDetails[0].equalsIgnoreCase("newOrder")) {
                // recevied new order.
                // newOrder # table id # order # time 
                model.addRow(new Object[]{orderDetails[1], orderDetails[2].replaceAll("\\+", "\n "), orderDetails[3], orderDetails[4], "Recevied"});
            } else if (orderDetails[0].equalsIgnoreCase("updateOrder")) {
                // we look for the row of the order that we want to update
                for (int i = 0; i < kitchenGUI.ordersTable.getRowCount(); i++) {
                    if (((String) kitchenGUI.ordersTable.getValueAt(i, 3)).equalsIgnoreCase(orderDetails[4])) {
                        // update the order
                        kitchenGUI.ordersTable.setValueAt(orderDetails[2].replaceAll("\\+", "\n "), i, 1);
                        break;
                    }
                }
            } else if (orderDetails[0].equalsIgnoreCase("removeOrder")) {
                for (int i = 0; i < kitchenGUI.ordersTable.getRowCount(); i++) {
                    if (((String) kitchenGUI.ordersTable.getValueAt(i, 3)).equalsIgnoreCase(orderDetails[1])) {
                        // remove the order
                        model.removeRow(i);
                        break;
                    }
                }
            } else if (orderDetails[0].equalsIgnoreCase("updateProducts")) {
                //server wants us to update the products list
                System.out.println("Server wants us to update the products list.. okay requesting latest version..");
                writer.println("products");
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(kitchenGUI.connection.getInputStream());
                    this.kitchenGUI.setProducts((ArrayList<product>) objectInputStream.readObject());
                } catch (IOException ex) {
                     System.err.println("Error " + ex);
                } catch (ClassNotFoundException ex) {
                    System.err.println("Error " + ex);
                }
                // refresh the table in the view
                this.kitchenGUI.refreshList();
            }
        }
    }
}

public class kitchen extends javax.swing.JFrame {

    /**
     * Creates new form kitchen
     */
    public kitchen() {
        initComponents();
    }

    public Socket connection;
    private Scanner scanner;
    private PrintWriter writer;
    private listenForOrders ordersThread;
    private menu menueGUI;
    ArrayList<product> products;

    public kitchen(Socket connection, Scanner scanner, PrintWriter writer) {
        this.connection = connection;
        this.scanner = scanner;
        this.writer = writer;
        initComponents();
        // create a the class for the menu GUI
        this.menueGUI = new menu(connection, scanner, writer, products);
        // handling window closing event
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                // on close write exit so it will be handled by the server
                // TODO tell to close all orders first
                writer.println("exit");
                System.out.println("Kitchen is closed!");
                dispose();
                System.exit(0);
            }
        });

        // request latest version of products
        writer.println("products");
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(this.connection.getInputStream());
            this.products = (ArrayList<product>) objectInputStream.readObject();
            this.menueGUI.setProducts(products);
            this.menueGUI.refreshList();
        } catch (IOException ex) {
             System.err.println("Error " + ex);
        } catch (ClassNotFoundException ex) {
             System.err.println("Error " + ex);
        }
        // create a thread that will keep lisineing for orders.
        this.ordersThread = new listenForOrders(this, scanner, writer);
        ordersThread.start();
        this.setVisible(true);
    }

    public void setProducts(ArrayList<product> products) {
        this.products = products;
    }

    public void refreshList() {
        this.menueGUI.setProducts(products);
        this.menueGUI.refreshList();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editMenuBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        ordersTable = new javax.swing.JTable();
        confirmOrderBtn = new javax.swing.JButton();
        orderReadyBtn = new javax.swing.JButton();
        rejectOrderBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        editMenuBtn.setText("Edit menu");
        editMenuBtn.setToolTipText("");
        editMenuBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuBtnActionPerformed(evt);
            }
        });

        ordersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Table number", "Order", "Order time", "Order ID", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(ordersTable);

        confirmOrderBtn.setText("Confirm order");
        confirmOrderBtn.setToolTipText("");
        confirmOrderBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmOrderBtnActionPerformed(evt);
            }
        });

        orderReadyBtn.setText("Order ready");
        orderReadyBtn.setToolTipText("");
        orderReadyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderReadyBtnActionPerformed(evt);
            }
        });

        rejectOrderBtn.setText("Reject order");
        rejectOrderBtn.setToolTipText("");
        rejectOrderBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rejectOrderBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(editMenuBtn))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1093, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rejectOrderBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(confirmOrderBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderReadyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editMenuBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(orderReadyBtn)
                    .addComponent(confirmOrderBtn)
                    .addComponent(rejectOrderBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void editMenuBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuBtnActionPerformed
        this.menueGUI.setVisible(true);
    }//GEN-LAST:event_editMenuBtnActionPerformed

    private void confirmOrderBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmOrderBtnActionPerformed

        // check selected status of recevied then we can aprove it        
        // if aproved or above print already approved
        // format : orderStatusUpdate:X:Y
        // X == status
        // status : 1 means aproved and they are working on it
        // status : 2 means that the order is ready
        // Y == order number
        // resp either confirmed or not.
        // get selected id and sent to server
        int[] selectedIndexes = ordersTable.getSelectedRows();
        for (int i = 0; i < selectedIndexes.length; i++) {
            //
            if (((String) ordersTable.getValueAt(selectedIndexes[i], 4)).equalsIgnoreCase("Recevied")) {
                // we can approve it
                String orderID = (String) ordersTable.getValueAt(selectedIndexes[i], 3);
                writer.println("orderStatusUpdate:1:" + orderID);
                ordersTable.setValueAt("Confirmed", selectedIndexes[i], 4);
                //
            } else {
                JOptionPane.showMessageDialog(null, "Order number " + ordersTable.getValueAt(selectedIndexes[i], 3) + " has already been confirmed or is rejected", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_confirmOrderBtnActionPerformed

    private void orderReadyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderReadyBtnActionPerformed
        int[] selectedIndexes = ordersTable.getSelectedRows();
        for (int i = 0; i < selectedIndexes.length; i++) {
            if (((String) ordersTable.getValueAt(selectedIndexes[i], 4)).equalsIgnoreCase("Confirmed")) {
                // we can approve it
                String orderID = (String) ordersTable.getValueAt(selectedIndexes[i], 3);
                writer.println("orderStatusUpdate:2:" + orderID);
                ordersTable.setValueAt("Ready", selectedIndexes[i], 4);
                // TODO remove the row
            } else if (((String) ordersTable.getValueAt(selectedIndexes[i], 4)).equalsIgnoreCase("Recevied")) {
                JOptionPane.showMessageDialog(null, "Order number " + ordersTable.getValueAt(selectedIndexes[i], 3) + " need to be confirmed first", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (((String) ordersTable.getValueAt(selectedIndexes[i], 4)).equalsIgnoreCase("Rejected")) {
                JOptionPane.showMessageDialog(null, "Order number " + ordersTable.getValueAt(selectedIndexes[i], 3) + " is rejected", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (((String) ordersTable.getValueAt(selectedIndexes[i], 4)).equalsIgnoreCase("Ready")) {
                JOptionPane.showMessageDialog(null, "Order number " + ordersTable.getValueAt(selectedIndexes[i], 3) + " is already Ready", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_orderReadyBtnActionPerformed

    private void rejectOrderBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectOrderBtnActionPerformed
        int[] selectedIndexes = ordersTable.getSelectedRows();
        for (int i = 0; i < selectedIndexes.length; i++) {
            //
            if (((String) ordersTable.getValueAt(selectedIndexes[i], 4)).equalsIgnoreCase("Recevied")) {
                // we can reject it
                String orderID = (String) ordersTable.getValueAt(selectedIndexes[i], 3);
                writer.println("orderStatusUpdate:0:" + orderID);
                ordersTable.setValueAt("Rejected", selectedIndexes[i], 4);
            } else {
                JOptionPane.showMessageDialog(null, "Order number " + ordersTable.getValueAt(selectedIndexes[i], 3) + " cannot be rejected now", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_rejectOrderBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton confirmOrderBtn;
    private javax.swing.JButton editMenuBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton orderReadyBtn;
    public javax.swing.JTable ordersTable;
    private javax.swing.JButton rejectOrderBtn;
    // End of variables declaration//GEN-END:variables
}
