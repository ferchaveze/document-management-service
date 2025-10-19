package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.model.Tag;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.TagRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private MinioClient minioClient;
    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadDocument_shouldSaveDocumentAndTags() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));
        when(file.getSize()).thenReturn(3L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(tagRepository.findByName(anyString())).thenReturn(null);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        Document doc = documentService.uploadDocument(file, "user1", "doc1.pdf", Collections.singletonList("tag1"));
        assertThat(doc.getUser()).isEqualTo("user1");
        assertThat(doc.getName()).isEqualTo("doc1.pdf");
        assertThat(doc.getTags()).extracting(Tag::getName).containsExactly("tag1");
        verify(minioClient).putObject(any());
    }

    @Test
    void generatePresignedUrl_shouldReturnUrl() throws Exception {
        UUID id = UUID.randomUUID();
        Document doc = Document.builder().id(id).minioPath("user1/doc1.pdf").build();
        when(documentRepository.findById(id)).thenReturn(java.util.Optional.of(doc));
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://minio-url");
        String url = documentService.generatePresignedUrl(id);
        assertThat(url).isEqualTo("http://minio-url");
    }

    @Test
    void generatePresignedUrl_shouldReturnNullIfNotFound() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(java.util.Optional.empty());
        String url = documentService.generatePresignedUrl(id);
        assertThat(url).isNull();
    }
}

