package com.shopsphere.controller;
import com.shopsphere.dto.OrderRequest;
import com.shopsphere.dto.OrderLookupRequest;
import com.shopsphere.dto.OrderConfirmation;
import com.shopsphere.model.CustomerOrder;
import com.shopsphere.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders") @CrossOrigin(origins="http://localhost:5173")
public class OrderController {
 private final StoreService store;
 public OrderController(StoreService store){this.store=store;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public OrderConfirmation create(@Valid @RequestBody OrderRequest r){return store.placeOrder(r);}
 @PostMapping("/lookup") public CustomerOrder lookup(@Valid @RequestBody OrderLookupRequest r){return store.findOrder(r.orderId(),r.email(),r.accessToken());}
}
