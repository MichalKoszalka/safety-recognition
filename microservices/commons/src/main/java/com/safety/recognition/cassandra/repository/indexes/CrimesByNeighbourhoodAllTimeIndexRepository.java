package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAllTimeIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByNeighbourhoodAllTimeIndexRepository extends CassandraRepository<CrimesByNeighbourhoodAllTimeIndex, String> {
}
