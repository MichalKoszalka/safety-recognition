package com.safety.recognition.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Crime {

    private Long id;
    private String category;
    private Long persistent_id;
    private String month;
    private String location_type;
    private String location_subtype;
    private Location location;
    private String context;
    private OutcomeStatus outcomeStatus;

}