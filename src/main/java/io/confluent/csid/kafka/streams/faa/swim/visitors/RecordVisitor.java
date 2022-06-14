package io.confluent.csid.kafka.streams.faa.swim.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordVisitor extends Visitor<GenericRecord> {
  private static final Logger log = LoggerFactory.getLogger(RecordVisitor.class);
  final Schema schema;
  final Map<String, Visitor<?>> visitors;

  static Schema nonNullSchema(Schema schema) {
    Schema result;

    if (Schema.Type.UNION == schema.getType()) {
      List<Schema> schemas = schema.getTypes().stream().filter(t -> t.getType() != Schema.Type.NULL)
          .collect(Collectors.toList());
      if (schemas.isEmpty()) {
        throw new IllegalStateException("No non null schema was found for " + schema.toString(true));
      }
      if (schemas.size() > 1) {
        throw new IllegalStateException("More than one non null schema was found for " + schema.toString(true));
      }
      result = schemas.get(0);
    } else {
      result = schema;
    }
    return result;
  }


  public RecordVisitor(Schema schema) {
    this.schema = schema;
    Map<String, Visitor<?>> visitors = new HashMap<>();

    for (Schema.Field field : schema.getFields()) {
      Schema nonNullSchema = nonNullSchema(field.schema());
      Visitor<?> visitor;

      switch (nonNullSchema.getType()) {
        case INT:
          visitor = new IntVisitor();
          break;
        case LONG:
          if (LogicalTypes.timestampMillis().equals(nonNullSchema.getLogicalType())) {
            visitor = new TimestampVisitor();
          } else {
            visitor = new LongVisitor();
          }
          break;
        case STRING:
          visitor = new StringVisitor();
          break;
        case DOUBLE:
          visitor = new DoubleVisitor();
          break;
        case RECORD:
          visitor = new RecordVisitor(nonNullSchema);
          break;
        default:
          throw new UnsupportedOperationException(
              String.format("Field '%s' is not supported. %s", field.name(), nonNullSchema.toString(true))
          );
      }
      visitors.put(field.name(), visitor);

    }

    this.visitors = visitors;
  }

  @Override
  public GenericRecord visit(JsonNode node) {
    ObjectNode objectNode = (ObjectNode) node;
    GenericRecord genericRecord = new GenericData.Record(this.schema);

    for (Map.Entry<String, Visitor<?>> kvp : this.visitors.entrySet()) {
      log.trace("visit() - processing '{}'", kvp.getKey());
      String fieldName = kvp.getKey();
      JsonNode fieldNode = objectNode.get(fieldName);
      Object recordValue;
      if (null != fieldNode && !fieldNode.isNull()) {
        Visitor<?> visitor = kvp.getValue();
        recordValue = visitor.visit(fieldNode);
      } else {
        recordValue = null;
      }
      genericRecord.put(fieldName, recordValue);
    }

    return genericRecord;
  }
}
