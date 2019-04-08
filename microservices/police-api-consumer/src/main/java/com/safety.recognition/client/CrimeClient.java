package com.safety.recognition.client;

import com.safety.recognition.cassandra.model.Neighbourhood;
import data.police.uk.model.crime.Crime;

import java.time.LocalDate;
import java.util.List;

public interface CrimeClient {

    List<Crime> getCrimes(Neighbourhood neighbourhood, LocalDate updateDate);

}
