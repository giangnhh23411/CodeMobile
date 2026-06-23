package com.huonggiang.models;

import java.io.Serializable;
import java.util.Date;

public class Order implements Serializable {

    private String orderId;
    private String customerId;
    private String employeeId;
    private Date orderDate;
    private OrderStatus status;

    public Order() {
    }

    public Order(String orderId, String customerId, String employeeId, Date orderDate) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.status = OrderStatus.COMPLETED;  // default
    }

    public Order(String orderId, String customerId, String employeeId, Date orderDate, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", orderDate=" + orderDate +
                ", status=" + status +
                '}';
    }
}
