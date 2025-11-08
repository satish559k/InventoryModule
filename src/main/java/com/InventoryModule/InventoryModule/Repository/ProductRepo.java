package com.InventoryModule.InventoryModule.Repository;

import com.InventoryModule.InventoryModule.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepo extends JpaRepository<Product, UUID>{
    public Product findByproductName(String name);

    public Product findByproductId(UUID id);


}
