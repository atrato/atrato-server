CREATE TABLE appPackage (
  owner VARCHAR(100),
  name VARCHAR(100),
  version VARCHAR(20),
  description VARCHAR(200),
  metaInfo TEXT,
  file BLOB,
  PRIMARY KEY (owner, name, version)
)
