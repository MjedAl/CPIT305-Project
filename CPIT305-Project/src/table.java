
import java.awt.PopupMenu;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mjed
 */
// This thread will always keep waiting for updates from the server
class listenForServerUpdates extends Thread {

    private Socket connection;
    private Scanner scanner;
    private PrintWriter writer;
    private table tableGUI;

    public listenForServerUpdates(Socket connection, Scanner scanner, PrintWriter writer, table tableGUI) {
        this.connection = connection;
        this.scanner = scanner;
        this.writer = writer;
        this.tableGUI = tableGUI;
    }

    @Override
    public void run() {
        String line;
        System.out.println("Table is lisining for commands.");
        while (true) {

            // recevied new order.
            // newOrder # table id # order # time
            line = scanner.nextLine();
            String[] parts = line.split(":");
            System.out.println("We recevied the following command : " + line);

            if (line.startsWith("orderStatusUpdate")) {
                // some of our order status has been updated so we need to check witch one is it.
                int status = Integer.parseInt(parts[1]);
                int orderID = Integer.parseInt(parts[2]);
                for (int i = 0; i < tableGUI.orderPages.size(); i++) {
                    if (tableGUI.orderPages.get(i).orderNumber == orderID) {
                        tableGUI.orderPages.get(i).updateStatus(status);
                    }
                }
            } else if (line.startsWith("accepted")) {
                tableGUI.ServerReponse = line;
            } else if (line.startsWith("rejected")) {
                tableGUI.ServerReponse = line;
            } else if (line.startsWith("updateProducts")) {
                System.out.println("Okay updating list");
                // server wants us to udpate the products lsit
//                System.out.println("ready to take products");
//                try {
//                    ObjectInputStream objectInputStream = new ObjectInputStream(this.connection.getInputStream());
//                    this.tableGUI.setProducts((ArrayList<product>) objectInputStream.readObject());
//                    System.out.println("okay done");
//                    System.out.println(this.tableGUI.products.size());
//                } catch (IOException ex) {
//                    Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (ClassNotFoundException ex) {
//                    Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                System.out.println("Done?");

                writer.println("products");
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(this.connection.getInputStream());
                    this.tableGUI.products = (ArrayList<product>) objectInputStream.readObject();
                } catch (IOException ex) {
                    Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
                }

                this.tableGUI.refreshListView();
            }
        }
    }
}

public class table extends javax.swing.JFrame {

    /**
     * Creates new form table
     */
    public table() {
        initComponents();
    }

    ArrayList<product> products;
    private Socket connection;
    private Scanner scanner;
    private PrintWriter writer;
    private String tableID;
    // the products list
    private ArrayList<product> productsInCart = new ArrayList<product>();
    // obj of the cart class
    private tableCart cart;
    // keep track of current order list.
    public String ServerReponse = "";
    // ^ to keep track of server latest response

    // 
    public ArrayList<trackOrderPage> orderPages = new ArrayList<trackOrderPage>();

    public void addNewTrackPage(trackOrderPage page) {
        this.orderPages.add(page);
    }

    public void removeTrackPage(int orderNumber) {
        for (int i = 0; i < orderPages.size(); i++) {
            if (orderPages.get(i).orderNumber == orderNumber) {
                orderPages.remove(i);
                break;
            }
        }
    }

    public table(Socket connection, Scanner scanner, PrintWriter writer, String tableID) {
        this.connection = connection;
        this.scanner = scanner;
        this.writer = writer;
        this.tableID = tableID;
        initComponents();
        tableNumLabel.setText("Table number : " + tableID);
        // make obj for the cart
        cart = new tableCart(this, connection, scanner, writer);
        // make a thread that will wait for updates.
        refreshList();
        new listenForServerUpdates(connection, scanner, writer, this).start();
        this.setVisible(true);
    }

    public void setProducts(ArrayList<product> products) {
        this.products = products;
    }

//    public void refreshList() {
//        //ObjectInputStream objectInputStream = new ObjectInputStream(this.connection.getInputStream());
//        //this.products = (ArrayList<product>) objectInputStream.readObject();
//        System.out.println("kk");
//        DefaultTableModel model = (DefaultTableModel) productsTable.getModel();
//        // reset the table.
//        model.setRowCount(0);
//        for (int i = 0; i < this.products.size(); i++) {
//            // only add products that are available
//            if (this.products.get(i).getQuantity() > 0) {
//                model.addRow(new Object[]{this.products.get(i).getId(), this.products.get(i).getName(), this.products.get(i).getPrice()});
//            } else {
//                // product that just got update is not available, so check if it was in the cart remove it.
//                for (int j = 0; j < productsInCart.size(); j++) {
//                    if (productsInCart.get(j).getId() == this.products.get(i).getId()) {
//                        JOptionPane.showMessageDialog(null, "Sorry we removed the prouct " + productsInCart.get(j).getName() + " from your cart. it's not available anymore.", "Sorry", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//            }
//        }
//
//    }
    public void refreshListView() {
        DefaultTableModel model = (DefaultTableModel) productsTable.getModel();
        model.setRowCount(0);
        for (int i = 0; i < this.products.size(); i++) {
            // only add products that are available
            if (this.products.get(i).getQuantity() > 0) {
                model.addRow(new Object[]{this.products.get(i).getId(), this.products.get(i).getName(), this.products.get(i).getPrice()});
            } else {
                // product that just got update is not available, so check if it was in the cart remove it.
                for (int j = 0; j < productsInCart.size(); j++) {
                    if (productsInCart.get(j).getId() == this.products.get(i).getId()) {
                        productsInCart.remove(j);
                        cartBtn.setText("Cart (" + productsInCart.size() + ")");
                        JOptionPane.showMessageDialog(null, "Sorry we removed the prouct " + productsInCart.get(j).getName() + " from your cart. it's not available anymore.", "Sorry", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    public void refreshList() {
        // requsting the updated list from the db
        writer.println("products");
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(this.connection.getInputStream());
            this.products = (ArrayList<product>) objectInputStream.readObject();

            DefaultTableModel model = (DefaultTableModel) productsTable.getModel();
            for (int i = 0; i < this.products.size(); i++) {
                // only add products that are available
                if (this.products.get(i).getQuantity() > 0) {
                    model.addRow(new Object[]{this.products.get(i).getId(), this.products.get(i).getName(), this.products.get(i).getPrice()});
                } else {
                    // product that just got update is not available, so check if it was in the cart remove it.
                    for (int j = 0; j < productsInCart.size(); j++) {
                        if (productsInCart.get(j).getId() == this.products.get(i).getId()) {
                            JOptionPane.showMessageDialog(null, "Sorry we removed the prouct " + productsInCart.get(j).getName() + " from your cart. it's not available anymore.", "Sorry", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addToCart() {
        int[] productsIndxes = productsTable.getSelectedRows();
        for (int i = 0; i < productsIndxes.length; i++) {
            productsInCart.add(new product((Integer) productsTable.getValueAt(productsIndxes[i], 0),
                    (String) productsTable.getValueAt(productsIndxes[i], 1), (Double) productsTable.getValueAt(productsIndxes[i], 2), 1));
        }
        cartBtn.setText("Cart (" + productsInCart.size() + ")");
    }

    public void resetCart() {
        this.productsInCart = new ArrayList<product>();
        cartBtn.setText("Cart (0)");
    }

    public void updateCartBtn(String text) {
        cartBtn.setText(text);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableNumLabel = new javax.swing.JLabel();
        leaveBtn = new javax.swing.JButton();
        cartBtn = new javax.swing.JButton();
        addBtn = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        productsTable = new javax.swing.JTable();
        currentOrdersBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tableNumLabel.setText("Table number : ");

        leaveBtn.setText("Leave");
        leaveBtn.setToolTipText("");

        cartBtn.setText("Cart (0)");
        cartBtn.setToolTipText("");
        cartBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cartBtnActionPerformed(evt);
            }
        });

        addBtn.setText("Add");
        addBtn.setToolTipText("");
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        productsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Price"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollPane.setViewportView(productsTable);

        currentOrdersBtn.setText("Current Orders (0)");
        currentOrdersBtn.setToolTipText("");
        currentOrdersBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentOrdersBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tableNumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(leaveBtn)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(addBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(currentOrdersBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cartBtn)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(tableNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leaveBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentOrdersBtn)
                    .addComponent(cartBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        addToCart();
    }//GEN-LAST:event_addBtnActionPerformed

    private void cartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cartBtnActionPerformed
        if (productsInCart.size() == 0) {
            JOptionPane.showMessageDialog(null, "Pleae add some products first", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            this.cart.callAgain(productsInCart);
            this.setVisible(false);
        }
    }//GEN-LAST:event_cartBtnActionPerformed

    private void currentOrdersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentOrdersBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_currentOrdersBtnActionPerformed

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
            java.util.logging.Logger.getLogger(table.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(table.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(table.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(table.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new table().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton cartBtn;
    private javax.swing.JButton currentOrdersBtn;
    private javax.swing.JButton leaveBtn;
    private javax.swing.JTable productsTable;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel tableNumLabel;
    // End of variables declaration//GEN-END:variables

}
