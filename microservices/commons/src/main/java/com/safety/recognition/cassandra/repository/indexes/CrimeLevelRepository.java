package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimeLevel;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByCategory;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeLevelRepository extends CassandraRepository<CrimeLevel, String> {

}
