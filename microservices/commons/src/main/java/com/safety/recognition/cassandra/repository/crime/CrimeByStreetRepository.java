package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByStreet;
import com.safety.recognition.cassandra.model.crime.CrimeByStreetKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CrimeByStreetRepository extends CassandraRepository<CrimeByStreet, CrimeByStreetKey> {
}
