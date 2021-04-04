## Audire

A generic audit framework that defines Audit Ingestion pipelines, and an Audit Service that exposes the enriched audits
adhering to a generic schema for rich querying.

### Usage

- Clone the project.

- If setting up in local, ensure to point the config to the correct bootstrap servers. The topics need to be created
  or `auto.create`
  should be enabled


- Produce events in Kafka topics either through a Debezium connector or using a local console producer

- If you want to view the processed changelog events in the topic, create a Elasticsearch sink connector in local.

- The Elasticsearch server should be created with an index with the mapping present
  in `src/main/resources/es_mapping.json`
