
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
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
        while (true) {
            // recevied new order.
            // table id # order # time
            line = scanner.nextLine();
            System.out.println(line);
            String[] orderDetails = line.split("\\#");
            DefaultTableModel model = (DefaultTableModel) kitchenGUI.ordersTable.getModel();
            model.addRow(new Object[]{orderDetails[0], orderDetails[1], orderDetails[2]});
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

    private Socket connection;
    private Scanner scanner;
    private PrintWriter writer;
    private listenForOrders ordersThread;

    public kitchen(Socket connection, Scanner scanner, PrintWriter writer) {
        this.connection = connection;
        this.scanner = scanner;
        this.writer = writer;
        initComponents();
        this.setVisible(true);

        // create a thread that will keep lisineing for orders and give it the blah blah
        this.ordersThread = new listenForOrders(this, connection, scanner, writer);
        ordersThread.start();
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
                "Table number", "Order", "Order time"
            }
        ));
        jScrollPane1.setViewportView(ordersTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 302, Short.MAX_VALUE)
                        .addComponent(editMenuBtn))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editMenuBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void editMenuBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuBtnActionPerformed
        // TODO add your handling code here:
        System.out.println("okkk");
    }//GEN-LAST:event_editMenuBtnActionPerformed

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
    private javax.swing.JButton editMenuBtn;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTable ordersTable;
    // End of variables declaration//GEN-END:variables
}