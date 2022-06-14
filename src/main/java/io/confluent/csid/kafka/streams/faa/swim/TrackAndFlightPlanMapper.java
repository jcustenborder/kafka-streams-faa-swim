package io.confluent.csid.kafka.streams.faa.swim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.confluent.csid.kafka.streams.faa.swim.visitors.RecordVisitor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TrackAndFlightPlanMapper implements KeyValueMapper<Object, GenericRecord, Iterable<? extends KeyValue<? extends String, ? extends GenericRecord>>> {
  private final ObjectMapper objectMapper = new XmlMapper();
  private final RecordVisitor visitor;

  public TrackAndFlightPlanMapper() {
    try (InputStream inputStream = this.getClass().getResourceAsStream("TrackAndFlightPlanRecord.avsc")) {
      Schema.Parser parser = new Schema.Parser();
      Schema schema = parser.parse(inputStream);
      this.visitor = new RecordVisitor(schema);
    } catch (IOException ex) {
      throw new IllegalStateException(
          "Exception thrown while creating visitor", ex
      );
    }
  }

  KeyValue<? extends String, ? extends GenericRecord> parse(JsonNode node) {
    GenericRecord genericRecord = this.visitor.visit(node);

    GenericRecord enhancedData = (GenericRecord) genericRecord.get("enhancedData");
    String key = null;
    if (null != enhancedData) {
      String eramGufi = (String) enhancedData.get("eramGufi");
      if (null != eramGufi) {
        key = eramGufi;
      }
    }
    return new KeyValue<>(key, genericRecord);
  }

  @Override
  public Iterable<? extends KeyValue<? extends String, ? extends GenericRecord>> apply(Object inputKey, GenericRecord inputValue) {
    try {
      Utf8 text = (Utf8) inputValue.get("text");
      ObjectNode objectNode = this.objectMapper.readValue(text.getBytes(), ObjectNode.class);
      List<KeyValue<? extends String, ? extends GenericRecord>> records = new ArrayList<>();
      JsonNode recordNode = objectNode.get("record");
      if (recordNode.isArray()) {
        ArrayNode arrayNode = (ArrayNode) recordNode;
        for (JsonNode node : arrayNode) {
          KeyValue<? extends String, ? extends GenericRecord> kv = parse(node);
          records.add(kv);
        }
      } else if (recordNode.isObject()) {
        KeyValue<? extends String, ? extends GenericRecord> kv = parse(recordNode);
        records.add(kv);
      }
      return records;
    } catch (IOException e) {
      throw new IllegalStateException(
          "Exception thrown parsing xml", e
      );
    }
  }
}
