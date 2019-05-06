package recognition.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import recognition.calculator.IndexesCalculator;

@Service
public class CalculateIndexesListener {

    private static final Logger LOG = LoggerFactory.getLogger(CalculateIndexesListener.class);

    private final IndexesCalculator indexesCalculator;

    @Autowired
    public CalculateIndexesListener(IndexesCalculator indexesCalculator) {
        this.indexesCalculator = indexesCalculator;
    }

    @KafkaListener(topics = "calculate_indexes", containerFactory = "kafkaCalculateIndexesListenerFactory")
    public void crimesListener() {
        LOG.info("Starting calculating indexes");
        indexesCalculator.calculateIndexes();
        LOG.info("Finished calculating indexes");
    }

}
