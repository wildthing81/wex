package com.example.wex.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PurchaseResponse {
    @Schema(description = "Description of purchase",requiredMode = Schema.RequiredMode.REQUIRED)
    String description;
    @Schema(description = "Date of purchase",requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime trxDate;
    @Schema(description = "Original purchase amount in USD",requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal originalAmt;
    @Schema(description = "Exchange rate of requested currency",requiredMode = Schema.RequiredMode.REQUIRED)
    Double exchangeRate;
    @Schema(description = "Purchase amount in requested currency",requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal convertedAmt;
}
