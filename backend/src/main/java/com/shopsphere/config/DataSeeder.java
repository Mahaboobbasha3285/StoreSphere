package com.shopsphere.config;
import com.shopsphere.model.Product;
import com.shopsphere.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import java.math.BigDecimal;
import java.util.List;
@Configuration public class DataSeeder {
 @Bean CommandLineRunner seed(ProductRepository repo){return args->{
  if(repo.count()==0) repo.saveAll(List.of(
   new Product("Wireless Headphones","Comfortable over-ear headphones.",new BigDecimal("2499.00"),"Electronics","https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800",20),
   new Product("Everyday Backpack","Durable backpack for work and travel.",new BigDecimal("1599.00"),"Accessories","https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800",35),
   new Product("Minimal Desk Lamp","Warm adjustable desk lighting.",new BigDecimal("999.00"),"Home","https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800",15),
   new Product("Running Shoes","Lightweight everyday running shoes.",new BigDecimal("3299.00"),"Fashion","https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800",12)
  ));
 };}
}
