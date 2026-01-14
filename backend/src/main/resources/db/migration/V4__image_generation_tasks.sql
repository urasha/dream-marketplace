create table if not exists image_generation_tasks (
    id uuid primary key,
    user_id bigint not null references user_account(id),
    prompt text not null,
    model_version varchar(32),
    aspect_ratio varchar(16),
    provider_request_id varchar(64),
    status varchar(32) not null,
    result_url text,
    error text,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
