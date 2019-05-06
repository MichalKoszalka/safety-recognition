package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonByCategoryLast3MonthsIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesForLondonByCategoryLast3MonthsIndexRepository extends CassandraRepository<CrimesForLondonByCategoryLast3MonthsIndex, String> {
}
