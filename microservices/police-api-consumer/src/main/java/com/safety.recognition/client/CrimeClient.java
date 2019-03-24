package com.safety.recognition.client;

import data.police.uk.model.crime.Crime;

import java.util.List;

public interface CrimeClient {

    List<Crime> getCrimes();

}
