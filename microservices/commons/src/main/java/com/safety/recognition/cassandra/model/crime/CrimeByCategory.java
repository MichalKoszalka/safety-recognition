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
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrimeByCategory {

    @Column
    private Long id;
    @PrimaryKey
    private CrimeByCategoryKey key;
    @Column
    private Point location;
}
