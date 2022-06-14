package io.confluent.csid.kafka.streams.faa.swim;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class App {
  private static final Logger log = LoggerFactory.getLogger(App.class);

  static <T> Serde<T> configureSerde(Serde<T> serde, Map<String, ?> settings, boolean isKey) {
    serde.configure(settings, isKey);
    return serde;
  }

  static Map<String, ?> loadSettings() {
    Map<String, ?> settings = System.getenv().entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("FAA_"))
        .collect(Collectors.toMap(
            e -> e.getKey().toLowerCase().substring(4).replace('_', '.'),
            Map.Entry::getValue
        ));
    return settings;
  }

  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    Map<String, ?> settings = loadSettings();
    properties.putAll(settings);
    AppConfig appConfig = new AppConfig(settings);

    StreamsBuilder streamsBuilder = new StreamsBuilder();
    Serde<GenericRecord> genericRecordSerde = configureSerde(new GenericAvroSerde(), settings, false);
    //"faa_input_avro"
    //"TrackAndFlightPlanParsed"

    KStream<String, GenericRecord> trackAndFlightPlanUnparsed = streamsBuilder.stream(appConfig.inputTopic,
        Consumed.with(
            Serdes.String(),
            genericRecordSerde
        )
    );
    KStream<String, GenericRecord> trackAndFlightPlanAvro = trackAndFlightPlanUnparsed.flatMap(new TrackAndFlightPlanMapper());
    trackAndFlightPlanAvro.to(
        appConfig.trackAndFlightPlanTopic,
        Produced.with(
            Serdes.String(),
            genericRecordSerde
        )
    );
    trackAndFlightPlanAvro.flatMapValues((ValueMapper<GenericRecord, Iterable<GenericRecord>>) trackAndFlightPlanRecord -> {
      List<GenericRecord> output = new ArrayList<>();
      GenericRecord track = (GenericRecord) trackAndFlightPlanRecord.get("track");
      if (null != track) {
        output.add(track);
      }
      GenericRecord flightPlan = (GenericRecord) trackAndFlightPlanRecord.get("flightPlan");
      if (null != flightPlan) {
        output.add(flightPlan);
      }
      return output;
    }).to((s, genericRecord, recordContext) -> appConfig.topicLocations.get(genericRecord.getSchema().getFullName()), Produced.with(
        Serdes.String(),
        genericRecordSerde
    ));


    Topology topology = streamsBuilder.build();
    KafkaStreams kafkaStreams = new KafkaStreams(topology, properties);
    kafkaStreams.start();
    Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
  }

}
