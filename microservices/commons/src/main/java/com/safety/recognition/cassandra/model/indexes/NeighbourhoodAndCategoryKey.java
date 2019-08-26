package com.safety.recognition.cassandra.model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.Objects;

@PrimaryKeyClass
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NeighbourhoodAndCategoryKey {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String neighbourhood;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourhoodAndCategoryKey that = (NeighbourhoodAndCategoryKey) o;
        return Objects.equals(neighbourhood, that.neighbourhood) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbourhood, category);
    }
}
