package org.cbioportal.genome_nexus.util;

public class IsoformOverrideSource {
    public static final String ENSEMBL = "ensembl";
    public static final String GENOME_NEXUS = "genome_nexus";
    public static final String UNIPROT = "uniprot";
    public static final String MSKCC = "mskcc";

    /**
     * Normalises a requested isoform override source. Anything unusable -- null, blank, or a
     * source this build does not know -- resolves to {@link #MSKCC}, which is the documented
     * default for every endpoint that takes an isoformOverrideSource parameter. This is the
     * single place that decision is made; do not re-implement it per call site.
     */
    public static String getOrDefault(String isoformOverrideSource) {
        String normalizedSource = isoformOverrideSource == null ?
            MSKCC : isoformOverrideSource.trim().toLowerCase();

        return (
            normalizedSource.equals(ENSEMBL) ||
            normalizedSource.equals(GENOME_NEXUS) ||
            normalizedSource.equals(UNIPROT)
        ) ? normalizedSource : MSKCC;
    }
}
