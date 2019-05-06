package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonAllTimeIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesForLondonAllTimeIndexRepository extends CassandraRepository<CrimesForLondonAllTimeIndex, String> {
}
