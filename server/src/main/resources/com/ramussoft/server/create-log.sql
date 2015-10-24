CREATE TABLE {0}qualifiers_log
(
  log_id BIGINT NOT NULL,
  attribute_id BIGINT,
  element_id BIGINT,
  qualifier_id BIGINT,
  change_type integer,
  old_value text,
  new_value text,
  user_login text,
  when_done timestamp default NOW(),
  CONSTRAINT qualifiers_log PRIMARY KEY (log_id)
);

CREATE SEQUENCE {0}qualifiers_log_seq;
