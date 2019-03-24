package com.safety.recognition.service;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.client.NeighbourhoodBoundaryClient;
import com.safety.recognition.client.NeighbourhoodClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NeighbourhoodLoader {

    private final NeighbourhoodClient neighbourhoodClient;

    private final NeighbourhoodBoundaryClient neighbourhoodBoundaryClient;

    private final CassandraRepository<Neighbourhood, String> neighbourhoodCassandraRepository;

    @Autowired
    public NeighbourhoodLoader(NeighbourhoodClient neighbourhoodClient, NeighbourhoodBoundaryClient neighbourhoodBoundaryClient, CassandraRepository<Neighbourhood, String> neighbourhoodCassandraRepository) {
        this.neighbourhoodClient = neighbourhoodClient;
        this.neighbourhoodBoundaryClient = neighbourhoodBoundaryClient;
        this.neighbourhoodCassandraRepository = neighbourhoodCassandraRepository;
    }

    public List<Neighbourhood> loadNeighbourhoods() {
        return neighbourhoodCassandraRepository.saveAll(neighbourhoodClient.getNeigbourhoods().stream().map(this::createNeighbourhood).collect(Collectors.toList()));
    }

    private Neighbourhood createNeighbourhood(data.police.uk.model.neighbourhood.Neighbourhood policeNeighbourhood) {
        var neighbourhood = new Neighbourhood();
        neighbourhood.setId(policeNeighbourhood.getId());
        neighbourhood.setName(policeNeighbourhood.getName());
        neighbourhood.setBoundary(neighbourhoodBoundaryClient.getBoundariesByNeighbourhood(policeNeighbourhood.getId()));
        return neighbourhood;
    }

}
