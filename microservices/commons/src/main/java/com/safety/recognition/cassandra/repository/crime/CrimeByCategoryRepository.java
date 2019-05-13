package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByCategory;
import com.safety.recognition.cassandra.model.crime.CrimeByCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrimeByCategoryRepository extends CassandraRepository<CrimeByCategory, CrimeByCategoryKey> {

    List<CrimeByCategory> findCrimeByKeyCategoryAndKeyCrimeDateAfter(String street, LocalDate date);
    List<CrimeByCategory> findCrimeByKeyCategory(String street);

}
