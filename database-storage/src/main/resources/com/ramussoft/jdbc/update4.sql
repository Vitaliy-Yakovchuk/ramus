CREATE TABLE {0}branches
(
  branch_id BIGINT NOT NULL,
  creation_user TEXT,
  reason TEXT NOT NULL,
  branch_type INTEGER NOT NULL DEFAULT 0,
  module_name TEXT NOT NULL,
  creation_time timestamp NOT NULL,
  CONSTRAINT {0}branches_pkey PRIMARY KEY (branch_id)
);

CREATE TABLE {0}attributes_data_metadata
(
  element_id BIGINT NOT NULL,
  attribute_id BIGINT NOT NULL,
  branch_id BIGINT NOT NULL,
  CONSTRAINT {0}attributes_data_metadata_pkey PRIMARY KEY (element_id, attribute_id, branch_id)
);

CREATE INDEX {0}attributes_data_metadata_index ON {0}attributes_data_metadata(element_id, attribute_id);

ALTER TABLE {0}elements ADD COLUMN created_branch_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE {0}elements ADD COLUMN removed_branch_id BIGINT NOT NULL DEFAULT 2147483647;

ALTER TABLE {0}qualifiers ADD COLUMN created_branch_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE {0}qualifiers ADD COLUMN removed_branch_id BIGINT NOT NULL DEFAULT 2147483647;

ALTER TABLE {0}attributes ADD COLUMN created_branch_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE {0}attributes ADD COLUMN removed_branch_id BIGINT NOT NULL DEFAULT 2147483647;

ALTER TABLE {0}streams ADD COLUMN created_branch_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE {0}streams ADD COLUMN removed_branch_id BIGINT NOT NULL DEFAULT 2147483647;

CREATE TABLE {0}attributes_history
(
  ATTRIBUTE_ID BIGINT NOT NULL,
  ATTRIBUTE_NAME TEXT NOT NULL,
  created_branch_id BIGINT NOT NULL,
  removed_branch_id BIGINT NOT NULL DEFAULT 2147483647
);

CREATE TABLE {0}qualifiers_history
(
  QUALIFIER_ID BIGINT NOT NULL,
  QUALIFIER_NAME TEXT NOT NULL,
  ATTRIBUTE_FOR_NAME BIGINT,
  created_branch_id BIGINT NOT NULL
);

ALTER TABLE {0}qualifiers_attributes ADD COLUMN created_branch_id BIGINT NOT NULL DEFAULT 0;

CREATE TABLE {0}formulas_data_metadata
(
  element_id BIGINT NOT NULL,
  attribute_id BIGINT NOT NULL,
  branch_id BIGINT NOT NULL,
  CONSTRAINT {0}formulas_data_metadata_pkey PRIMARY KEY (element_id, attribute_id, branch_id)
);

CREATE TABLE {0}formula_dependences_data_metadata
(
  element_id BIGINT NOT NULL,
  attribute_id BIGINT NOT NULL,
  branch_id BIGINT NOT NULL,
  CONSTRAINT {0}formula_dependences_data_metadata_pkey PRIMARY KEY (element_id, attribute_id, branch_id)
);

CREATE TABLE {0}branch_persistent_classes
(
  class_name text NOT NULL,--повна назва класа
  table_name text NOT NULL,
  persistent_exists BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT {0}branch_persistent_classes_pkey PRIMARY KEY (class_name)  
);

CREATE TABLE {0}branch_persistent_fields
(
  class_name text NOT NULL,
  field_name text NOT NULL,--Назва поля об’єкта
  column_name text NOT NULL,--Назва стовпчика в Базі даних
  field_type int NOT NULL, --Тип поля
  field_exists BOOLEAN NOT NULL DEFAULT TRUE,
  field_primary BOOLEAN NOT NULL,
  CONSTRAINT {0}branch_persistent_fields_pkey PRIMARY KEY (class_name, field_name),
  CONSTRAINT {0}branch_persistent_fields_class_name_fkey FOREIGN KEY (class_name)
      REFERENCES {0}branch_persistent_classes (class_name)
);

