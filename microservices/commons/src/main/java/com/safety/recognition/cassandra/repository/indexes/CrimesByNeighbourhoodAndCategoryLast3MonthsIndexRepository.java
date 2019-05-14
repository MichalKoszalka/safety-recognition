package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryIndexKey;
import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryLast3MonthsIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository extends CassandraRepository<CrimesByNeighbourhoodAndCategoryLast3MonthsIndex, CrimesByNeighbourhoodAndCategoryIndexKey> {
}