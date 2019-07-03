package com.safety.recognition.cassandra.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@PrimaryKeyClass
public class StreetKey {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String street;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String neighbourhood;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreetKey streetKey = (StreetKey) o;
        return Objects.equals(street, streetKey.street) &&
                Objects.equals(neighbourhood, streetKey.neighbourhood);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, neighbourhood);
    }
}
