package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.HighestCrimeLevel;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface HighestCrimeLevelRepository extends CassandraRepository<HighestCrimeLevel, Long> {
}
