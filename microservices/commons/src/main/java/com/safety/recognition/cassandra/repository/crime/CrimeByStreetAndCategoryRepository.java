package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByStreetAndCategory;
import com.safety.recognition.cassandra.model.crime.CrimeByStreetAndCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrimeByStreetAndCategoryRepository extends CassandraRepository<CrimeByStreetAndCategory, CrimeByStreetAndCategoryKey> {

    List<CrimeByStreetAndCategory> findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(String street, String neighbourhood, String category, LocalDate date);
    List<CrimeByStreetAndCategory> findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCategory(String street, String neighbourhood, String category);

}
