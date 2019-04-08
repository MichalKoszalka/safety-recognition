package com.safety.recognition.service;

import com.safety.recognition.cassandra.model.LastUpdateDate;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.client.UpdateDateClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LastUpdateDateLoader {

    private final LastUpdateDateRepository lastUpdateDateRepository;

    private final UpdateDateClient updateDateClient;

    @Autowired
    public LastUpdateDateLoader(LastUpdateDateRepository lastUpdateDateRepository, UpdateDateClient updateDateClient) {
        this.lastUpdateDateRepository = lastUpdateDateRepository;
        this.updateDateClient = updateDateClient;
    }

    public LastUpdateDate loadLastUpdateDate() {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        return lastUpdateDate.orElse(createNewLastUpdateDate());
    }

    private LastUpdateDate createNewLastUpdateDate() {
        return new LastUpdateDate(updateDateClient.getUpdateDate(), LocalDate.now());
    }

}
