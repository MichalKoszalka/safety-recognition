package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhood;
import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhoodKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeByNeighbourhoodRepository extends CassandraRepository<CrimeByNeighbourhood, CrimeByNeighbourhoodKey> {
}
