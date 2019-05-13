package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhoodAndCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrimeByNeighbourhoodAndCategoryRepository extends CassandraRepository<CrimeByNeighbourhoodAndCategory, CrimeByNeighbourhoodAndCategoryKey> {

    List<CrimeByNeighbourhoodAndCategory> findCrimeByKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(String neighbourhood, String category, LocalDate date);
    List<CrimeByNeighbourhoodAndCategory> findCrimeByKeyNeighbourhoodAndKeyCategory(String neighbourhood, String category);

}
