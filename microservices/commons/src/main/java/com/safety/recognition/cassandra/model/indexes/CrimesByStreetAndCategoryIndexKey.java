package com.safety.recognition.cassandra.model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
@Getter
@Setter
@AllArgsConstructor
public class CrimesByStreetAndCategoryIndexKey {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String street;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String category;
}
