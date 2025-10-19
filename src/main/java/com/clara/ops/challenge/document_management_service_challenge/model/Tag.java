package com.clara.ops.challenge.document_management_service_challenge.model;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "tags", schema = "document_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Document> documents;
}

