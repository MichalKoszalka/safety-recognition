package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryLast3MonthsIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByNeighbourhoodLast3MonthsIndexRepository extends CassandraRepository<CrimesByNeighbourhoodAndCategoryLast3MonthsIndex, String> {
}
