
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Mjed
 */
public class trackOrderPage extends javax.swing.JFrame {

    private ArrayList<product> productsInCart;
    private table theTable;
    private Socket connection;
    private Scanner scanner;
    private PrintWriter writer;
    public int orderNumber;
    private int orderStatusNum;

    // this method will be accessed from the table class to update the order status.
    public void updateStatus(int status) {
        this.orderStatusNum = status;
        // order got aprovved we must stop user from editing the order.
        if (status == 0) {
            editOrderPanel.setVisible(false);
            orderStatus.setText("Order rejected :(");
            statusProgressbar.setValue(-1);
            // tell server to return reserved items
            String productsStr = "";
            for (int i = 0; i < productsInCart.size(); i++) {
                productsStr += productsInCart.get(i).getRequiredQuantity() + "*" + productsInCart.get(i).getName() + "+";
            }
            writer.println("orderDelete:" + productsStr + ":" + this.orderNumber);
        } else if (status == 1) {
            editOrderPanel.setVisible(false);
            orderStatus.setText("Order in-progress");
            statusProgressbar.setValue(1);

            // order got aproved so stop the editable from being editied
            orderTable.setDefaultEditor(Object.class, null);
            // calculate estimated time
            int totalMinitues = 0;
            for (int i = 0; i < this.productsInCart.size(); i++) {
                totalMinitues += this.productsInCart.get(i).getEstimatedTimeInMintiues();
            }
            int avgTime = totalMinitues / this.productsInCart.size();
            eta.setText("Estimated time:" + avgTime + " Mintiues");
        } else if (status == 2) {
            orderStatus.setText("Order is coming for you :)");
            statusProgressbar.setValue(2);
        }
    }

    public trackOrderPage(ArrayList<product> productsInCart, table theTable, Socket connection, Scanner scanner, PrintWriter writer, int orderNumber) {
        this.productsInCart = productsInCart;
        this.theTable = theTable;
        this.connection = connection;
        this.scanner = scanner;
        this.writer = writer;
        this.orderNumber = orderNumber;
        initComponents();
        this.setVisible(true);
        // load products to the table
        DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
        model.setRowCount(0);
        for (int i = 0; i < this.productsInCart.size(); i++) {
            model.addRow(new Object[]{this.productsInCart.get(i).getId(), this.productsInCart.get(i).getName(), this.productsInCart.get(i).getPrice(), this.productsInCart.get(i).getRequiredQuantity()});
        }
        orderIdText.setText("Order id: " + orderNumber);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        orderIdText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        orderTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        editOrderPanel = new javax.swing.JPanel();
        removeOrderBtn = new javax.swing.JButton();
        removeProductBtn = new javax.swing.JButton();
        saveChangeBtn = new javax.swing.JButton();
        statusProgressbar = new javax.swing.JProgressBar();
        orderStatus = new javax.swing.JLabel();
        eta = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        orderIdText.setText("Order ID: ");

        orderTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Price", "Quantity"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class
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
        jScrollPane1.setViewportView(orderTable);

        jLabel2.setText("Order status:");

        removeOrderBtn.setText("Cancel Order");
        removeOrderBtn.setToolTipText("");
        removeOrderBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeOrderBtnActionPerformed(evt);
            }
        });

        removeProductBtn.setText("Remove product");
        removeProductBtn.setToolTipText("");
        removeProductBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProductBtnActionPerformed(evt);
            }
        });

        saveChangeBtn.setText("Send changes");
        saveChangeBtn.setToolTipText("");
        saveChangeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveChangeBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editOrderPanelLayout = new javax.swing.GroupLayout(editOrderPanel);
        editOrderPanel.setLayout(editOrderPanelLayout);
        editOrderPanelLayout.setHorizontalGroup(
            editOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editOrderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(removeOrderBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeProductBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveChangeBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        editOrderPanelLayout.setVerticalGroup(
            editOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editOrderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(removeOrderBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeProductBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveChangeBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statusProgressbar.setMaximum(2);
        statusProgressbar.setMinimum(-1);
        statusProgressbar.setToolTipText("");

        orderStatus.setText("Waiting Approval");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(statusProgressbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(orderStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(eta, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editOrderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(189, 189, 189)
                        .addComponent(orderIdText, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(orderIdText)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(12, 12, 12)
                        .addComponent(orderStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusProgressbar, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eta))
                    .addComponent(editOrderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void removeOrderBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeOrderBtnActionPerformed
        // 0 == waiting for aproval so user can edit and cancel order
        if (orderStatusNum == 0) {
            //
            String productsStr = "";
            for (int i = 0; i < productsInCart.size(); i++) {
                productsStr += productsInCart.get(i).getRequiredQuantity() + "*" + productsInCart.get(i).getName() + "+";
            }
            writer.println("orderDelete:" + productsStr + ":" + this.orderNumber);
            JOptionPane.showMessageDialog(null, "Your order was deleted :(", "Okay", JOptionPane.ERROR_MESSAGE);
            this.theTable.removeTrackPage(orderNumber);
            dispose();
        }
    }//GEN-LAST:event_removeOrderBtnActionPerformed

    public void setProductsInCart(ArrayList<product> productsInCart) {
        this.productsInCart = productsInCart;
    }

    private void saveChangeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveChangeBtnActionPerformed
        // update the products quantity
        if (orderStatusNum == 0) {
            //save the updated quantity
            String productsStr = "orderUpdate:";

            for (int i = 0; i < orderTable.getRowCount(); i++) {
                // search for the object of the prodcut
                for (int j = 0; j < this.productsInCart.size(); j++) {
                    // found the obj
                    if (this.productsInCart.get(j).getId() == (Integer) orderTable.getValueAt(i, 0)) {

                        if ((Integer) orderTable.getValueAt(i, 3) < 0) {
                            JOptionPane.showMessageDialog(null, "Quantity can't be less than 0", "Rejected", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if ((Integer) orderTable.getValueAt(i, 3) == 0) {
                            JOptionPane.showMessageDialog(null, "Please remove the product instead of putting 0", "Rejected", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        // user wants quantity that's bigger than the available
                        // if new quantiy - old quantity > the avaialble quantity
                        // why? because the old quanttiy is already reservered for this order so we don't need to see if it's available again :)
                        if (((Integer) orderTable.getValueAt(i, 3) - this.productsInCart.get(j).getRequiredQuantity()) > this.productsInCart.get(j).getQuantity()) {
                            // print msg
                            JOptionPane.showMessageDialog(null, "Product " + productsInCart.get(j).getName() + " only has " + productsInCart.get(j).getQuantity() + " more in stock", "Rejected", JOptionPane.ERROR_MESSAGE);
                            orderTable.setValueAt(this.productsInCart.get(j).getRequiredQuantity(), i, 3);
                            return;
                        } else {
                            // old quantity * new quantity * product name
                            productsStr += productsInCart.get(j).getRequiredQuantity() + "*" + orderTable.getValueAt(i, 3) + "*" + orderTable.getValueAt(i, 1) + "+";
                            this.productsInCart.get(j).setRequiredQuantity((Integer) orderTable.getValueAt(i, 3));
                        }
                    }
                }
            }
            writer.println(productsStr + ":" + this.orderNumber);
            JOptionPane.showMessageDialog(null, "Your order update was sent", "Okay", JOptionPane.DEFAULT_OPTION);
        }
    }//GEN-LAST:event_saveChangeBtnActionPerformed

    private void removeProductBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeProductBtnActionPerformed
        JOptionPane.showMessageDialog(null, "TODO", "Info", JOptionPane.DEFAULT_OPTION);
        // FIX IT LATER
//        int[] productsIndxes = orderTable.getSelectedRows();
//        if (productsInCart.size() == 1) {
//            JOptionPane.showMessageDialog(null, "You only have one product. your order will be canceled", "Info", JOptionPane.DEFAULT_OPTION);
//            String productsStr = "";
//            for (int i = 0; i < productsInCart.size(); i++) {
//                productsStr += productsInCart.get(i).getRequiredQuantity() + "*" + productsInCart.get(i).getName() + "+";
//            }
//            writer.println("orderDelete:" + productsStr + ":" + this.orderNumber);
//            JOptionPane.showMessageDialog(null, "Your order was deleted :(", "Okay", JOptionPane.ERROR_MESSAGE);
//            this.theTable.removeTrackPage(orderNumber);
//            dispose();
//        } else {
//            // user has multiple orders, only delete the selected
//            DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
//            for (int i = 0; i < productsIndxes.length; i++) {
//                productsInCart.remove(productsIndxes[i]);
//                model.removeRow(productsIndxes[i]);
//            }
//        }

        // send the update order to the kitchen

    }//GEN-LAST:event_removeProductBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel editOrderPanel;
    private javax.swing.JLabel eta;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel orderIdText;
    private javax.swing.JLabel orderStatus;
    private javax.swing.JTable orderTable;
    private javax.swing.JButton removeOrderBtn;
    private javax.swing.JButton removeProductBtn;
    private javax.swing.JButton saveChangeBtn;
    private javax.swing.JProgressBar statusProgressbar;
    // End of variables declaration//GEN-END:variables

    // TODO, maybe remove this?
    void updateProductsQuantites(ArrayList<product> products) {
        // loop through all products in cart
        for (int i = 0; i < this.productsInCart.size(); i++) {
            // search for the object in the full list
            for (int j = 0; j < products.size(); j++) {
                if (productsInCart.get(i).getId() == products.get(j).getId()) {
                    productsInCart.get(i).setQuantity(products.get(j).getQuantity());
                    break;
                }
            }
        }
    }
}
