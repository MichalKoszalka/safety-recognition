package com.safety.recognition.client;

import com.safety.recognition.cassandra.model.CrimeCategory;

import java.util.List;

public interface CrimeCategoryClient {

    List<CrimeCategory> getCrimeCategories();

}
