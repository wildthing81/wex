package com.example.wex;

import com.example.wex.controller.PurchaseController;
import com.example.wex.model.PurchaseRequest;
import com.example.wex.model.PurchaseResponse;
import com.example.wex.model.PurchaseTrx;
import com.example.wex.service.PurchaseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PurchaseControllerTest {
    @Mock
    private PurchaseService purchaseService;

    @InjectMocks
    private PurchaseController controllerInTest;

    private static PurchaseTrx testTransaction;

    @BeforeAll
    static void init(){
        testTransaction = new PurchaseTrx(99,"this is a test purchase",
                LocalDateTime.parse("2021-03-31T09:00"),
                BigDecimal.valueOf(543.56));
    }

    @Nested
    class TestPostPurchase{
        PurchaseRequest testRequest;

        @BeforeEach
        void setUp(){
            testRequest = new PurchaseRequest();
        }

        @Test
        void testSavePurchase_200_Returns_TransactionId(){
            // Assign
            given(purchaseService.createPurchase(any(PurchaseRequest.class))).willReturn(testTransaction);

            // Action
            var response = controllerInTest.postPurchase(testRequest);

            // Assert
            verify(purchaseService).createPurchase(testRequest);
            assertEquals(Map.of("transactionId",99L),response.getBody());
            assertTrue(HttpStatus.OK.isSameCodeAs(response.getStatusCode()));
        }

        @Test
        //@ValueSource(strings = {"2021-03T09:00", "2021-03-31 09:00", "2021-03-31T09:000Z"})
        void testSavePurchase_400_InvalidPurchaseDate(){
            // Assign
            given(purchaseService.createPurchase(any(PurchaseRequest.class)))
                    .willThrow(DateTimeParseException.class);

            // Action
            var response = controllerInTest.postPurchase(testRequest);

            // Assert
            verify(purchaseService).createPurchase(testRequest);
            assertTrue(HttpStatus.BAD_REQUEST.isSameCodeAs(response.getStatusCode()));
        }

        @Test
        void testSavePurchase_400_InvalidPurchaseAmount(){
            // Assign
            given(purchaseService.createPurchase(any(PurchaseRequest.class)))
                    .willThrow(new NumberFormatException("Invalid purchase amount"));

            // Action
            var response = controllerInTest.postPurchase(testRequest);

            // Assert
            verify(purchaseService).createPurchase(testRequest);
            assertTrue(HttpStatus.BAD_REQUEST.isSameCodeAs(response.getStatusCode()));
        }
    }

    @Nested
    class TestGetPurchase{
        long testTrxId;
        String testCountry;
        String testCurrency;

        PurchaseResponse testResponse;

        @BeforeEach
        void setUp(){
            testResponse = PurchaseResponse.builder().build();
            testCountry = "Mexico";
            testCurrency = "Peso";
        }

        @Test
        void testGetPurchase_200_Returns_PurchaseDetails() throws IOException {
            // Assign
            given(purchaseService.getPurchase(anyLong(),anyString(),anyString())).willReturn(testResponse);

            // Action
            var response = controllerInTest.getPurchase(testTrxId,testCurrency,testCountry);

            // Assert
            verify(purchaseService).getPurchase(testTrxId,testCurrency,testCountry);
            assertEquals(testResponse,response.getBody());
            assertTrue(HttpStatus.OK.isSameCodeAs(response.getStatusCode()));
        }

//        @Test
//        void testGetPurchase_Error_Invalid_Transaction_Id() throws IOException {
//            // Action
//            var response = controllerInTest.getPurchase(testTrxId,testCurrency,testCountry);
//
//            // Assert
//            verify(purchaseService, never()).getPurchase(testTrxId,testCurrency,testCountry);
//            assertTrue(HttpStatus.BAD_REQUEST.isSameCodeAs(response.getStatusCode()));
//        }

        @Test
        void testGetPurchase_204_No_Transaction_Exists() throws IOException {
            // Assign
            given(purchaseService.getPurchase(anyLong(),anyString(),anyString())).willThrow(NoSuchElementException.class);

            // Action
            var response = controllerInTest.getPurchase(testTrxId,testCurrency,testCountry);

            // Assert
            verify(purchaseService).getPurchase(testTrxId,testCurrency,testCountry);
            assertTrue(HttpStatus.NO_CONTENT.isSameCodeAs(response.getStatusCode()));
        }

        @Test
        void testGetPurchase_503_ExchangeRate_Call_Failure() throws IOException {
            // Assign
            given(purchaseService.getPurchase(anyLong(),anyString(),anyString())).willThrow(IOException.class);

            // Action
            var response = controllerInTest.getPurchase(testTrxId,testCurrency,testCountry);

            // Assert
            verify(purchaseService).getPurchase(testTrxId,testCurrency,testCountry);
            assertTrue(HttpStatus.SERVICE_UNAVAILABLE.isSameCodeAs(response.getStatusCode()));
        }

        @Test
        void testGetPurchase_500_ExchangeRate_Missing() throws IOException {
            // Assign
            given(purchaseService.getPurchase(anyLong(),anyString(),anyString())).willReturn(null);

            // Action
            var response = controllerInTest.getPurchase(testTrxId,testCurrency,testCountry);

            // Assert
            verify(purchaseService).getPurchase(testTrxId,testCurrency,testCountry);
            assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.isSameCodeAs(response.getStatusCode()));
        }
    }
}
