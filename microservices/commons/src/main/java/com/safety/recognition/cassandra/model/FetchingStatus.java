package com.safety.recognition.cassandra.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table
public class FetchingStatus {

    @PrimaryKey
    private Long key;

    private boolean categoriesFetched;

    private boolean neighbourhoodsFetched;

    private CrimesFetchingStatus crimesFetchingStatus;

    @Override
    public String toString() {
        return "FetchingStatus{" +
                "key=" + key +
                ", categoriesFetched=" + categoriesFetched +
                ", neighbourhoodsFetched=" + neighbourhoodsFetched +
                ", crimesFetchingStatus=" + crimesFetchingStatus +
                '}';
    }

    public boolean isReadyForInitialFetching() {
        return categoriesFetched && neighbourhoodsFetched && CrimesFetchingStatus.NOT_FETCHED == crimesFetchingStatus;
    }

    public boolean isReadyForMonthlyFetching() {
        return categoriesFetched && neighbourhoodsFetched && CrimesFetchingStatus.FETCHED == crimesFetchingStatus;
    }
}
