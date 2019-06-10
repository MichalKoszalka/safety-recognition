package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreetAndCategory;
import com.safety.recognition.cassandra.model.indexes.NeighbourhoodAndCategoryKey;
import com.safety.recognition.cassandra.model.indexes.StreetAndCategoryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeLevelByStreetAndCategoryRepository extends CassandraRepository<CrimeLevelByStreetAndCategory, StreetAndCategoryKey> {

}
