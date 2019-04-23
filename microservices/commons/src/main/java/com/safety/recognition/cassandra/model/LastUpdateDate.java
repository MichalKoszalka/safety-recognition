package com.safety.recognition.cassandra.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Table
public class LastUpdateDate {

    @PrimaryKey
    private LocalDate policeApiLastUpdate;

    @Column
    private LocalDate safetyRecognitionLastUpdate;

}
