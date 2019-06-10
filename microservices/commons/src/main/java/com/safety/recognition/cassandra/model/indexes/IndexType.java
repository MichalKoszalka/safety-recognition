package com.safety.recognition.cassandra.model.indexes;

public enum IndexType {

    LONDON("London"),
    LONDON_AND_CATEGORY("London and category"),
    NEIGHBOURHOOD("Neighbourhood"),
    NEIGHBOURHOOD_AND_CATEGORY("Neighbourhood and category"),
    STREET("Street"),
    STREET_AND_CATEGORY("Street and category");

    private final String name;

    IndexType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
