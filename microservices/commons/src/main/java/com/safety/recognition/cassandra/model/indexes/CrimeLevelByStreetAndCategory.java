package com.safety.recognition.cassandra.model.indexes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateKeyDeserializer;
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
public class CrimeLevelByStreetAndCategory {

    @PrimaryKey
    private StreetAndCategoryKey key;

    @Column
    @JsonDeserialize(keyUsing = LocalDateKeyDeserializer.class)
    private Map<LocalDate, Long> crimesByMonth;

}
