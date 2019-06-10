package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.CrimesByStreetAllTimeIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByStreetAllTimeIndexRepository extends CassandraRepository<CrimesByStreetAllTimeIndex, StreetKey> {
}
