package com.safety.recognition.cassandra.model.indexes;

public enum IndexType {

    LONDON("LONDON"),
    LONDON_AND_CATEGORY("LONDON_AND_CATEGORY"),
    NEIGHBOURHOOD("NEIGHBOURHOOD"),
    NEIGHBOURHOOD_AND_CATEGORY("NEIGHBOURHOOD_AND_CATEGORY"),
    STREET("STREET"),
    STREET_AND_CATEGORY("STREET_AND_CATEGORY");

    private final String name;

    IndexType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }




}
