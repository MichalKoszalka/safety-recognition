package com.safety.recognition.cassandra.repository.predictions;

import com.safety.recognition.cassandra.model.predictions.CrimePrediction;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimePredictionRepository extends CassandraRepository<CrimePrediction, String> {

}
