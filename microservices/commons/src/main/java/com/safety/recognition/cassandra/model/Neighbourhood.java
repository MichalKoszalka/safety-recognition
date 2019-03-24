package com.safety.recognition.cassandra.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;

@Getter
@Setter
@Table
public class Neighbourhood {

    @PrimaryKeyColumn
    private String id;
    @Column
    private String name;

    @Column
    private List<Point> boundary;

}
