package com.example.demo.repository;

import com.example.demo.models.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM TransactionItem ti WHERE ti.product.id = :productId")
    void deleteByProductId(Long productId);
}
