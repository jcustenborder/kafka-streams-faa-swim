# Introduction

This project contains a proof of concept data to parse data from the FAA data projects.



# Configuration

Open of the build outputs for this project is a docker container that can be utilised to run the application. Currently, 
all the configuration is passed in through configuration variables. For example here are the input and output topics.


```
FAA_FLIGHTPLAN_TOPIC=FlightPlanParsed
FAA_INPUT_TOPIC=faa_input_avro
FAA_TRACK_AND_FLIGHTPLAN_TOPIC=TrackAndFlightPlanParsed
FAA_TRACK_TOPIC=TrackParsed
FAA_BOOTSTRAP_SERVERS=localhost:9092
```

# Input data 

The data for this project is provided by the [Confluent Solace Connector](https://www.confluent.io/hub/confluentinc/kafka-connect-solace-source). The solace connector is used 
with an AvroConverter to write the data to the `faa_input_avro` topic. This application uses the data that was written by the connector as input and converts the xml to avro.

