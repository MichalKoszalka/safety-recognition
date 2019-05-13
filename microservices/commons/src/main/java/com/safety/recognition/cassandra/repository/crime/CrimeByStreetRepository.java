package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByStreet;
import com.safety.recognition.cassandra.model.crime.CrimeByStreetKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrimeByStreetRepository extends CassandraRepository<CrimeByStreet, CrimeByStreetKey> {

    List<CrimeByStreet> findCrimeByKeyStreetAndKeyCrimeDateAfter(String street, LocalDate date);
    List<CrimeByStreet> findCrimeByKeyStreet(String street);

}
