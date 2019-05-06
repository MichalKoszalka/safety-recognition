package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonByCategoryAllTimeIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesForLondonByCategoryAllTimeIndexRepository extends CassandraRepository<CrimesForLondonByCategoryAllTimeIndex, String> {
}
