package com.safety.recognition.cassandra.model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.util.Map;

@Table
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class HighestCrimeLevelByCategory {

    @PrimaryKey
    private String category;

    @Column
    private Map<LocalDate, Long> highestLevelForNeighbourhoodByMonth;

    @Column
    private Map<LocalDate, Long> highestLevelForStreetByMonth;

    @Column
    private Map<LocalDate, Long> highestLevelForLondonByMonth;

}
