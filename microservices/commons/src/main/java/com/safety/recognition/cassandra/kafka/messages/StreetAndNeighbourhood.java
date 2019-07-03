package com.safety.recognition.cassandra.kafka.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StreetAndNeighbourhood implements Serializable {

    private String street;
    private String neighbourhood;

}
