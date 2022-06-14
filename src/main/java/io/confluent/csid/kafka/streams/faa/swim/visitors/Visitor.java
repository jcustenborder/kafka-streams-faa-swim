package io.confluent.csid.kafka.streams.faa.swim.visitors;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class Visitor<T> {

  public abstract T visit(JsonNode node);
}
