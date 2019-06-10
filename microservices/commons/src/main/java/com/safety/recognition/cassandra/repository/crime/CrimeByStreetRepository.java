package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByStreet;
import com.safety.recognition.cassandra.model.crime.CrimeByStreetKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrimeByStreetRepository extends CassandraRepository<CrimeByStreet, CrimeByStreetKey> {

    List<CrimeByStreet> findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCrimeDateAfter(String street, String neighbourhood, LocalDate date);
    List<CrimeByStreet> findCrimeByKeyStreetAndKeyNeighbourhood(String street, String neighbourhood);

}
