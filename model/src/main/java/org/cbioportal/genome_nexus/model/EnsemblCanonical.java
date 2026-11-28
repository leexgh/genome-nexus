package org.cbioportal.genome_nexus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.cbioportal.genome_nexus.util.IsoformOverrideSource;

/**
 * Ensembl canonical mappings from Hugo Symbol to Ensembl Gene and Transcript.
 */
@Document(collection="ensembl.canonical_transcript_per_hgnc")
public class EnsemblCanonical
{
    @Field(value="hgnc_symbol")
    private String hugoSymbol;
    @Field(value="ensembl_canonical_gene")
    private String ensemblCanonicalGeneId;
    @Field(value="ensembl_canonical_transcript")
    private String ensemblCanonicalTranscriptId;
    @Field(value="genome_nexus_canonical_transcript")
    private String genomeNexusCanonicalTranscriptId;
    @Field(value="uniprot_canonical_transcript")
    private String uniprotCanonicalTranscriptId;
    @Field(value="mskcc_canonical_transcript")
    private String mskccCanonicalTranscriptId;
    // A gene can legitimately have more than one canonical transcript (e.g. CDKN2A's two
    // reading frames, p14ARF and p16INK4A). The importer writes the primary one above and
    // any others here, comma separated.
    @Field(value="mskcc_canonical_transcript_additional")
    private String mskccAdditionalCanonicalTranscriptIds;
    // Extra gene symbols that name one specific transcript of this gene, as
    // "alias:transcriptId" pairs (e.g. "CDKN2Ap14:ENST00000579755, CDKN2Ap16:ENST00000304494").
    // Source-neutral: an alias names a reading frame, not a particular isoform override source.
    @Field(value="canonical_transcript_alias")
    private String canonicalTranscriptAliases;
    @Field(value="ensembl_canonical_transcript_version")
    private String ensemblCanonicalTranscriptVersion;
    @Field(value="genome_nexus_canonical_transcript_version")
    private String genomeNexusCanonicalTranscriptVersion;
    @Field(value="uniprot_canonical_transcript_version")
    private String uniprotCanonicalTranscriptVersion;
    @Field(value="mskcc_canonical_transcript_version")
    private String mskccCanonicalTranscriptVersion;
    @Field(value="hgnc_id")
    private String hgncId;
    @Field(value="approved_name")
    private String approvedName;
    @Field(value="status")
    private String status;
    @Field(value="previous_symbols")
    private String previousSymbols; // comma separated
    @Field(value="synonyms")
    private String synonyms; // comma separated
    @Field(value="accession_numbers")
    private String accessionNumbers; // comma separated
    @Field(value="refseq_ids")
    private String refseqIds;
    @Field(value="uniprot_id")
    private String uniprotId;
    @Field(value="entrez_gene_id")
    private String entrezGeneId;

    private static String[] splitByComma(String input) {
        Pattern p = Pattern.compile("\\s*,\\s*");
        String[] output = p.split(input);
        if (output.length == 0 || (output.length == 1 && output[0].equals(""))) {
            output = null;
        }
        return output;
    }

    /**
     * Canonical transcript for the requested isoform override source. An omitted, blank or
     * unrecognised source resolves to mskcc via {@link IsoformOverrideSource#getOrDefault},
     * so every caller gets the same default rather than each deciding for itself.
     */
    public String getCanonicalTranscriptId(String isoformOverrideSource) {
        switch (IsoformOverrideSource.getOrDefault(isoformOverrideSource)) {
            case IsoformOverrideSource.GENOME_NEXUS:
                return this.genomeNexusCanonicalTranscriptId;
            case IsoformOverrideSource.ENSEMBL:
                return this.ensemblCanonicalTranscriptId;
            case IsoformOverrideSource.UNIPROT:
                return this.uniprotCanonicalTranscriptId;
            case IsoformOverrideSource.MSKCC:
            default:
                return this.mskccCanonicalTranscriptId;
        }
    }

    /**
     * Canonical transcripts beyond the primary one returned by
     * {@link #getCanonicalTranscriptId(String)}. Only the mskcc source carries these today;
     * every other source returns an empty array. An omitted or unrecognised source resolves
     * to mskcc, matching {@link #getCanonicalTranscriptId(String)}.
     */
    public String[] getAdditionalCanonicalTranscriptIds(String isoformOverrideSource) {
        if (IsoformOverrideSource.MSKCC.equals(IsoformOverrideSource.getOrDefault(isoformOverrideSource))
            && this.mskccAdditionalCanonicalTranscriptIds != null) {
            String[] additional = EnsemblCanonical.splitByComma(this.mskccAdditionalCanonicalTranscriptIds);
            if (additional != null) {
                return additional;
            }
        }
        return new String[0];
    }

    /**
     * Transcript named by an alias gene symbol (e.g. "CDKN2Ap14"), or null when the symbol is
     * not an alias for this gene. Matching is case insensitive. Returns null on documents
     * imported before the alias column existed, so callers fall back to the canonical pick.
     */
    public String getCanonicalTranscriptIdForAliasSymbol(String symbol) {
        if (symbol == null || this.canonicalTranscriptAliases == null) {
            return null;
        }
        String[] pairs = EnsemblCanonical.splitByComma(this.canonicalTranscriptAliases);
        if (pairs == null) {
            return null;
        }
        for (String pair : pairs) {
            int separator = pair.indexOf(':');
            if (separator <= 0 || separator == pair.length() - 1) {
                continue;
            }
            if (pair.substring(0, separator).trim().equalsIgnoreCase(symbol.trim())) {
                return pair.substring(separator + 1).trim();
            }
        }
        return null;
    }

    /** Alias gene symbols defined for this gene; empty when the document has none. */
    public String[] getAliasSymbols() {
        if (this.canonicalTranscriptAliases == null) {
            return new String[0];
        }
        String[] pairs = EnsemblCanonical.splitByComma(this.canonicalTranscriptAliases);
        if (pairs == null) {
            return new String[0];
        }
        List<String> symbols = new ArrayList<>();
        for (String pair : pairs) {
            int separator = pair.indexOf(':');
            if (separator > 0) {
                symbols.add(pair.substring(0, separator).trim());
            }
        }
        return symbols.toArray(new String[0]);
    }

    public String getCanonicalTranscriptAliases() {
        return this.canonicalTranscriptAliases;
    }

    public void setCanonicalTranscriptAliases(String canonicalTranscriptAliases) {
        this.canonicalTranscriptAliases = canonicalTranscriptAliases;
    }

    public String getMskccAdditionalCanonicalTranscriptIds() {
        return this.mskccAdditionalCanonicalTranscriptIds;
    }

    public void setMskccAdditionalCanonicalTranscriptIds(String mskccAdditionalCanonicalTranscriptIds) {
        this.mskccAdditionalCanonicalTranscriptIds = mskccAdditionalCanonicalTranscriptIds;
    }

    public String getHugoSymbol() {
        return this.hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String[] getSynonyms() {
        return EnsemblCanonical.splitByComma(this.synonyms);
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String[] getPreviousSymbols() {
        return EnsemblCanonical.splitByComma(this.previousSymbols);
    }

    public void setPreviousSymbols(String previousSymbols) {
        this.previousSymbols = previousSymbols;
    }

    public String getEnsemblCanonicalGeneId() {
        return this.ensemblCanonicalGeneId;
    }

    public void setEnsemblCanonicalGeneId(String ensemblCanonicalGeneId) {
        this.ensemblCanonicalGeneId = ensemblCanonicalGeneId;
    }

    public String getEnsemblCanonicalTranscriptId() {
        return this.ensemblCanonicalTranscriptId;
    }

    public void setEnsemblCanonicalTranscriptId(String ensemblCanonicalTranscriptId) {
        this.ensemblCanonicalTranscriptId = ensemblCanonicalTranscriptId;
    }

    public String getEntrezGeneId() {
        // remove .0 for backwards compatibility with databases where
        // entrez_gene_id field is a float
        if (this.entrezGeneId != null && this.entrezGeneId.endsWith(".0")) {
            return this.entrezGeneId.substring(0, this.entrezGeneId.length() - 2);
        } else {
            return this.entrezGeneId;
        }
    }

    public void setEntrezGeneId(String entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHgncId() {
        if (this.hgncId != null && this.hgncId.startsWith("HGNC:")) {
            return this.hgncId.substring(5);
        }
        return this.hgncId;
    }

    public void setHgncId(String hgncId) {
        this.hgncId = hgncId;
    }

    public String getEnsemblCanonicalTranscriptVersion() {
        return this.ensemblCanonicalTranscriptVersion;
    }

    public void setEnsemblCanonicalTranscriptVersion(String ensemblCanonicalTranscriptVersion) {
        this.ensemblCanonicalTranscriptVersion = ensemblCanonicalTranscriptVersion;
    }

    public String getGenomeNexusCanonicalTranscriptVersion() {
        return this.genomeNexusCanonicalTranscriptVersion;
    }

    public void setGenomeNexusCanonicalTranscriptVersion(String genomeNexusCanonicalTranscriptVersion) {
        this.genomeNexusCanonicalTranscriptVersion = genomeNexusCanonicalTranscriptVersion;
    }

    public String getUniprotCanonicalTranscriptVersion() {
        return this.uniprotCanonicalTranscriptVersion;
    }

    public void setUniprotCanonicalTranscriptVersion(String uniprotCanonicalTranscriptVersion) {
        this.uniprotCanonicalTranscriptVersion = uniprotCanonicalTranscriptVersion;
    }

    public String getMskccCanonicalTranscriptVersion() {
        return this.mskccCanonicalTranscriptVersion;
    }

    public void setMskccCanonicalTranscriptVersion(String mskccCanonicalTranscriptVersion) {
        this.mskccCanonicalTranscriptVersion = mskccCanonicalTranscriptVersion;
    }
}
