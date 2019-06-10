package com.safety.recognition.cassandra.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

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

}
