package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    private String type; // "Cash Sale", "Internal Req."
    private String requestorName;
    private String department;
    private List<TransactionItemDTO> items;
}
