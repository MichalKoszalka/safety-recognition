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
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CrimeByStreet {

    @Column
    private Long id;
    @PrimaryKey
    private CrimeByStreetKey key;
    @Column
    private Point location;

}
