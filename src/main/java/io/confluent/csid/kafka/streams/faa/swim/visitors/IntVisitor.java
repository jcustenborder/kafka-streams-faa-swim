package io.confluent.csid.kafka.streams.faa.swim.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class IntVisitor extends Visitor<Integer> {
  @Override
  public Integer visit(JsonNode node) {
    TextNode textNode = (TextNode) node;
    return Integer.parseInt(textNode.asText());
  }
}
