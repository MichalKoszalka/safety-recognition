package com.safety.recognition.cassandra.model.crime;

import com.safety.recognition.cassandra.model.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
@Setter
@Getter
@AllArgsConstructor
public class CrimeByStreetAndCategory {

    @Column
    private Long id;
    @PrimaryKey
    private CrimeByStreetAndCategoryKey key;
    @Column
    private Point location;

}
