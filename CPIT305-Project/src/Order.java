/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mjed
 */
public class Order {

    private int tableID;
    private int orderID;
    private product[] orders;
    private int status;

    public Order(int tableID, int orderID, product[] orders, int status) {
        this.tableID = tableID;
        this.orderID = orderID;
        this.orders = orders;
        this.status = status;
    }

}
