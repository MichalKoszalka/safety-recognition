package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.Crime;
import com.safety.recognition.cassandra.model.crime.CrimeKey;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrimeRepository extends CassandraRepository<Crime, CrimeKey> {

    @AllowFiltering
    List<Crime> findCrimesByKeyCrimeDateAfter(LocalDate date);
}
