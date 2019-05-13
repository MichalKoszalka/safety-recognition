package com.safety.recognition.cassandra.model.crime;

import com.safety.recognition.cassandra.model.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CrimeByNeighbourhoodAndCategory {

    @Column
    private Long id;
    @PrimaryKey
    private CrimeByNeighbourhoodAndCategoryKey key;
    @Column
    private Point location;

}
