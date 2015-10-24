CREATE TABLE {0}formulas
(
  element_id bigint NOT NULL,
  attribute_id bigint NOT NULL,
  autorecalculate boolean,
  formula text,
  CONSTRAINT {0}formulas_pkey PRIMARY KEY (element_id, attribute_id),
  CONSTRAINT {0}formulas_attribute_id_fkey FOREIGN KEY (attribute_id)
      REFERENCES {0}attributes (attribute_id),
  CONSTRAINT {0}formulas_element_id_fkey FOREIGN KEY (element_id)
      REFERENCES {0}elements (element_id)
);

CREATE TABLE {0}formula_dependences
(
  source_element_id bigint NOT NULL,
  source_attribute_id bigint NOT NULL,
  element_id bigint NOT NULL,
  attribute_id bigint NOT NULL,
  CONSTRAINT {0}furmula_dependences_pkey PRIMARY KEY (source_element_id, source_attribute_id, element_id, attribute_id),
  CONSTRAINT {0}furmula_dependences_element_id_fkey FOREIGN KEY (element_id, attribute_id)
      REFERENCES {0}formulas (element_id, attribute_id),
  CONSTRAINT {0}furmula_dependences_source_attribute_id_fkey FOREIGN KEY (source_attribute_id)
      REFERENCES {0}attributes (attribute_id),
  CONSTRAINT {0}furmula_dependences_source_element_id_fkey FOREIGN KEY (source_element_id)
      REFERENCES {0}elements (element_id)
);