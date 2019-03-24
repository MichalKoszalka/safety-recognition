package com.safety.recognition.cassandra.repository;

import com.safety.recognition.cassandra.model.Neighbourhood;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeighbourhoodRepository extends CassandraRepository<Neighbourhood, String> {
}
