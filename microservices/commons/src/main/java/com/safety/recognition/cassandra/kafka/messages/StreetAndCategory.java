package com.safety.recognition.cassandra.kafka.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StreetAndCategory {

    StreetAndNeighbourhood streetAndNeighbourhood;
    String category;

}
