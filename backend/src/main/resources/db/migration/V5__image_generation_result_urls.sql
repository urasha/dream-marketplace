alter table image_generation_tasks
    add column if not exists result_urls text;
