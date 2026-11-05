package com.shopsphere.controller;
import com.shopsphere.dto.ProductRequest;
import com.shopsphere.model.Product;
import com.shopsphere.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/products") @CrossOrigin(origins="http://localhost:5173")
public class ProductController {
 private final StoreService store;
 public ProductController(StoreService store){this.store=store;}
 @GetMapping public List<Product> list(@RequestParam(required=false) String q,@RequestParam(required=false) String category){return store.listProducts(q,category);}
 @GetMapping("/{id}") public Product get(@PathVariable Long id){return store.getProduct(id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Product create(@Valid @RequestBody ProductRequest r){return store.createProduct(r);}
 @PutMapping("/{id}") public Product update(@PathVariable Long id,@Valid @RequestBody ProductRequest r){return store.updateProduct(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){store.deleteProduct(id);}
}
