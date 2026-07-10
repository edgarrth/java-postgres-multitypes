create extension if not exists vector;

create table if not exists payment_search_documents (
    id uuid primary key,
    payment_reference varchar(60) not null unique,
    channel varchar(40) not null,
    description text not null,
    search_vector tsvector generated always as (
        setweight(to_tsvector('spanish', coalesce(payment_reference, '')), 'A') ||
        setweight(to_tsvector('spanish', coalesce(channel, '')), 'B') ||
        setweight(to_tsvector('spanish', coalesce(description, '')), 'C')
    ) stored,
    created_at timestamptz not null default now()
);

create index if not exists idx_payment_search_documents_vector
    on payment_search_documents using gin (search_vector);

create table if not exists payment_semantic_rules (
    id uuid primary key,
    rule_code varchar(50) not null unique,
    title varchar(140) not null,
    description text not null,
    embedding vector(6) not null,
    created_at timestamptz not null default now()
);

create index if not exists idx_payment_semantic_rules_embedding
    on payment_semantic_rules using hnsw (embedding vector_cosine_ops);

create unlogged table if not exists payment_cache_entries (
    cache_key varchar(180) primary key,
    cache_value jsonb not null,
    expires_at timestamptz not null,
    updated_at timestamptz not null default now()
);

create index if not exists idx_payment_cache_entries_expires_at
    on payment_cache_entries (expires_at);

create table if not exists payment_profiles (
    id uuid primary key,
    profile_code varchar(60) not null unique,
    document_number varchar(20) not null,
    attributes jsonb not null,
    created_at timestamptz not null default now()
);

create index if not exists idx_payment_profiles_attributes_gin
    on payment_profiles using gin (attributes jsonb_path_ops);

create table if not exists payment_outbox_events (
    id uuid primary key,
    aggregate_id varchar(80) not null,
    event_type varchar(120) not null,
    payload jsonb not null,
    status varchar(20) not null check (status in ('PENDING', 'PUBLISHED', 'FAILED')),
    created_at timestamptz not null default now(),
    published_at timestamptz
);

create index if not exists idx_payment_outbox_events_status_created
    on payment_outbox_events (status, created_at);

create table if not exists payment_digital_certificates (
    id uuid primary key,
    alias varchar(80) not null unique,
    owner varchar(120) not null,
    algorithm varchar(40) not null,
    fingerprint_sha256 char(64) not null,
    pem_content bytea not null,
    created_at timestamptz not null default now()
);

create index if not exists idx_payment_digital_certificates_owner
    on payment_digital_certificates (owner);
