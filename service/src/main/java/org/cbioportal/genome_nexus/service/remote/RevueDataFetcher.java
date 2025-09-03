package org.cbioportal.genome_nexus.service.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.genome_nexus.model.VuesJsonRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Component
public class RevueDataFetcher {

    private static final Log LOG = LogFactory.getLog(RevueDataFetcher.class);
    public static VuesJsonRecord[] getRevueData(String revueUrl) {
        RestTemplate restTemplate = new RestTemplate();
        // Fetch the JSON data
        LOG.debug("Fetching reVUE data from URL: " + revueUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(revueUrl, String.class);
        String json = response.getBody(); 
        Gson gson = new Gson();
        try {
            // Convert the JSON data to a VuesJsonRecord[] array
            VuesJsonRecord[] records = gson.fromJson(json, VuesJsonRecord[].class);
            LOG.debug("Successfully parsed reVUE data, number of records: " + (records != null ? records.length : 0));
            return records;
        } catch (JsonSyntaxException e) {
            LOG.error("Error getting JSON file: " + e.getMessage());
            return null;
        }
    }

}
