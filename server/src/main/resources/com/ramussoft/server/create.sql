CREATE TABLE users
(
  "login" character(60) NOT NULL,
  "name" text NOT NULL,
  "password" character(60) NOT NULL,
  CONSTRAINT users_pkey PRIMARY KEY ("login")
);

CREATE TABLE groups
(
  group_name character(60) NOT NULL,
  user_element_id bigint,
  CONSTRAINT groups_pkey PRIMARY KEY (group_name),
  CONSTRAINT groups_user_element_id_fkey FOREIGN KEY (user_element_id)
      REFERENCES ramus_elements (element_id)
);

CREATE TABLE users_groups
(
  "login" character(60) NOT NULL,
  group_name character(60) NOT NULL,
  CONSTRAINT users_groups_pkey PRIMARY KEY ("login", group_name),
  CONSTRAINT users_groups_group_name_fkey FOREIGN KEY (group_name)
      REFERENCES groups (group_name),
  CONSTRAINT users_groups_login_fkey FOREIGN KEY ("login")
      REFERENCES users ("login")
);

INSERT INTO groups (group_name) VALUES ('admin');
INSERT INTO users ("login", "name", "password") VALUES ('admin', 'Administrator', 'admin');
INSERT INTO users_groups ("login", group_name) VALUES ('admin', 'admin');

CREATE TABLE group_qualifier_access
(
  group_name character(60) NOT NULL,
  qualifier_id bigint NOT NULL,
  CONSTRAINT group_qualifier_access_pkey PRIMARY KEY (group_name, qualifier_id),
  CONSTRAINT group_qualifier_access_group_name_fkey FOREIGN KEY (group_name)
      REFERENCES groups (group_name)
);

CREATE TABLE glossary
(
  term character(120) NOT NULL,
  meaning text,
  CONSTRAINT glossary_pkey PRIMARY KEY (term)
);

CREATE TABLE news
(
  date_time timestamp NOT NULL,
  data text,
  title text,
  CONSTRAINT news_pkey PRIMARY KEY (date_time)
);

CREATE TABLE binary_data
(
  "name" text NOT NULL,
  data bytea,
  CONSTRAINT binary_data_pkey PRIMARY KEY ("name")
);