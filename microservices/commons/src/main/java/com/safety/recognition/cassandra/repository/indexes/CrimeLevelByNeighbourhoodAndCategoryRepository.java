package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreetAndCategory;
import com.safety.recognition.cassandra.model.indexes.NeighbourhoodAndCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeLevelByNeighbourhoodAndCategoryRepository extends CassandraRepository<CrimeLevelByNeighbourhoodAndCategory, NeighbourhoodAndCategoryKey> {

}
