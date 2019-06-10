package com.safety.recognition.cassandra.kafka.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StreetAndNeighbourhood {

    private String street;
    private String neighbourhood;

}
