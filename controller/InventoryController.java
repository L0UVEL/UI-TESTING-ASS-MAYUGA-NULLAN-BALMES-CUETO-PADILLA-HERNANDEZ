package com.example.demo.controller;

import com.example.demo.models.Inventory;
import com.example.demo.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Inventory>> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProduct(productId));
    }

    @PostMapping("/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Inventory> addStock(@RequestBody Map<String, Object> request) {
        Long productId = Long.valueOf(request.get("productId").toString());
        String location = request.get("location").toString();
        Integer quantity = (Integer) request.get("quantity");
        return ResponseEntity.ok(inventoryService.addStock(productId, location, quantity));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> transferStock(@RequestBody Map<String, Object> request) {
        Long productId = Long.valueOf(request.get("productId").toString());
        String fromLocation = request.get("fromLocation").toString();
        String toLocation = request.get("toLocation").toString();
        Integer quantity = (Integer) request.get("quantity");
        
        inventoryService.transferStock(productId, fromLocation, toLocation, quantity);
        return ResponseEntity.ok().build();
    }
}
