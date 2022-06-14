package io.confluent.csid.kafka.streams.faa.swim.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class StringVisitor extends Visitor<String> {
  @Override
  public String visit(JsonNode node) {
    TextNode textNode = (TextNode) node;
    return textNode.asText();
  }
}
