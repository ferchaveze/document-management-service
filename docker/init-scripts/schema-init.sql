--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.

CREATE SCHEMA document_schema;
SET SCHEMA 'document_schema';

-- Documents table
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    minio_path VARCHAR(512) NOT NULL,
    size BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tags table
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Document-Tags join table
CREATE TABLE document_tags (
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    tag_id INTEGER NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (document_id, tag_id)
);

-- Indexes for performance
CREATE INDEX idx_documents_user ON documents(user);
CREATE INDEX idx_documents_name ON documents(name);
CREATE INDEX idx_documents_created_at ON documents(created_at DESC);
CREATE INDEX idx_document_tags_tag_id ON document_tags(tag_id);
