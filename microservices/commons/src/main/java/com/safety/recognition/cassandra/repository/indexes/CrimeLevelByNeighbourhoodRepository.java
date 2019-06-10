package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhood;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeLevelByNeighbourhoodRepository extends CassandraRepository<CrimeLevelByNeighbourhood, String> {

}
