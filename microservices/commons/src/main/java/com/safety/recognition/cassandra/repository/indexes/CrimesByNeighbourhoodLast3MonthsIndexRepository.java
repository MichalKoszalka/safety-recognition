package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodLast3MonthsIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByNeighbourhoodLast3MonthsIndexRepository extends CassandraRepository<CrimesByNeighbourhoodLast3MonthsIndex, String> {
}
