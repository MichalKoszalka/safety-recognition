package com.safety.recognition.cassandra.repository;

import com.safety.recognition.cassandra.model.FetchingStatus;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface FetchingStatusRepository extends CassandraRepository<FetchingStatus, Long> {
}
