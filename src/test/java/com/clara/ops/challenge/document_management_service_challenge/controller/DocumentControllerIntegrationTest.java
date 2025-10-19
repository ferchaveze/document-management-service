package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadAndSearchDocument_shouldSucceed() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "doc1.pdf", "application/pdf", new byte[]{1,2,3});
        mockMvc.perform(multipart("/document-management/upload")
                .file(file)
                .param("user", "user1")
                .param("name", "doc1.pdf")
                .param("tags", "tag1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user").value("user1"))
                .andExpect(jsonPath("$.name").value("doc1.pdf"));

        DocumentController.SearchRequest searchRequest = new DocumentController.SearchRequest();
        searchRequest.setUser("user1");
        searchRequest.setName("doc1.pdf");
        searchRequest.setTags(Collections.singletonList("tag1"));
        mockMvc.perform(post("/document-management/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].user").value("user1"));
    }

    @Test
    void downloadDocument_shouldReturnPresignedUrlOrNotFound() throws Exception {
        Document doc = Document.builder()
                .user("user2")
                .name("doc2.pdf")
                .minioPath("user2/doc2.pdf")
                .size(123L)
                .type("application/pdf")
                .build();
        doc = documentRepository.save(doc);
        UUID id = doc.getId();
        mockMvc.perform(get("/document-management/download/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());

        mockMvc.perform(get("/document-management/download/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}

