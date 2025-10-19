package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/document-management")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping("/search")
    public ResponseEntity<Page<Document>> searchDocuments(
            @RequestBody SearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> result = documentService.searchDocuments(
                request.getUser(),
                request.getName(),
                request.getTags(),
                pageable
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user") String user,
            @RequestParam("name") String name,
            @RequestParam("tags") List<String> tags
    ) {
        // Validate file type and size
        if (file.isEmpty() || !file.getContentType().equalsIgnoreCase("application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // Delegate to service (to be implemented)
        Document saved = documentService.uploadDocument(file, user, name, tags);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<DownloadUrlResponse> downloadDocument(@PathVariable UUID documentId) {
        String url = documentService.generatePresignedUrl(documentId);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DownloadUrlResponse(url));
    }

    // DTO for search request
    public static class SearchRequest {
        private String user;
        private String name;
        private List<String> tags;
        // getters and setters
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class DownloadUrlResponse {
        private String url;
        public DownloadUrlResponse(String url) { this.url = url; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
