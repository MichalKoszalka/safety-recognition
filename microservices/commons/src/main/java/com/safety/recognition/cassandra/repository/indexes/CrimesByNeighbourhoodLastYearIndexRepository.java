package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodLastYearIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByNeighbourhoodLastYearIndexRepository extends CassandraRepository<CrimesByNeighbourhoodLastYearIndex, String> {
}
