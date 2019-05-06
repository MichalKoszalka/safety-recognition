package com.safety.recognition.cassandra.repository;

import com.safety.recognition.cassandra.model.CrimeCategory;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrimeCategoryRepository extends CassandraRepository<CrimeCategory, String> {
}
