package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryIndexKey;
import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryLastYearIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByNeighbourhoodAndCategoryLastYearIndexRepository extends CassandraRepository<CrimesByNeighbourhoodAndCategoryLastYearIndex, CrimesByNeighbourhoodAndCategoryIndexKey> {
}
