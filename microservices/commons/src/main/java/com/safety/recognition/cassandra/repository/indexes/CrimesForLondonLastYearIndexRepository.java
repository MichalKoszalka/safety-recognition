package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonLastYearIndex;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimesForLondonLastYearIndexRepository extends CassandraRepository<CrimesForLondonLastYearIndex    , String> {
}
