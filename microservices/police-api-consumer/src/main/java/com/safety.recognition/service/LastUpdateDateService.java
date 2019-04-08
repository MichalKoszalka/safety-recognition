package com.safety.recognition.service;

import com.safety.recognition.cassandra.model.LastUpdateDate;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.client.UpdateDateClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class LastUpdateDateService {

    private final LastUpdateDateRepository lastUpdateDateRepository;

    @Autowired
    public LastUpdateDateService(LastUpdateDateRepository lastUpdateDateRepository, UpdateDateClient updateDateClient) {
        this.lastUpdateDateRepository = lastUpdateDateRepository;
    }

    public Optional<LastUpdateDate> loadLastUpdateDate() {
        return lastUpdateDateRepository.findAll().stream().findFirst();
    }

    public void merge(LocalDate policeApiUpdateDate) {
        lastUpdateDateRepository.deleteAll();
        lastUpdateDateRepository.save(new LastUpdateDate(policeApiUpdateDate, LocalDate.now()));
    }

}
