package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByCategory;
import com.safety.recognition.cassandra.model.crime.CrimeByCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeByCategoryRepository extends CassandraRepository<CrimeByCategory, CrimeByCategoryKey> {
}
