package com.example.wex.controller;

import com.example.wex.model.PurchaseRequest;
import com.example.wex.model.PurchaseResponse;
import com.example.wex.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@Tag(name ="Purchase apis")
@RequestMapping("/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("")
    @Operation(description = "Post a purchase in US dollars", responses = {
            @ApiResponse(responseCode = "200", description = "Purchase transaction Id",
                    content = @Content(mediaType = "application/json",examples = {
                            @ExampleObject(value = """
                                    {
                                      "transactionId": 1
                                    }""")
                    }))
    })
    public ResponseEntity<Map<String,Long>> postPurchase( @Valid @RequestBody PurchaseRequest request){
        try {
            var trxId = purchaseService.createPurchase(request).getTrxId();
            return new ResponseEntity<>(Map.of("transactionId",trxId), HttpStatus.OK);
        } catch (NumberFormatException | DateTimeParseException ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{transactionId}")
    @Operation(description = "Get purchase details of a transaction id", responses =
    @ApiResponse(responseCode = "200", description = "Purchase transaction details",
            content = @Content(mediaType = "application/json",schema = @Schema(implementation = PurchaseResponse.class))))
    public ResponseEntity<PurchaseResponse> getPurchase(@PathVariable Long transactionId,
                                                        @RequestParam String currency,
                                                        @RequestParam String country){
        try {
            var response = purchaseService.getPurchase(transactionId, currency, country);
            return response != null ? new ResponseEntity<>(response, HttpStatus.OK) :
                    new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException e1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e2) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IOException ioe) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
