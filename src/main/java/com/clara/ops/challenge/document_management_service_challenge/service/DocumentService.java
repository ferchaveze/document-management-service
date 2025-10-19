package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.model.Tag;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.TagRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final TagRepository tagRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Transactional
    public Document saveDocument(Document document, List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName);
            if (tag == null) {
                tag = Tag.builder().name(tagName).build();
                tag = tagRepository.save(tag);
            }
            tags.add(tag);
        }
        document.setTags(tags);
        return documentRepository.save(document);
    }

    public Document getDocument(UUID id) {
        return documentRepository.findById(id).orElse(null);
    }

    public Page<Document> searchDocuments(String user, String name, List<String> tags, Pageable pageable) {
        Specification<Document> spec = Specification.where(null);
        if (user != null && !user.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user"), user));
        }
        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (tags != null && !tags.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.join("tags").get("name").in(tags));
        }
        return documentRepository.findAll(spec, pageable);
    }

    @Transactional
    public Document uploadDocument(MultipartFile file, String user, String name, List<String> tags) {
        try (InputStream inputStream = file.getInputStream()) {
            String objectName = user + "/" + name;
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            Document document = Document.builder()
                .user(user)
                .name(name)
                .minioPath(objectName)
                .size(file.getSize())
                .type(file.getContentType())
                .build();
            return saveDocument(document, tags);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    public String generatePresignedUrl(UUID documentId) {
        Document document = getDocument(documentId);
        if (document == null) return null;
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(document.getMinioPath())
                    .expiry((int) Duration.ofMinutes(15).getSeconds())
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
}
