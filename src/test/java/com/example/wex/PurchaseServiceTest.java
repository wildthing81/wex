package com.example.wex;

import com.example.wex.model.PurchaseRequest;
import com.example.wex.model.PurchaseResponse;
import com.example.wex.model.PurchaseTrx;
import com.example.wex.repository.PurchaseRepository;
import com.example.wex.service.PurchaseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceTest {
    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService serviceInTest;

    private static PurchaseTrx testTransaction;

    @BeforeAll
    static void init(){
        testTransaction = new PurchaseTrx(99,"this is a test purchase",
                LocalDateTime.parse("2021-03-31T09:00"),
                BigDecimal.valueOf(543.56));
    }

    @Nested
    class TestCreatePurchase{
        PurchaseRequest testRequest;

        @BeforeEach
        void setUp(){
            testRequest = new PurchaseRequest();
            testRequest.setAmount("543.456");
            testRequest.setDescription("test purchase");
            testRequest.setTrxDate("2023-03-31T10:00");
        }

        @Test
        void testSavePurchase_Success_Returns_New_Transaction(){
            // Assign
            given(purchaseRepository.save(any(PurchaseTrx.class))).willReturn(testTransaction);

            // Action
            var response = serviceInTest.createPurchase(testRequest);

            // Assert
            verify(purchaseRepository).save(any(PurchaseTrx.class));
            assertEquals(testTransaction,response);
        }

        @ParameterizedTest
        @ValueSource(strings = {"2021-03T09:00", "2021-03-31 09:00", "2021-03-31T09:000Z"})
        void testSavePurchase_Exception_InvalidPurchaseDate(String date){
            testRequest.setTrxDate(date);
            // Action
            assertThrows(DateTimeParseException.class,() -> serviceInTest.createPurchase(testRequest));

            // Assert
            verify(purchaseRepository, never()).save(any(PurchaseTrx.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"$54.67", "467.45 dollars"})
        void testSavePurchase_Exception_InvalidPurchaseAmount(String amount){
            testRequest.setAmount(amount);
            // Action
            assertThrows(NumberFormatException.class,() -> serviceInTest.createPurchase(testRequest));

            // Assert
            verify(purchaseRepository,never()).save(any(PurchaseTrx.class));
        }
    }

    @Nested
    class TestGetPurchase {
        long testTrxId;
        String testCountry;
        String testCurrency;
        PurchaseResponse testResponse;

        @BeforeEach
        void setUp() {
            testResponse = PurchaseResponse.builder().build();
            testCountry = "Mexico";
            testCurrency = "Peso";

            ReflectionTestUtils.setField(serviceInTest,"exchangeRateUrl",
                    "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange");
        }

        @Test
        void testGetPurchase_Success_Returns_PurchaseDetails() throws IOException {
            // Assign
            given(purchaseRepository.findById(anyLong())).willReturn(Optional.of(testTransaction));

            // Action
            var response = serviceInTest.getPurchase(testTrxId,testCurrency,testCountry);

            // Assert
            verify(purchaseRepository).findById(testTrxId);
            assertAll("PurchaseResponse",
                ()->  assertEquals(BigDecimal.valueOf(543.56), response.getOriginalAmt()),
                () -> assertEquals(20.518, response.getExchangeRate()),
                () -> assertEquals(BigDecimal.valueOf(11152.76), response.getConvertedAmt()),
                () -> assertEquals(LocalDateTime.parse("2021-03-31T09:00"), response.getTrxDate())
            );
        }

        @Test
        void testGetPurchase_Return_Null_ExchangeRateApi_NoResponse() throws IOException {
            // Assign
            testCurrency = "Dinar";
            given(purchaseRepository.findById(anyLong())).willReturn(Optional.of(testTransaction));

            // Action
            var response = serviceInTest.getPurchase(testTrxId,testCurrency,testCountry);

            // Assert
            verify(purchaseRepository).findById(testTrxId);
            assertNull(response);
        }

        @Test
        void testGetPurchase_Return_Null_ExchangeRate_Missing() throws IOException {
            // Assign
            testTransaction.setTransactionDate(LocalDateTime.parse("2023-12-31T09:00"));
            given(purchaseRepository.findById(anyLong())).willReturn(Optional.of(testTransaction));

            // Action
            var response = serviceInTest.getPurchase(testTrxId,testCurrency,testCountry);

            // Assert
            verify(purchaseRepository).findById(testTrxId);
            assertNull(response);
        }
    }
}
