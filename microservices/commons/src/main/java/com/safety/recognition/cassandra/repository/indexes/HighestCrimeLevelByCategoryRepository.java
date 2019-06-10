package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.HighestCrimeLevelByCategory;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface HighestCrimeLevelByCategoryRepository extends CassandraRepository<HighestCrimeLevelByCategory, String> {
}
