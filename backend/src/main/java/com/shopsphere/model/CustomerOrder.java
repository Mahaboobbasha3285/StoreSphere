package com.shopsphere.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="customer_orders")
public class CustomerOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String customerName;
 @Column(nullable=false) private String email;
 @Column(nullable=false,length=1000) private String address;
 @Column(nullable=false,precision=20,scale=2) private BigDecimal total;
 @Column(nullable=false,length=64,unique=true) private String accessTokenHash;
 @Column(nullable=false) private LocalDateTime createdAt;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) private List<OrderLine> items=new ArrayList<>();
 protected CustomerOrder(){}
 public CustomerOrder(String name,String email,String address,BigDecimal total,String accessTokenHash){this.customerName=name;this.email=email;this.address=address;this.total=total;this.accessTokenHash=accessTokenHash;this.createdAt=LocalDateTime.now();}
 public void addItem(OrderLine line){items.add(line);line.setOrder(this);}
 public Long getId(){return id;} public String getCustomerName(){return customerName;} public String getEmail(){return email;}
 public String getAddress(){return address;} public BigDecimal getTotal(){return total;} public LocalDateTime getCreatedAt(){return createdAt;}
 public List<OrderLine> getItems(){return items;}
}
