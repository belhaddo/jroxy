package com.belhaddou.jroxy;

import com.belhaddou.jroxy.exception.JRoxyIllegalArgumentException;
import com.belhaddou.jroxy.exception.JRoxyRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class IntegrationTest extends AbstractTest {

    @BeforeEach
    void setup() {
        doReturn(new ResponseEntity<>("GET Executed".getBytes(), HttpStatus.OK))
                .when(restTemplate).exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(byte[].class)
                );

        doReturn(new ResponseEntity<>("POST Executed".getBytes(), HttpStatus.OK))
                .when(restTemplate).exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(byte[].class)
                );

        doThrow(new JRoxyRuntimeException("PUT Error"))
                .when(restTemplate).exchange(
                        anyString(),
                        eq(HttpMethod.PUT),
                        any(HttpEntity.class),
                        eq(byte[].class)
                );

        doThrow(new JRoxyIllegalArgumentException("PATCH Error"))
                .when(restTemplate).exchange(
                        anyString(),
                        eq(HttpMethod.PATCH),
                        any(HttpEntity.class),
                        eq(byte[].class)
                );
    }

    @Test
    void shouldFailBadGateway() throws Exception {
        mockMvc.perform(get("/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    @Test
    void shouldSucceedGetRequest() throws Exception {
        mockMvc.perform(get("/")
                        .header("Host", "test.my-company.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("GET Executed"));
    }

    @Test
    void shouldSucceedPostRequest() throws Exception {
        String requestBody = "{\"key\":\"value\"}";

        mockMvc.perform(post("/")
                        .header("Host", "test.my-company.com")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("POST Executed"));
    }

    @Test
    void shouldFailPutRequest() throws Exception {
        String requestBody = "{\"key\":\"value\"}";

        mockMvc.perform(put("/1")
                        .header("Host", "test.my-company.com")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: PUT Error"));
    }

    @Test
    void shouldFailPatchRequest() throws Exception {
        String requestBody = "{\"key\":\"value\"}";

        mockMvc.perform(patch("/1")
                        .header("Host", "test.my-company.com")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(content().string("Error: PATCH Error"));
    }
}
