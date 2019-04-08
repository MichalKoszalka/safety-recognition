package com.safety.recognition.cassandra.repository;

import com.safety.recognition.cassandra.model.LastUpdateDate;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LastUpdateDateRepository extends CassandraRepository<LastUpdateDate, LocalDate> {
}
