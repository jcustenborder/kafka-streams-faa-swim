package io.confluent.csid.kafka.streams.faa.swim;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AppConfig extends AbstractConfig {
  static final String INPUT_TOPIC_CONF = "input.topic";
  static final String TRACK_AND_FLIGHT_PLAN_TOPIC = "track.and.flightplan.topic";
  static final String FLIGHT_PLAN_TOPIC = "flightplan.topic";
  static final String TRACK_TOPIC = "track.topic";

  public final String inputTopic;
  public final String trackAndFlightPlanTopic;
  public final String flightPlanTopic;
  public final String trackTopic;
  public final Map<String, String> topicLocations;

  public AppConfig(Map<?, ?> originals) {
    super(config(), originals);
    this.inputTopic = getString(INPUT_TOPIC_CONF);
    this.trackAndFlightPlanTopic = getString(TRACK_AND_FLIGHT_PLAN_TOPIC);
    this.trackTopic = getString(TRACK_TOPIC);
    this.flightPlanTopic = getString(FLIGHT_PLAN_TOPIC);

    Map<String, String> topicLocations = new HashMap<>();
    topicLocations.put("io.confluent.data.faa.TrackRecord", this.trackTopic);
    topicLocations.put("io.confluent.data.faa.FlightPlan", this.flightPlanTopic);
    this.topicLocations = Collections.unmodifiableMap(topicLocations);
  }


  public static ConfigDef config() {
    return new ConfigDef()
        .define(INPUT_TOPIC_CONF, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "topic to read from")
        .define(TRACK_AND_FLIGHT_PLAN_TOPIC, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "topic to write to")
        .define(FLIGHT_PLAN_TOPIC, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "topic to write to")
        .define(TRACK_TOPIC, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "topic to write to");
  }
}
