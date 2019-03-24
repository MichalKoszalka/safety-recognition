package com.safety.recognition.cassandra.model;

import com.datastax.driver.core.DataType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@Setter
@Getter
@UserDefinedType("point")
public class Point {

    @CassandraType(type = DataType.Name.DOUBLE)
    private Double latitude;

    @CassandraType(type = DataType.Name.DOUBLE)
    private Double longitude;

}
