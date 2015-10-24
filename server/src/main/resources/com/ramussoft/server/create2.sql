ALTER TABLE binary_data DROP CONSTRAINT binary_data_pkey;
ALTER TABLE binary_data ADD COLUMN created_branch_id bigint NOT NULL DEFAULT 0;
ALTER TABLE binary_data ADD COLUMN removed_branch_id bigint;
ALTER TABLE binary_data ADD PRIMARY KEY ("name", created_branch_id);
