package com.safety.recognition.kafka.messages;

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

    @Override
    public String toString() {
        return "StreetAndCategory{" +
                "streetAndNeighbourhood=" + streetAndNeighbourhood +
                ", category='" + category + '\'' +
                '}';
    }
}
