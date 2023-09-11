package com.example.wex.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PurchaseRequest {
    @Schema(description = "Description of purchase",
            requiredMode = Schema.RequiredMode.REQUIRED,maxLength = 50)
    @Size(min = 1,max = 50)
    String description;
    @Schema(description = "Date of purchase in ISO Local Date and Time",requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2023-03-31T10:00:00")
    String trxDate;
    @Schema(description = "Amount of purchase in USD",requiredMode = Schema.RequiredMode.REQUIRED, example = "553.23")
    String amount;
}
