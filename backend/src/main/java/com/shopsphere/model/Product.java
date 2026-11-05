package com.shopsphere.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="products")
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=140) private String name;
 @Column(nullable=false,length=2000) private String description;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal price;
 @Column(nullable=false,length=80) private String category;
 @Column(length=1000) private String imageUrl;
 @Column(nullable=false) private Integer stock;
 protected Product() {}
 public Product(String name,String description,BigDecimal price,String category,String imageUrl,Integer stock) {
  this.name=name; this.description=description; this.price=price; this.category=category; this.imageUrl=imageUrl; this.stock=stock;
 }
 public Long getId(){return id;} public String getName(){return name;} public String getDescription(){return description;}
 public BigDecimal getPrice(){return price;} public String getCategory(){return category;} public String getImageUrl(){return imageUrl;}
 public Integer getStock(){return stock;}
 public void update(String n,String d,BigDecimal p,String c,String image,Integer s){name=n;description=d;price=p;category=c;imageUrl=image;stock=s;}
 public void reduceStock(int quantity){if(quantity<1 || stock<quantity) throw new IllegalArgumentException("Insufficient stock for "+name); stock-=quantity;}
}
