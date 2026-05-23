package com.example.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId; // e.g. TXN-00142, RQ-1042

    @Column(nullable = false)
    private String type; // e.g., "Cash Sale", "Internal Req."

    private String requestorName;

    private String department;

    private LocalDateTime date;

    private Double totalAmount;

    @Column(nullable = false)
    private String status; // "Approved", "Pending", "Rejected"

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionItem> items = new ArrayList<>();
}
