package com.safety.recognition;


import com.safety.recognition.cassandra.model.PreprocessedCrime;
import com.safety.recognition.cassandra.repository.CrimeRepository;
import data.police.uk.model.crime.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
public class CrimesProcessorApplication {


    @Autowired CrimeRepository crimeRepository;

    public static void main(String[] args) {
        SpringApplication.run(CrimesProcessorApplication.class, args);
    }

    @KafkaListener(topics = "new_crimes", containerFactory = "kafkaCrimesListenerContainerFactory")
    public void crimesListener(Crime crime) {
        PreprocessedCrime preprocessedCrime = new PreprocessedCrime(crime);
        crimeRepository.save(preprocessedCrime);
    }
}
