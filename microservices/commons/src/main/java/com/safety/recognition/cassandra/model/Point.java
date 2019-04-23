package com.safety.recognition.cassandra.model;

import com.datastax.driver.core.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@UserDefinedType("point")
public class Point {

    @CassandraType(type = DataType.Name.DOUBLE)
    private Double latitude;

    @CassandraType(type = DataType.Name.DOUBLE)
    private Double longitude;

    @Override
    public String toString() {
        return String.format("%s,%s", latitude, longitude);
    }
}
