package com.safety.recognition.cassandra.model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.util.Map;

@Table
@AllArgsConstructor
@Getter
@Setter
public class CrimesByNeighbourhoodAndCategoryLast3MonthsIndex {

    @PrimaryKey
    private CrimesByNeighbourhoodAndCategoryIndexKey key;
    @Column
    private Integer numberOfCrimes;
    @Column
    private Integer medianByMonth;
    @Column
    private Integer meanByMonth;
    @Column
    private Integer meanByWeek;
    @Column
    private Integer meanByDay;
    @Column
    private Map<LocalDate, Long> numberOfCrimesByMonth;

}
