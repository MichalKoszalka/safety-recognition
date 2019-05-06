package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonByCategoryLastYearIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesForLondonByCategoryLastYearIndexRepository extends CassandraRepository<CrimesForLondonByCategoryLastYearIndex, String> {
}
