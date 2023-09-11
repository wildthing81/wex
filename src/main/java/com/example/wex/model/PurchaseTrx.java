package com.example.wex.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTrx {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long trxId;

    @Column(length = 50)
    String description;

    @Column(nullable = false)
    LocalDateTime transactionDate;

    @Column(nullable = false)
    BigDecimal amountInUSD;
}
