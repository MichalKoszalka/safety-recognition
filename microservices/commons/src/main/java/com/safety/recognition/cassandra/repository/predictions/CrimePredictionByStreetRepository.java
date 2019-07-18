package com.safety.recognition.cassandra.repository.predictions;

import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.predictions.CrimePredictionByStreet;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimePredictionByStreetRepository extends CassandraRepository<CrimePredictionByStreet, StreetKey> {

}
