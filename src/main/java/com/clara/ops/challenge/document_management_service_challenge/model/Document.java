package com.clara.ops.challenge.document_management_service_challenge.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "documents", schema = "document_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @Type(type = "uuid-char")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(name = "user", nullable = false)
    private String user;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "minio_path", nullable = false)
    private String minioPath;

    @NotNull
    @Column(name = "size", nullable = false)
    private long size;

    @NotBlank
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "document_tags",
        schema = "document_schema",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;
}
