package com.shopsphere.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
@Entity @Table(name="order_lines")
public class OrderLine {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @JsonIgnore @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false) private CustomerOrder order;
 @Column(nullable=false) private Long productId;
 @Column(nullable=false) private String productName;
 @Column(nullable=false) private Integer quantity;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal unitPrice;
 protected OrderLine(){}
 public OrderLine(Long id,String name,Integer quantity,BigDecimal price){this.productId=id;this.productName=name;this.quantity=quantity;this.unitPrice=price;}
 public void setOrder(CustomerOrder order){this.order=order;}
 public Long getId(){return id;} public Long getProductId(){return productId;} public String getProductName(){return productName;}
 public Integer getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;}
}
