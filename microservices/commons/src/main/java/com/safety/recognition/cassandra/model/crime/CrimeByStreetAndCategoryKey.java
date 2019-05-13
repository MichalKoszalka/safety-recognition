package com.safety.recognition.cassandra.model.crime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDate;

@PrimaryKeyClass
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CrimeByStreetAndCategoryKey {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String street;
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String category;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private LocalDate crimeDate;

}
