package com.safety.recognition.cassandra.repository;

import com.safety.recognition.cassandra.model.PreprocessedCrime;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrimeRepository extends CassandraRepository<PreprocessedCrime, Long> {
}
