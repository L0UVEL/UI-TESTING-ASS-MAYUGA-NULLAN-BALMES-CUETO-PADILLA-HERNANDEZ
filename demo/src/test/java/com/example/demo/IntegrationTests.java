package com.example.demo;

import com.example.demo.dto.DashboardReportDTO;
import com.example.demo.dto.TransactionItemDTO;
import com.example.demo.dto.TransactionRequestDTO;
import com.example.demo.models.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.services.InventoryService;
import com.example.demo.services.ReportService;
import com.example.demo.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // Rollback after each test
public class IntegrationTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private com.example.demo.services.ProductService productService;

    @Autowired
    private ReportService reportService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Setup a base product for tests
        Product product = Product.builder()
                .name("Test Product")
                .sku("TEST-SKU-001")
                .category("Electronics")
                .basePrice(100.0)
                .lowStockThreshold(5)
                .status("In Stock")
                .build();
        testProduct = productRepository.save(product);
        
        // Add initial stock of 20
        inventoryService.addStock(testProduct.getId(), "Main Storage", 20);
    }

    @Test
    void testSalesReduceStock() {
        // Arrange
        TransactionItemDTO itemDto = TransactionItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(3)
                .location("Main Storage")
                .build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .type("Cash Sale")
                .requestorName("Test User")
                .department("Sales")
                .items(Collections.singletonList(itemDto))
                .build();

        // Act
        transactionService.processTransaction(request);

        // Assert
        int remainingStock = inventoryService.getInventoryByProduct(testProduct.getId())
                .stream()
                .filter(i -> i.getLocation().equals("Main Storage"))
                .findFirst()
                .get()
                .getQuantity();

        assertEquals(17, remainingStock, "Stock should be reduced from 20 to 17 after a sale of 3");
    }

    @Test
    void testReportsReflectUpdatedInventory() {
        // Arrange
        // Initial report check
        DashboardReportDTO initialReport = reportService.getDashboardReport();
        int initialTotalItems = initialReport.getTotalItems();
        double initialEstimatedValue = initialReport.getEstimatedInventoryValue();

        // Perform a sale to update inventory
        TransactionItemDTO itemDto = TransactionItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(5)
                .location("Main Storage")
                .build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .type("Cash Sale")
                .requestorName("Test User")
                .department("Sales")
                .items(Collections.singletonList(itemDto))
                .build();

        transactionService.processTransaction(request);

        // Act
        DashboardReportDTO updatedReport = reportService.getDashboardReport();

        // Assert
        assertEquals(initialTotalItems - 5, updatedReport.getTotalItems(), "Report total items should reflect the reduced inventory");
        assertEquals(initialEstimatedValue - (5 * testProduct.getBasePrice()), updatedReport.getEstimatedInventoryValue(), "Report estimated value should reflect the reduced inventory");
    }

    @Test
    void testLowStockAlertsAppear() {
        // Arrange
        // Current stock is 20, threshold is 5. We need to sell 16 to bring it to 4 (which is <= 5)
        TransactionItemDTO itemDto = TransactionItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(16)
                .location("Main Storage")
                .build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .type("Cash Sale")
                .requestorName("Test User")
                .department("Sales")
                .items(Collections.singletonList(itemDto))
                .build();

        // Check initial low stock alerts (should be 0 for this product if it's the only one, or just store the baseline)
        int initialAlerts = reportService.getDashboardReport().getLowStockAlertsCount();

        // Act
        transactionService.processTransaction(request);

        // Assert
        // Verify product status
        Product updatedProduct = productRepository.findById(testProduct.getId()).get();
        assertEquals("Low Stock", updatedProduct.getStatus(), "Product status should be updated to 'Low Stock'");

        // Verify report includes the low stock alert
        int updatedAlerts = reportService.getDashboardReport().getLowStockAlertsCount();
        assertTrue(updatedAlerts > initialAlerts, "Low stock alerts count should increase in the dashboard report");
        assertEquals(initialAlerts + 1, updatedAlerts, "There should be exactly 1 new low stock alert");
    }

    @Test
    void testProductDeletion() {
        // Arrange
        Long productId = testProduct.getId();
        
        // Act
        productService.deleteProduct(productId);
        
        // Assert
        assertTrue(productRepository.findById(productId).isEmpty(), "Product should be deleted");
        assertTrue(inventoryService.getInventoryByProduct(productId).isEmpty(), "Inventory records for the product should be deleted");
    }
}
