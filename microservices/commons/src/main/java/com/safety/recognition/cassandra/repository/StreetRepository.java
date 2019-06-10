package com.safety.recognition.cassandra.repository;

import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface StreetRepository extends CassandraRepository<Street, StreetKey> {
}
