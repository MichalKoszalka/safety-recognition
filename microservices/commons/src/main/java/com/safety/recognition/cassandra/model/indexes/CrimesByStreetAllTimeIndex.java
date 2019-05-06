package com.safety.recognition.cassandra.model.indexes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
@AllArgsConstructor
@Getter
@Setter
public class CrimesByStreetAllTimeIndex {

    @PrimaryKey
    private String street;
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

}
