package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesByStreetLast3MonthsIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesByStreetLast3MonthsIndexRepository extends CassandraRepository<CrimesByStreetLast3MonthsIndex, String> {
}
