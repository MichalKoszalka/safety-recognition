package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.CrimesByStreetLastYearIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByStreetLastYearIndexRepository extends CassandraRepository<CrimesByStreetLastYearIndex, StreetKey> {
}
