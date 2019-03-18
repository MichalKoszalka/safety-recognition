package com.safety.recognition.cassandra.model;

import model.Crime;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.function.BiConsumer;

@Table("crime")
public class PreprocessedCrime {

    @PrimaryKey
    private Long id;
    @Column
    private String category;
    @Column
    private String persistentId;
    @Column
    private Month month;
    @Column
    private String locationType;
    @Column
    private String locationSubtype;
    @Column
    private BigDecimal longitute;
    @Column
    private BigDecimal latitude;
    @Column
    private Long streetId;
    @Column
    private String street;
    @Column
    private String context;
    @Column
    private String outcomeStatusCategory;
    @Column
    private LocalDate outcomeStatusDate;

    public PreprocessedCrime(Crime crime) {
        this.id = crime.getId();
        this.category = crime.getCategory();
        this.persistentId = crime.getPersistentId();
//        this.month = Month.valueOf(crime.getMonth());
        this.locationType = crime.getLocationType();
        this.locationSubtype = crime.getLocationSubtype();
//        this.longitute = crime.getLocation().getLongitude();
//        this.latitude = crime.getLocation().getLatitude();
//        this.streetId = crime.getLocation().getStreet().getId();
//        this.street = crime.getLocation().getStreet().getName();
        this.context = crime.getContext();
//        this.outcomeStatusCategory = crime.getOutcomeStatus().getCategory();
//        this.outcomeStatusDate = LocalDate.parse(crime.getOutcomeStatus().getDate());
    }
}
