
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mjed
 */
public class tableCart extends javax.swing.JFrame {

    private ArrayList<product> productsInCart;
    private table theTable;
    private Socket connection;
    private Scanner scanner;
    private PrintWriter writer;

    public tableCart(table theTable, Socket connection, Scanner scanner, PrintWriter writer) {
        this.theTable = theTable;
        this.connection = connection;
        this.scanner = scanner;
        this.writer = writer;
        initComponents();
    }

    public void setProductsInCart(ArrayList<product> productsInCart) {
        this.productsInCart = productsInCart;
    }

    public void redrawTable() {
        DefaultTableModel model = (DefaultTableModel) productsTable.getModel();
        model.setRowCount(0);
        for (int i = 0; i < this.productsInCart.size(); i++) {
            model.addRow(new Object[]{this.productsInCart.get(i).getId(), this.productsInCart.get(i).getName(), this.productsInCart.get(i).getPrice(), this.productsInCart.get(i).getRequiredQuantity()});
        }
        calcuatePrice();
    }

    private void calcuatePrice() {
        double total = 0;
        for (product item : productsInCart) {
            total += item.getRequiredQuantity() * item.getPrice();
        }
        totalPrice.setText("Total price: " + total);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableNumLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        productsTable = new javax.swing.JTable();
        sendOrderBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();
        totalPrice = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tableNumLabel.setText("Your cart:");

        productsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Price", "Quantity"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollPane.setViewportView(productsTable);

        sendOrderBtn.setText("Send order");
        sendOrderBtn.setToolTipText("");
        sendOrderBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendOrderBtnActionPerformed(evt);
            }
        });

        removeBtn.setText("Remove");
        removeBtn.setToolTipText("");
        removeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBtnActionPerformed(evt);
            }
        });

        backBtn.setText("Back");
        backBtn.setToolTipText("");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });

        totalPrice.setText("Total price:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sendOrderBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .addComponent(removeBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(backBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tableNumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(totalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tableNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(backBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 131, Short.MAX_VALUE)
                .addComponent(removeBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalPrice)
                .addGap(29, 29, 29)
                .addComponent(sendOrderBtn)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(48, 48, 48)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(125, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void orderResponse(String response) {
        System.out.println("We got response from server");
        int orderNumber = -1;
        if (response.startsWith("accepted")) {
            JOptionPane.showMessageDialog(null, "Your order was sent :)", "Accepted", JOptionPane.DEFAULT_OPTION);
            orderNumber = Integer.parseInt(response.split(":")[1]);
            // open tracking page for the order
            // open the order page
            theTable.setVisible(true);
            trackOrderPage orderPage = new trackOrderPage(productsInCart, theTable, connection, scanner, writer, orderNumber);
            // save the tracking page to the main page
            theTable.addNewTrackPage(orderPage);
            // reset the cart
            theTable.resetCart();
            dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Your order was rejected :(", "Rejected", JOptionPane.ERROR_MESSAGE);
            // show rejection reaseon
            // redirect to home page
            dispose();
            theTable.setVisible(true);
        }
        sendOrderBtn.setEnabled(true);
    }

    private void sendOrderBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendOrderBtnActionPerformed
        if (productsInCart.size() == 0) {
            JOptionPane.showMessageDialog(null, "Pleae add some products first", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // first update the products in cart from the table, maybe user changed the quantity.
            String productsStr = "order:";

            for (int i = 0; i < productsTable.getRowCount(); i++) {
                // search for the object of the prodcut
                for (int j = 0; j < this.productsInCart.size(); j++) {
                    // found the obj
                    if (this.productsInCart.get(j).getId() == (Integer) productsTable.getValueAt(i, 0)) {
                        if ((Integer) productsTable.getValueAt(i, 3) < 0) {
                            JOptionPane.showMessageDialog(null, "Quantity can't be less than 0", "Rejected", JOptionPane.ERROR_MESSAGE);
                            return;
                        }                        // user wants quantity that's bigger than the available
                        if ((Integer) productsTable.getValueAt(i, 3) > this.productsInCart.get(j).getQuantity()) {
                            // print msg
                            JOptionPane.showMessageDialog(null, "Product " + productsInCart.get(j).getName() + " only has " + productsInCart.get(j).getQuantity() + " in stock", "Rejected", JOptionPane.ERROR_MESSAGE);
                            return;
                        } else {
                            this.productsInCart.get(j).setRequiredQuantity((Integer) productsTable.getValueAt(i, 3));
                        }
                    }
                }
                productsStr += productsTable.getValueAt(i, 3) + "*" + productsTable.getValueAt(i, 1) + "+";
            }
            System.out.println("Sending order: " + productsStr);

            writer.println(productsStr);

            sendOrderBtn.setEnabled(false);
        }

    }//GEN-LAST:event_sendOrderBtnActionPerformed

    private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBtnActionPerformed
        // remove selected id from list, update cart label
        int[] productsIndxes = productsTable.getSelectedRows();
        DefaultTableModel model = (DefaultTableModel) productsTable.getModel();

        for (int i = 0; i < productsIndxes.length; i++) {
            // remove the selected product from the array list
            // first we need to find the product in the cart
            //
            productsInCart.remove(productsIndxes[i]);
            // remove from the view
            model.removeRow(productsIndxes[i]);

        }
        calcuatePrice();
        this.theTable.updateCartBtn("Cart (" + productsInCart.size() + ")");
    }//GEN-LAST:event_removeBtnActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        dispose();
        theTable.setVisible(true);
    }//GEN-LAST:event_backBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JTable productsTable;
    private javax.swing.JButton removeBtn;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton sendOrderBtn;
    private javax.swing.JLabel tableNumLabel;
    private javax.swing.JLabel totalPrice;
    // End of variables declaration//GEN-END:variables
}
