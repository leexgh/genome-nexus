package org.cbioportal.genome_nexus.persistence.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.cbioportal.genome_nexus.model.AggregateSourceInfo;
import org.cbioportal.genome_nexus.model.GenomeNexusInfo;
import org.cbioportal.genome_nexus.model.SourceVersionInfo;
import org.cbioportal.genome_nexus.model.VEPInfo;
import org.cbioportal.genome_nexus.persistence.AnnotationVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class SourceInfoRepository {

    @Autowired
    private GenomeNexusInfo genomeNexusInfo;

    @Autowired
    private VEPInfo vepInfo;

    @Autowired
    private AnnotationVersionRepository annotationVersionRepository;

    @Bean
    public GenomeNexusInfo genomeNexusInfo(@Value("${genomenexus.server.version:NA}") String serverVersion, @Value("${spring.mongodb.embedded.version:NA}") String dbVersion) {
        return new GenomeNexusInfo(serverVersion, dbVersion);
    }

    @Bean
    public VEPInfo vepInfo(@Value("${gn_vep.server.version:NA}") String serverVersion, @Value("${vep.url:}") String vepUrl) throws MalformedURLException {
        URL url = new URL(vepUrl);
        RestTemplate restTemplate = new RestTemplate();
        String vepVersion = "";
        String comment = null;
        try {
            vepVersion = String.valueOf(restTemplate.getForObject(url.getProtocol() + "://" + url.getAuthority() + "/info/software", VepVersionObject.class).get("release"));
        } catch (RestClientException e) {
            comment = "Error fetching VEP version";
            vepVersion = "NA";
        }
        
        return new VEPInfo(serverVersion, vepVersion, comment);
    }

    public AggregateSourceInfo getAggregateSourceInfo() {
        AggregateSourceInfo aggregateSourceInfo = new AggregateSourceInfo(genomeNexusInfo, vepInfo);
        List<SourceVersionInfo> sourceVersionInfos = annotationVersionRepository.findAll();
        aggregateSourceInfo.setAnnotationSourcesInfo(sourceVersionInfos);
        return aggregateSourceInfo;
    }

    private static class VepVersionObject extends HashMap<String, Integer> {}
}