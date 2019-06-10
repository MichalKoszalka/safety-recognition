package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhood;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreet;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeLevelByStreetRepository extends CassandraRepository<CrimeLevelByStreet, StreetKey> {

}
