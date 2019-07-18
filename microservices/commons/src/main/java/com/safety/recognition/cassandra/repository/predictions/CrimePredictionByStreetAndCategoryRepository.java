package com.safety.recognition.cassandra.repository.predictions;

import com.safety.recognition.cassandra.model.indexes.StreetAndCategoryKey;
import com.safety.recognition.cassandra.model.predictions.CrimePredictionByStreetAndCategory;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimePredictionByStreetAndCategoryRepository extends CassandraRepository<CrimePredictionByStreetAndCategory, StreetAndCategoryKey> {

}
