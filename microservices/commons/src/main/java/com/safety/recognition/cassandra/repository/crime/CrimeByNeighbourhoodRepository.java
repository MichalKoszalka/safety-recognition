package com.safety.recognition.cassandra.repository.crime;

import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhood;
import com.safety.recognition.cassandra.model.crime.CrimeByNeighbourhoodKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrimeByNeighbourhoodRepository extends CassandraRepository<CrimeByNeighbourhood, CrimeByNeighbourhoodKey> {

    List<CrimeByNeighbourhood> findCrimeByKeyNeighbourhoodAndKeyCrimeDateAfter(String Neighbourhood, LocalDate date);
    List<CrimeByNeighbourhood> findCrimeByKeyNeighbourhood(String Neighbourhood);

}
