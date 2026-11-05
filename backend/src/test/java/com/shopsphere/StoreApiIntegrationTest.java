
package com.shopsphere;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import jakarta.servlet.http.Cookie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.model.Product;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.OrderRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@AutoConfigureMockMvc
class StoreApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    private MockHttpServletRequestBuilder withValidCsrfToken(
            MockHttpServletRequestBuilder request) throws Exception {

        MvcResult result = mvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andReturn();

        Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");

        assertNotNull(csrfCookie, "CSRF cookie should be generated");

        return request
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue());
    }

    @Test
    void healthIsUp() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void csrfEndpointIssuesTokenCookie() throws Exception {
        mvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(header().string(
                        "Set-Cookie",
                        org.hamcrest.Matchers.containsString("XSRF-TOKEN")));
    }

    @Test
    void productsEndpointReturnsJson() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void invalidProductIsRejected() throws Exception {
        String body = "{\"name\":\"Test\",\"description\":\"Description\",\"price\":0,\"category\":\"Test\",\"imageUrl\":\"\",\"stock\":2}";

        mvc.perform(withValidCsrfToken(post("/api/products"))
                        .with(httpBasic("admin", "test-only-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void productWritesRequireAdminAuthentication() throws Exception {
        mvc.perform(withValidCsrfToken(post("/api/products"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkoutReturnsPrivateTokenAndLookupRequiresIt() throws Exception {
        String orderJson = "{\"customerName\":\"Test Buyer\",\"email\":\"buyer@example.com\",\"address\":\"Test Address\",\"items\":[{\"productId\":1,\"quantity\":1}]}";

        MvcResult placed = mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Map<?, ?> confirmation = objectMapper.readValue(
                placed.getResponse().getContentAsString(), Map.class);

        Map<String, Object> lookup = new java.util.HashMap<>(Map.of(
                "orderId", confirmation.get("orderId"),
                "email", "buyer@example.com",
                "accessToken", confirmation.get("accessToken")));

        mvc.perform(post("/api/orders/lookup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lookup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("buyer@example.com"));

        lookup.put("accessToken", "wrong-token");

        mvc.perform(post("/api/orders/lookup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lookup)))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutDoesNotOversellStock() throws Exception {
        MvcResult before = mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andReturn();

        Map<?, ?> product = objectMapper.readValue(
                before.getResponse().getContentAsString(), Map.class);

        int stock = ((Number) product.get("stock")).intValue();

        String orderJson = "{\"customerName\":\"Test Buyer\",\"email\":\"stock@example.com\",\"address\":\"Test Address\",\"items\":[{\"productId\":1,\"quantity\":"
                + (stock / 2 + 1)
                + "},{\"productId\":1,\"quantity\":"
                + (stock / 2 + 1)
                + "}]}";

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(stock));
    }

    @Test
    void largeValidOrderTotalFitsDatabasePrecision() throws Exception {
        String productJson = "{\"name\":\"High Value Test Item\",\"description\":\"Precision test\",\"price\":99999999.99,\"category\":\"Test\",\"imageUrl\":\"\",\"stock\":5000}";

        MvcResult created = mvc.perform(
                        withValidCsrfToken(post("/api/products"))
                                .with(httpBasic("admin", "test-only-password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(productJson))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> product = objectMapper.readValue(
                created.getResponse().getContentAsString(), Map.class);

        StringBuilder items = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            if (i > 0) {
                items.append(",");
            }
            items.append("{\"productId\":")
                    .append(product.get("id"))
                    .append(",\"quantity\":100}");
        }
        items.append("]");

        String orderJson = "{\"customerName\":\"Precision Buyer\",\"email\":\"precision@example.com\",\"address\":\"Test Address\",\"items\":"
                + items + "}";

        MvcResult placed = mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> confirmation = objectMapper.readValue(
                placed.getResponse().getContentAsString(), Map.class);

        assertEquals(
                0,
                new BigDecimal(confirmation.get("total").toString())
                        .compareTo(new BigDecimal("499999999950.00")));
    }

    @Test
    void simultaneousCheckoutsCannotSellTheLastUnitTwice() throws Exception {
        Product scarce = productRepository.save(new Product(
                "Concurrency Test Product",
                "Only one unit available",
                new BigDecimal("19.99"),
                "Test",
                "",
                1));

        long ordersBefore = orderRepository.count();

        String body = objectMapper.writeValueAsString(Map.of(
                "customerName", "Concurrent Buyer",
                "email", "concurrent@example.com",
                "address", "Test Address",
                "items", java.util.List.of(
                        Map.of("productId", scarce.getId(), "quantity", 1))));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            java.util.concurrent.Callable<Integer> checkout = () -> {
                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Concurrent checkout start timed out");
                }

                return mvc.perform(post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            };

            Future<Integer> first = executor.submit(checkout);
            Future<Integer> second = executor.submit(checkout);

            assertEquals(true, ready.await(10, TimeUnit.SECONDS),
                    "Both checkout requests should be ready before release");

            start.countDown();

            int firstStatus = first.get(20, TimeUnit.SECONDS);
            int secondStatus = second.get(20, TimeUnit.SECONDS);

            java.util.List<Integer> statuses = java.util.List.of(
                    firstStatus, secondStatus);

            assertEquals(1,
                    statuses.stream().filter(status -> status == 201).count(),
                    "Exactly one checkout should succeed");

            assertEquals(1,
                    statuses.stream().filter(status -> status == 400).count(),
                    "The competing checkout should fail for insufficient stock");

            Product after = productRepository.findById(scarce.getId())
                    .orElseThrow();

            assertEquals(0, after.getStock(),
                    "Stock must never become negative");

            assertEquals(ordersBefore + 1, orderRepository.count(),
                    "Only one order should be persisted");

        } finally {
            start.countDown();
            executor.shutdownNow();
            productRepository.deleteById(scarce.getId());
        }
    }
}
