package com.example.wex;

import com.example.wex.model.PurchaseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WexApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testPostAndGetPurchase() throws Exception {
		var testRequest = new PurchaseRequest();
		testRequest.setAmount("543.456");
		testRequest.setDescription("test purchase");
		testRequest.setTrxDate("2023-03-31T10:00");

		mockMvc.perform(MockMvcRequestBuilders.post("/purchase")
						.accept(APPLICATION_JSON)
						.contentType(APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(testRequest))
				)
				.andDo(print())
				.andExpectAll(status().isOk(),
						content().contentType(APPLICATION_JSON),
						jsonPath("$.transactionId").value("1")
				);

		mockMvc.perform(MockMvcRequestBuilders.get("/purchase/1")
						.accept(APPLICATION_JSON)
						.queryParam("currency", "Dollar")
						.queryParam("country", "Canada")
				)
				.andDo(print())
				.andExpectAll(status().isOk(),
						content().contentType(APPLICATION_JSON),
						jsonPath("$.originalAmt").value("543.46"),
						jsonPath("$.exchangeRate").value("1.355"),
						jsonPath("$.convertedAmt").value("736.39")
				);
	}
}
