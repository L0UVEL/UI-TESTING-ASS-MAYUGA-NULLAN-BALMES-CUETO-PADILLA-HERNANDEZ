package com.example.demo.services;

import com.example.demo.dto.TransactionItemDTO;
import com.example.demo.dto.TransactionRequestDTO;
import com.example.demo.models.Product;
import com.example.demo.models.Transaction;
import com.example.demo.models.TransactionItem;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Transactional
    public Transaction processTransaction(TransactionRequestDTO request) {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .type(request.getType())
                .requestorName(request.getRequestorName())
                .department(request.getDepartment())
                .date(LocalDateTime.now())
                .status("Approved") // Default to approved for now, could be Pending based on logic
                .items(new ArrayList<>())
                .build();

        double totalAmount = 0.0;

        for (TransactionItemDTO itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Deduct from inventory if it's an outbound transaction
            if (request.getType().equalsIgnoreCase("Cash Sale") || request.getType().equalsIgnoreCase("Internal Req.")) {
                String location = itemDto.getLocation() != null ? itemDto.getLocation() : "Main Storage";
                inventoryService.removeStock(product.getId(), location, itemDto.getQuantity());
            }

            double subtotal = product.getBasePrice() * itemDto.getQuantity();
            totalAmount += subtotal;

            TransactionItem transactionItem = TransactionItem.builder()
                    .transaction(transaction)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(product.getBasePrice())
                    .subtotal(subtotal)
                    .build();

            transaction.getItems().add(transactionItem);
        }

        transaction.setTotalAmount(totalAmount);
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByDateDesc();
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
