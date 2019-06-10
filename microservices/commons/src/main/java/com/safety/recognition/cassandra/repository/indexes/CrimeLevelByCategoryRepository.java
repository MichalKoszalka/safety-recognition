package com.safety.recognition.cassandra.repository.indexes;

import com.safety.recognition.cassandra.model.indexes.CrimeLevelByCategory;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeLevelByCategoryRepository extends CassandraRepository<CrimeLevelByCategory, String> {

}
