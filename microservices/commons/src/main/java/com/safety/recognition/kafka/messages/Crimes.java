package com.safety.recognition.kafka.messages;

import data.police.uk.model.crime.Crime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Crimes implements Serializable {

    private List<Crime> crimeList;

}
