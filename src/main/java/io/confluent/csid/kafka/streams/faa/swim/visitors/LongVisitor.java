package io.confluent.csid.kafka.streams.faa.swim.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class LongVisitor extends Visitor<Long> {
  @Override
  public Long visit(JsonNode node) {
    TextNode textNode = (TextNode) node;
    return Long.parseLong(textNode.asText());
  }
}
