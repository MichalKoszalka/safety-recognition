package com.safety.recognition.cassandra.model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

@PrimaryKeyClass
@AllArgsConstructor
@Getter
@Setter
public class CrimesByNeighbourhoodAndCategoryIndexKey {


    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String neighbourhood;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String category;

}
