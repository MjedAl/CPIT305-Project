
import java.awt.PopupMenu;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.WindowConstants;
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
            if(!scanner.hasNextLine()){
                break;
            }
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
                tableGUI.cart.orderResponse(line);
            } else if (line.startsWith("rejected")) {
                tableGUI.cart.orderResponse(line);
            } else if (line.startsWith("updateProducts")) {
                // server wants us to udpate the products lsit
                System.out.println("Okay updating list");
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
    ArrayList<product> products;
    private Socket connection;
    private Scanner scanner;
    private PrintWriter writer;
    private String tableID;
    // the products list
    private ArrayList<product> productsInCart = new ArrayList<product>();
    // obj of the cart class
    public tableCart cart;
    // keep track of current order list.

    public ArrayList<trackOrderPage> orderPages = new ArrayList<trackOrderPage>();

    public void addNewTrackPage(trackOrderPage page) {
        this.orderPages.add(page);
        currentOrdersBtn.setText("Current Orders (" + orderPages.size() + ")");
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
        
        // handling window closing event
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                // on close write exit so it will be handled by the server
                writer.println("exit");
                System.out.println("Table checkout.");
                dispose();
                System.exit(0);
            }
        });

        // requsting the updated list from the db
        writer.println("products");
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(this.connection.getInputStream());
            products = (ArrayList<product>) objectInputStream.readObject();
            refreshListView();
        } catch (IOException ex) {
            Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(table.class.getName()).log(Level.SEVERE, null, ex);
        }

        new listenForServerUpdates(connection, scanner, writer, this).start();
        this.setVisible(true);
    }

    public void setProducts(ArrayList<product> products) {
        this.products = products;
    }

    // update the table view from the products arraylist.
    public void refreshListView() {
        DefaultTableModel model = (DefaultTableModel) productsTable.getModel();
        model.setRowCount(0);
        for (int i = 0; i < this.products.size(); i++) {
            // only add products that are available to the table
            if (this.products.get(i).getQuantity() > 0) {
                model.addRow(new Object[]{this.products.get(i).getId(), this.products.get(i).getName(), this.products.get(i).getPrice()});
            }
        }
        // compare the products that just got updated with the products in cart.
        // to update the products infomration that's in the cart and to see if a product was removed
        for (int i = 0; i < productsInCart.size(); i++) {
            boolean found = false;
            for (int j = 0; j < products.size(); j++) {
                if (productsInCart.get(i).getId() == products.get(j).getId()) {
                    // remove the existing product in the cart and add a new one with the updated information.
                    products.get(j).setRequiredQuantity(productsInCart.get(i).getRequiredQuantity());
                    productsInCart.add(products.get(j));
                    productsInCart.remove(i);
                    found = true;
                    break;
                }
            }
            // reached the end and a product in the cart is not found in the updated list == the product got removed
            if (!found) {
                JOptionPane.showMessageDialog(null, "Product " + productsInCart.get(i).getName() + " was removed from the menu. we will delete it from the cart.", "Whoops", JOptionPane.ERROR_MESSAGE);
                productsInCart.remove(i);
                cartBtn.setText("Cart (" + productsInCart.size() + ")");
            }
        }
        // update the list for the cart
        this.cart.setProductsInCart(productsInCart);
        this.cart.redrawTable();
//         update the list for all the tracking page (user is not tracking page and he did not confirm so he can change the quantites)
//         TODO
        for (int i = 0; i < this.orderPages.size(); i++) {
            this.orderPages.get(i).updateProductsQuantites(products);
        }
    }

    private void addToCart() {
        int[] productsIndxes = productsTable.getSelectedRows();
        for (int i = 0; i < productsIndxes.length; i++) {

            // first check if product already exists in the cart
            boolean exitsInCart = false;
            for (int j = 0; j < productsInCart.size(); j++) {
                // same ID
                if (productsInCart.get(j).getId() == (Integer) productsTable.getValueAt(productsIndxes[i], 0)) {
                    int wantedQ = productsInCart.get(j).getRequiredQuantity() + 1;
                    int availableQ = productsInCart.get(j).getQuantity();
                    if (wantedQ > availableQ) {
                        JOptionPane.showMessageDialog(null, "Product " + productsInCart.get(j).getName() + " only has " + productsInCart.get(j).getQuantity() + " in stock", "Rejected", JOptionPane.ERROR_MESSAGE);
                    } else {
                        productsInCart.get(j).setRequiredQuantity(wantedQ);
                    }
                    exitsInCart = true;
                    break;
                }
            }
            if (!exitsInCart) {
                // get the id of the product from the table and add it to the cart
                int productID = (Integer) productsTable.getValueAt(productsIndxes[i], 0);
                // search for the object of the prodcut
                for (int j = 0; j < this.products.size(); j++) {
                    if (this.products.get(j).getId() == productID) {
                        productsInCart.add(this.products.get(j));
                        break;
                    }
                }
            }
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
            JOptionPane.showMessageDialog(null, "Please add some products first", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            this.cart.setProductsInCart(productsInCart);
            this.cart.redrawTable();
            this.cart.setVisible(true);
            this.setVisible(false);
        }
    }//GEN-LAST:event_cartBtnActionPerformed

    private void currentOrdersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentOrdersBtnActionPerformed

        for (int i = 0; i < orderPages.size(); i++) {
            orderPages.get(i).setVisible(true);
        }
    }//GEN-LAST:event_currentOrdersBtnActionPerformed

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
