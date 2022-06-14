package io.confluent.csid.kafka.streams.faa.swim.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class DoubleVisitor extends Visitor<Double> {
  @Override
  public Double visit(JsonNode node) {
    TextNode textNode = (TextNode) node;
    return Double.parseDouble(textNode.asText());
  }
}
