package com.shopsphere.service;
import com.shopsphere.dto.*;
import com.shopsphere.model.*;
import com.shopsphere.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.shopsphere.dto.OrderConfirmation;
import java.util.HexFormat;
@Service
public class StoreService {
 private final ProductRepository products; private final OrderRepository orders;
 public StoreService(ProductRepository products,OrderRepository orders){this.products=products;this.orders=orders;}
 public List<Product> listProducts(String q,String category){
  List<Product> result=(q!=null&&!q.isBlank())?products.findByNameContainingIgnoreCase(q.trim()):
   (category!=null&&!category.isBlank())?products.findByCategoryIgnoreCase(category.trim()):products.findAll();
  if(q!=null&&!q.isBlank()&&category!=null&&!category.isBlank()) result=result.stream().filter(p->p.getCategory().equalsIgnoreCase(category.trim())).toList();
  return result;
 }
 public Product getProduct(Long id){return products.findById(id).orElseThrow(()->new ResourceNotFoundException("Product not found: "+id));}
 public Product createProduct(ProductRequest r){return products.save(new Product(r.name(),r.description(),r.price(),r.category(),r.imageUrl(),r.stock()));}
 @Transactional public Product updateProduct(Long id,ProductRequest r){
  Product p=products.findByIdForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Product not found: "+id));
  p.update(r.name(),r.description(),r.price(),r.category(),r.imageUrl(),r.stock());
  return products.save(p);
 }
 @Transactional public void deleteProduct(Long id){
  Product p=products.findByIdForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Product not found: "+id));
  products.delete(p);
 }
 @Transactional public OrderConfirmation placeOrder(OrderRequest r){
  // Merge repeated product IDs before locking, preventing duplicate lines and double-lock surprises.
  java.util.Map<Long,Integer> quantities=new java.util.TreeMap<>();
  for(OrderRequest.Item item:r.items()) quantities.merge(item.productId(),item.quantity(),Integer::sum);
  BigDecimal total=BigDecimal.ZERO;
  java.util.List<Product> lockedProducts=new java.util.ArrayList<>();
  for(var entry:quantities.entrySet()){
   Product product=products.findByIdForUpdate(entry.getKey()).orElseThrow(()->new ResourceNotFoundException("Product not found: "+entry.getKey()));
   product.reduceStock(entry.getValue());
   total=total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
   lockedProducts.add(product);
  }
  String accessToken=UUID.randomUUID().toString()+UUID.randomUUID().toString();
  CustomerOrder order=new CustomerOrder(r.customerName(),r.email(),r.address(),total,sha256(accessToken));
  for(Product product:lockedProducts){
   int quantity=quantities.get(product.getId());
   order.addItem(new OrderLine(product.getId(),product.getName(),quantity,product.getPrice()));
  }
  CustomerOrder saved=orders.save(order);
  return new OrderConfirmation(saved.getId(),saved.getTotal(),accessToken);
 }
 @Transactional(readOnly=true) public CustomerOrder findOrder(Long orderId,String email,String accessToken){
  if(email==null||email.isBlank()||accessToken==null||accessToken.isBlank()) throw new IllegalArgumentException("Order ID, email and access token are required");
  return orders.findByIdAndEmailIgnoreCaseAndAccessTokenHash(orderId,email.trim(),sha256(accessToken))
   .orElseThrow(()->new ResourceNotFoundException("Order not found or access details are incorrect"));
 }
 private String sha256(String value){
  try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
  catch(NoSuchAlgorithmException e){throw new IllegalStateException("SHA-256 is unavailable",e);}
 }
}
