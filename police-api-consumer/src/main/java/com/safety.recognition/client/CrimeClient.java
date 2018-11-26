package com.safety.recognition.client;

import com.safety.recognition.model.Crime;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CrimeClient {

    List<Crime> getCrimes();

}
