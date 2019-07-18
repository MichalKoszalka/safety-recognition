package com.safety.recognition.cassandra.repository.predictions;

import com.safety.recognition.cassandra.model.predictions.CrimePredictionByNeighbourhood;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimePredictionByNeighbourhoodRepository extends CassandraRepository<CrimePredictionByNeighbourhood, String> {

}
