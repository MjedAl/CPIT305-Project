
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mjed
 */
class listenForOrders extends Thread {

    private Socket connection;
    private Scanner scanner;
    private PrintWriter writer;
    private kitchen kitchenGUI;

    public listenForOrders(kitchen kitchenGUI, Socket connection, Scanner scanner, PrintWriter writer) {
        this.kitchenGUI = kitchenGUI;
        this.connection = connection;
        this.scanner = scanner;
        this.writer = writer;
    }

    @Override
    public void run() {
        String line;
        System.out.println("Kitchen is lisining for commands.");
        while (true) {
            // recevied new order.
            // newOrder # table id # order # time
            line = scanner.nextLine();
            System.out.println("Command is : " + line);
            String[] orderDetails = line.split("\\#");
            DefaultTableModel model = (DefaultTableModel) kitchenGUI.ordersTable.getModel();
            if (orderDetails[0].equalsIgnoreCase("newOrder")) {
                model.addRow(new Object[]{orderDetails[1], orderDetails[2].replaceAll("\\+", "\n"), orderDetails[3], orderDetails[4], "Recevied"});
            } else if (orderDetails[0].equalsIgnoreCase("updateOrder")) {
                // we look for the row of the order that we want to update
                for (int i = 0; i < kitchenGUI.ordersTable.getRowCount(); i++) {
                    if (((String) kitchenGUI.ordersTable.getValueAt(i, 3)).equalsIgnoreCase(orderDetails[4])) {
                        // update the order
                        kitchenGUI.ordersTable.setValueAt(orderDetails[2].replaceAll("\\+", "\n"), i, 1);
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
                // 
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(kitchenGUI.connection.getInputStream());
                    this.kitchenGUI.setProducts((ArrayList<product>) objectInputStream.readObject());
                } catch (IOException ex) {
                    Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
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

        // request latest version of products
        writer.println("products");
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(this.connection.getInputStream());
            this.products = (ArrayList<product>) objectInputStream.readObject();
            this.menueGUI.setProducts(products);// needed?
            this.menueGUI.refreshList();
        } catch (IOException ex) {
            Logger.getLogger(kitchen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(kitchen.class.getName()).log(Level.SEVERE, null, ex);
        }
        // create a thread that will keep lisineing for orders.
        this.ordersThread = new listenForOrders(this, connection, scanner, writer);
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editMenuBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        ordersTable = new javax.swing.JTable();
        confirmOrderBtn = new javax.swing.JButton();
        confirmOrderBtn1 = new javax.swing.JButton();

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

        confirmOrderBtn1.setText("Reject order");
        confirmOrderBtn1.setToolTipText("");
        confirmOrderBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmOrderBtn1ActionPerformed(evt);
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
                        .addGap(0, 558, Short.MAX_VALUE)
                        .addComponent(editMenuBtn))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(confirmOrderBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(confirmOrderBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(confirmOrderBtn1)
                    .addComponent(confirmOrderBtn))
                .addContainerGap(109, Short.MAX_VALUE))
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
        // status : 2 means that the order is on it's way
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
                JOptionPane.showMessageDialog(null, "Order number " + ordersTable.getValueAt(selectedIndexes[i], 3) + " has already been approved", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_confirmOrderBtnActionPerformed

    private void confirmOrderBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmOrderBtn1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_confirmOrderBtn1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(kitchen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(kitchen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(kitchen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(kitchen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new kitchen().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton confirmOrderBtn;
    private javax.swing.JButton confirmOrderBtn1;
    private javax.swing.JButton editMenuBtn;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTable ordersTable;
    // End of variables declaration//GEN-END:variables
}
