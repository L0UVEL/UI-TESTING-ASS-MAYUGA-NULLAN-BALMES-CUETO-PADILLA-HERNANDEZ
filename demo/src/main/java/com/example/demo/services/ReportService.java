package com.example.demo.services;

import com.example.demo.dto.DashboardReportDTO;
import com.example.demo.models.Inventory;
import com.example.demo.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final TransactionService transactionService;

    public DashboardReportDTO getDashboardReport() {
        List<Product> allProducts = productService.getAllProducts();
        List<Inventory> allInventory = inventoryService.getAllInventory();

        int totalItems = allInventory.stream().mapToInt(Inventory::getQuantity).sum();
        
        long lowStockAlertsCount = allProducts.stream()
                .filter(p -> "Low Stock".equals(p.getStatus()) || "Out of Stock".equals(p.getStatus()))
                .count();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long transactionsToday = transactionService.getAllTransactions().stream()
                .filter(t -> t.getDate().isAfter(startOfDay))
                .count();

        double estimatedValue = allInventory.stream()
                .mapToDouble(inv -> inv.getQuantity() * inv.getProduct().getBasePrice())
                .sum();

        return DashboardReportDTO.builder()
                .totalItems(totalItems)
                .lowStockAlertsCount((int) lowStockAlertsCount)
                .transactionsToday((int) transactionsToday)
                .estimatedInventoryValue(estimatedValue)
                .recentRequisitions(transactionService.getRecentTransactions())
                .build();
    }
}
