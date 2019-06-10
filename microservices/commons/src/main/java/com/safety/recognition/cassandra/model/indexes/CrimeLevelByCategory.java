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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrimeLevelByCategory {

    @PrimaryKey
    private String category;

    @Column
    private Map<LocalDate, Long> crimesByMonth;

}
