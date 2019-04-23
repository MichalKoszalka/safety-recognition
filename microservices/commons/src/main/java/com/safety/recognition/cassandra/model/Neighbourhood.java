package com.safety.recognition.cassandra.model;

import com.datastax.driver.core.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table
public class Neighbourhood {

    @PrimaryKey
    private String id;
    @Column
    private String name;

    @Column
    private List<Point> boundary;

}
