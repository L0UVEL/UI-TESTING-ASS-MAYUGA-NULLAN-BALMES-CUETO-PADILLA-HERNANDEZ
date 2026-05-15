package com.example.demo.services;

import com.example.demo.models.Product;
import com.example.demo.repository.InventoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.TransactionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionItemRepository transactionItemRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public Product createProduct(Product product) {
        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new RuntimeException("Product with SKU " + product.getSku() + " already exists.");
        }
        // Set default status if not provided
        if (product.getStatus() == null) {
            product.setStatus("In Stock");
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setSku(productDetails.getSku());
        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        product.setBasePrice(productDetails.getBasePrice());
        product.setLowStockThreshold(productDetails.getLowStockThreshold());
        product.setStatus(productDetails.getStatus());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        
        // Manually delete related records to be safe
        inventoryRepository.deleteByProductId(id);
        transactionItemRepository.deleteByProductId(id);
        
        productRepository.deleteById(id);
    }
}
