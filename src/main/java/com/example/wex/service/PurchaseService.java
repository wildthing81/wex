package com.example.wex.service;

import com.example.wex.model.PurchaseRequest;
import com.example.wex.model.PurchaseResponse;
import com.example.wex.model.PurchaseTrx;
import com.example.wex.repository.PurchaseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private static final String RESPONSE_FIELDS = "record_date,country,currency,exchange_rate";
    private static final String REQ_FILTER_1 = "record_date:eq:";
    private static final String REQ_FILTER_2 = "country_currency_desc:eq:";

    @Value("${fiscaldata.exchangerate.url}")
    private String exchangeRateUrl;

    private final PurchaseRepository repository;

    public PurchaseResponse getPurchase(long parseLong, String currency, String country) throws IOException {
        var purchaseTrx = repository.findById(parseLong).orElseThrow();
        String exchangeRate = getExchangeRate(currency, country, purchaseTrx.getTransactionDate().toLocalDate());
        if (!ObjectUtils.isEmpty(exchangeRate)){
            var rate =  Double.parseDouble(exchangeRate);
            return PurchaseResponse.builder()
                    .description(purchaseTrx.getDescription())
                    .trxDate(purchaseTrx.getTransactionDate())
                    .originalAmt(purchaseTrx.getAmountInUSD())
                    .exchangeRate(rate)
                    .convertedAmt(convertAmt(purchaseTrx.getAmountInUSD(),rate))
                    .build();
        }
        return null;
    }

    public PurchaseTrx createPurchase(PurchaseRequest purchaseRequest) {
       var purchaseTrx =  PurchaseTrx.builder()
               .amountInUSD(convertAmt(new BigDecimal(purchaseRequest.getAmount()), 1.0))
               .description(purchaseRequest.getDescription())
               .transactionDate(LocalDateTime.parse(purchaseRequest.getTrxDate()))
               .build();

       return repository.save(purchaseTrx);
    }

    private String getExchangeRate(String currency, String country, LocalDate recordDate) throws JsonProcessingException {
        MultiValueMap<String,String> queryParams =  new LinkedMultiValueMap<>();
        queryParams.add("format","json");
        queryParams.add("fields",RESPONSE_FIELDS);
        queryParams.add("filter", String.join(",",
                REQ_FILTER_1+recordDate, REQ_FILTER_2+country+'-'+currency));

        var url = UriComponentsBuilder.fromHttpUrl(exchangeRateUrl)
                .queryParams(queryParams)
                .toUriString();

        var response = WebClient.create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return Optional.ofNullable(new ObjectMapper().readTree(response).get("data"))
                .map(fiscaldata -> fiscaldata.get(0))
                .map(rateData -> rateData.get("exchange_rate").asText())
                .orElse(null);
    }

    private BigDecimal convertAmt(BigDecimal amountInUSD, double rate) {
        return amountInUSD.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
    }
}
