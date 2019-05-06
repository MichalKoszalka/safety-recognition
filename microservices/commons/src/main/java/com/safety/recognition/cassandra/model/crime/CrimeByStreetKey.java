package com.safety.recognition.cassandra.model.crime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDate;

@PrimaryKeyClass
@Setter
@Getter
@AllArgsConstructor
public class CrimeByStreetKey {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String street;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private LocalDate crimeDate;

}
