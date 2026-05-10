package org.example.hwtaskbackend.task.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hwtaskbackend.config.RequestIdFilter;
import org.example.hwtaskbackend.support.PostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskIntegrationTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgresTestSupport.registerDatasource(registry);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void requestIdEchoedWhenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/tasks").header(RequestIdFilter.REQUEST_ID_HEADER, "fixed-id"))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIdFilter.REQUEST_ID_HEADER, "fixed-id"));
    }

    @Test
    void listIgnoresInvalidSortParameterLikeSwaggerPlaceholder() throws Exception {
        mockMvc.perform(get("/api/v1/tasks").queryParam("sort", "string"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createAndGetRoundTrip() throws Exception {
        String json = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hello","description":null,"status":"TODO","priority":null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Hello"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(json);
        String id = node.get("id").asText();

        mockMvc.perform(get("/api/v1/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hello"));

        assertThat(id).isNotBlank();
    }
}
