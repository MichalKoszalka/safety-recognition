package com.safety.recognition.cassandra.model.predictions;

import com.safety.recognition.cassandra.model.StreetKey;
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
public class CrimePredictionByStreet {

    @PrimaryKey
    private StreetKey key;

    @Column
    private Map<LocalDate, Long> crimesByMonth;

}
