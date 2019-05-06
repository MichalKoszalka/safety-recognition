package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByStreetAndCategory;
import com.safety.recognition.cassandra.model.crime.CrimeByStreetAndCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeByStreetAndCategoryRepository extends CassandraRepository<CrimeByStreetAndCategory, CrimeByStreetAndCategoryKey> {
}
