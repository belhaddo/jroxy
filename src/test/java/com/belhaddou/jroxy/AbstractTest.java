package com.belhaddou.jroxy;

import org.ehcache.core.Ehcache;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AbstractTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected Ehcache ehcache;

    @MockitoSpyBean
    protected RestTemplate restTemplate;

    @AfterEach
    void cleanUp() {
        ehcache.clear();

    }
}
