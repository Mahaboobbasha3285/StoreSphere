package com.shopsphere.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record OrderRequest(@NotBlank @Size(max=120) String customerName,@NotBlank @Email @Size(max=180) String email,
 @NotBlank @Size(max=1000) String address,@NotEmpty @Size(max=50) List<@Valid Item> items) {
 public record Item(@NotNull @Positive Long productId,@NotNull @Min(1) @Max(100) Integer quantity) {}
}
