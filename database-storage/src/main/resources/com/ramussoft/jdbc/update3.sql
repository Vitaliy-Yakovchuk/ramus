CREATE TABLE persistent_classes
(
  class_name text NOT NULL,--повна назва класа
  table_name text NOT NULL,
  persistent_exists BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT persistent_classes_pkey PRIMARY KEY (class_name)  
);

CREATE TABLE persistent_fields
(
  class_name text NOT NULL,
  field_name text NOT NULL,--Назва поля об’єкта
  column_name text NOT NULL,--Назва стовпчика в Базі даних
  field_type int NOT NULL, --Тип поля
  field_exists BOOLEAN NOT NULL DEFAULT TRUE,
  field_primary BOOLEAN NOT NULL,
  CONSTRAINT persistent_fields_pkey PRIMARY KEY (class_name, field_name),
  CONSTRAINT persistent_fields_class_name_fkey FOREIGN KEY (class_name)
      REFERENCES persistent_classes (class_name)
);
