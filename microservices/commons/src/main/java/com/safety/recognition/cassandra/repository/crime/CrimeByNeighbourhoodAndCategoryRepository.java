package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhoodAndCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeByNeighbourhoodAndCategoryRepository extends CassandraRepository<CrimeByNeighbourhoodAndCategory, CrimeByNeighbourhoodAndCategoryKey> {
}
