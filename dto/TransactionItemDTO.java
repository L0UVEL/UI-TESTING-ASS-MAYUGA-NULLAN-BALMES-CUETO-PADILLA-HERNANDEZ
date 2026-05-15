package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItemDTO {
    private Long productId;
    private Integer quantity;
    private String location; // The location to deduct from, if applicable
}
