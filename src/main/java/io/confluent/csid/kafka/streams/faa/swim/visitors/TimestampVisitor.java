package io.confluent.csid.kafka.streams.faa.swim.visitors;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public class TimestampVisitor extends Visitor<Long> {
  @Override
  public Long visit(JsonNode node) {
    Instant instant = Instant.parse(node.asText());
    return instant.toEpochMilli();
  }
}
