package com.example.demo.services;

import com.example.demo.models.Inventory;
import com.example.demo.models.Product;
import com.example.demo.repository.InventoryRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Transactional
    public Inventory addStock(Long productId, String location, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Inventory inventory = inventoryRepository.findByProductIdAndLocation(productId, location)
                .orElse(Inventory.builder()
                        .product(product)
                        .location(location)
                        .quantity(0)
                        .build());

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory = inventoryRepository.save(inventory);
        updateProductStatus(product);
        return inventory;
    }

    @Transactional
    public Inventory removeStock(Long productId, String location, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductIdAndLocation(productId, location)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product in location"));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock in " + location);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory = inventoryRepository.save(inventory);
        updateProductStatus(inventory.getProduct());
        return inventory;
    }

    @Transactional
    public void transferStock(Long productId, String fromLocation, String toLocation, Integer quantity) {
        removeStock(productId, fromLocation, quantity);
        addStock(productId, toLocation, quantity);
    }

    private void updateProductStatus(Product product) {
        List<Inventory> allInventory = inventoryRepository.findByProductId(product.getId());
        int totalStock = allInventory.stream().mapToInt(Inventory::getQuantity).sum();
        
        if (totalStock == 0) {
            product.setStatus("Out of Stock");
        } else if (product.getLowStockThreshold() != null && totalStock <= product.getLowStockThreshold()) {
            product.setStatus("Low Stock");
        } else {
            product.setStatus("In Stock");
        }
        productRepository.save(product);
    }
}
