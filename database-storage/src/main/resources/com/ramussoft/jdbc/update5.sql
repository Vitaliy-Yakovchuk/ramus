UPDATE {0}elements SET removed_branch_id = 2147483647 WHERE removed_branch_id IS NULL;

ALTER TABLE {0}elements ALTER COLUMN removed_branch_id SET DEFAULT 2147483647;

ALTER TABLE {0}elements ALTER COLUMN removed_branch_id SET NOT NULL;

UPDATE {0}qualifiers SET removed_branch_id = 2147483647 WHERE removed_branch_id IS NULL;

ALTER TABLE {0}qualifiers ALTER COLUMN removed_branch_id SET DEFAULT 2147483647;

ALTER TABLE {0}qualifiers ALTER COLUMN removed_branch_id SET NOT NULL;

UPDATE {0}attributes SET removed_branch_id = 2147483647 WHERE removed_branch_id IS NULL;

ALTER TABLE {0}attributes ALTER COLUMN removed_branch_id SET DEFAULT 2147483647;

ALTER TABLE {0}attributes ALTER COLUMN removed_branch_id SET NOT NULL;

UPDATE {0}streams SET removed_branch_id = 2147483647 WHERE removed_branch_id IS NULL;

ALTER TABLE {0}streams ALTER COLUMN removed_branch_id SET DEFAULT 2147483647;

ALTER TABLE {0}streams ALTER COLUMN removed_branch_id SET NOT NULL;

UPDATE {0}attributes_history SET removed_branch_id = 2147483647 WHERE removed_branch_id IS NULL;

ALTER TABLE {0}attributes_history ALTER COLUMN removed_branch_id SET DEFAULT 2147483647;

ALTER TABLE {0}attributes_history ALTER COLUMN removed_branch_id SET NOT NULL;
