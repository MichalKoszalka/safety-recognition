package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonLast3MonthsIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesForLondonLast3MonthsIndexRepository extends CassandraRepository<CrimesForLondonLast3MonthsIndex, String> {
}
