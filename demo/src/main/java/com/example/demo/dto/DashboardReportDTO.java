package com.example.demo.dto;

import com.example.demo.models.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportDTO {
    private Integer totalItems;
    private Integer lowStockAlertsCount;
    private Integer transactionsToday;
    private Double estimatedInventoryValue;
    private List<Transaction> recentRequisitions;
}
