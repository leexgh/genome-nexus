This section is for developer or engineer user

### How do we pick canonical transcript for each isoform
1. Download `ensembl_biomart_geneids.txt` from `retrieve_biomart_tables.R`
2. Build `$(TMP_DIR)/ensembl_canonical_data.txt` from `download_transcript_info_from_ensembl.py` , `ensembl_biomart_geneids.txt` is input file
    1. check if it’s grch37 or grch38, fetching data from corresponding website ([https://grch37.rest.ensembl.org](https://grch37.rest.ensembl.org/) or [https://rest.ensembl.org](https://rest.ensembl.org/)), endpoint is `/lookup/id` , giving chunks of list of `transcript_stable_id` from `ensembl_biomart_geneids.txt`
    2. the return object is `ensembl_transcript_response`, key is `transcript_stable_id`, add three new columns:
        1. `is_canonical` = ensembl_transcript_response[transcript]['is_canonical']
        2. `protein_stable_id` = ensembl_transcript_response[transcript]['Translation']['id']
        3. `protein_length` = ensembl_transcript_response[transcript]['Translation']['length']
    3. Save `ensembl_transcript_response` as tmp files, append these files, sort and remove duplication, then `ensembl_canonical_data.txt` is created
3. Dateframes and sets:
    - Read `ensembl_canonical_data.txt` as dataframe `transcript_info_df`
    - read `hgnc_complete_set_2023-04-01.txt`  as dataframe `hgnc_df`
    - create synonyms set from hgnc_df.synonyms, name it as `syns`
    - create previous_symbols set from hgnc_df.previous_symbols, name it as `previous_symbols`
    - create hugo symbol dataframe from hgnc_df['approved_symbol'].unique(), name it as `hugos`
4. Ignore certain genes: find a set of genes that are in `transcript_info_df.hgnc_symbol` but not in the combined set of `hugos`, `syns`, and `previous_symbols`. Check if new genes need to be added to `ignored_genes.txt` by looking at the print. If there are new genes need to be added, add it and rerun.
5. Set `hgnc_symbol` as index to `transcript_info_df` , copy a new dataframe the same as `transcript_info_df` but uses `gene_stable_id` as index, name as `ensembl_table_indexed_by_gene_stable_id` (to increase performance when searching `gene_stable_id`)
6. For each hugo symbol in `hugos`, get the following value, and save to a dataframe called **`one_transcript_per_hugo_symbol`**:
    1. ensembl_canonical_gene: It tries to find the HGNC gene rows corresponding to the given **`hgnc_symbol`** from **`hgnc_df`**. 
        - If only one row in **`hgnc_df`** is found, it attempts to get the canonical transcript from  `ensembl_table_indexed_by_gene_stable_id` by searching for the corresponding **`hgnc_df.ensembl_gene_id`**.
            - If there is only one row in the `ensembl_table_indexed_by_gene_stable_id`, it returns the value from the `gene_stable_id` ****field ****along with a string indicating that this is the "ensembl only one transcript."
            - If there are multiple rows, it sorts the rows of `ensembl_table_indexed_by_gene_stable_id` based on the columns 'is_canonical', 'protein_length', and 'gene_stable_id' in descending order. It returns the value from the specified `gene_stable_id` in the first (top) row of the sorted `ensembl_table_indexed_by_gene_stable_id` along with a string indicating that this is the "ensembl longest" transcript.
            - If no rows found, that means it couldn't find any transcripts by `ensembl_gene_id`, switch to searching by `hgnc_symbol` . It tries to find rows by `hgnc_symbol`  in `transcript_info_df`
                - If there is only one row in the `transcript_info_df`, it returns the value from the `gene_stable_id` ****field ****along with a string indicating that this is the "ensembl only one transcript."
                - If there are multiple rows, it sorts the rows of `transcript_info_df` based on the columns 'is_canonical', 'protein_length', and 'gene_stable_id' in descending order. It returns the value from the specified `gene_stable_id` in the first (top) row of the sorted `transcript_info_df` along with a string indicating that this is the "ensembl longest" transcript.
                - If no rows found, that means it couldn't find any transcripts by `hgnc_symbol` either, so return `nan`
        - For other cases, like having more than 1 rows found, or no rows found, raise exception
    2. ensembl_canonical_transcript and ensembl_canonical_transcript_explanation
        - Same as above, but instead of returning `gene_stable_id` field, it returns value from `transcript_stable_id` field
    3. genome_nexus_overrides_transcript and genome_nexus_overrides_transcript_explanation: It iterates through the override tables (`isoform_overrides_genome_nexus`), attempting to find an override for the given HGNC symbol. 
        - If multiple overrides are found for a given `hgnc_symbol`, it takes the first one.
        - If one override is found, it extracts the transcript and the corresponding override table name.
        - If no override is found, it calls the **`get_ensembl_canonical_transcript_id_from_hgnc_then_ensembl`** function to get the Ensembl canonical transcript information (same as `b` section).
    4. uniprot_overrides_transcript and uniprot_overrides_transcript_explanation: It iterates through the override tables (in the order of `isoform_overrides_genome_nexus` , `isoform_overrides_uniprot`), attempting to find an override for the given HGNC symbol. 
        - Append second `soform_overrides_uniprot` right after`isoform_overrides_genome_nexus`  to be a single overrides_tables, the searching will be from top to the bottom in the giving order
        - If multiple overrides are found for a given `hgnc_symbol`, it takes the first one
        - If one override is found, it extracts the transcript and the corresponding override table name.
        - If no override is found, it calls the **`get_ensembl_canonical_transcript_id_from_hgnc_then_ensembl`** function to get the Ensembl canonical transcript information (same as `b` section).
    5. mskcc_overrides_transcript, mskcc_overrides_transcript_explanation: It iterates through the override tables (in the order of `isoform_overrides_oncokb_grch37` , `isoform_overrides_at_mskcc_grch37` , `isoform_overrides_genome_nexus` , `isoform_overrides_uniprot`), attempting to find an override for the given HGNC symbol. 
        - Append isoform_override files in the giving order together
        - If multiple overrides are found for a given `hgnc_symbol`, it takes the first one
        - If one override is found, it extracts the transcript and the corresponding override table name.
        - If no override is found, it calls the **`get_ensembl_canonical_transcript_id_from_hgnc_then_ensembl`** function to get the Ensembl canonical transcript information (same as `b` section).
7. merges two DataFrames (`one_transcript_per_hugo_symbol` and `hgnc_df`) based on a common column. The column used for merging is 'hgnc_symbol' in the one_transcript_per_hugo_symbol and 'approved_symbol' in the hgnc_df. Save the merged dataframe to ensembl_biomart_canonical_transcripts_per_hgnc.txt


### Why my gene has no transcript
There are several reasons:
Check this file:
- GRCh37: https://github.com/genome-nexus/genome-nexus-importer/blob/master/data/grch37_ensembl92/export/ensembl_biomart_canonical_transcripts_per_hgnc.txt
- GRCh38: https://github.com/genome-nexus/genome-nexus-importer/blob/master/data/grch38_ensembl95/export/ensembl_biomart_canonical_transcripts_per_hgnc.txt
1. not in `canonical_transcript_per_hgnc`:
    gene is not in ensembl, so it’s not in `ensembl_biomart_geneids.txt`, can check hgnc website
2. in `canonical_transcript_per_hgnc`, but transcript id is empty
3. in `canonical_transcript_per_hgnc`, has transcript id, but not in `ensembl.biomart_transcripts`


### Why the canonical transcript is not what I expect?
Here is how we find canonical transcript by hugo symbol:
- getCanonicalEnsemblTranscriptByHugoSymbol
- this.ensemblRepository.findOneByHugoSymbolIgnoreCase → findOneCanonicalByHugoSymbol
    - first check approved symbol: check `ensembl.canonical_transcript_per_hgnc` collection, the `hgnc_symbol` field in the database collection should exactly match giving `hugoSymbol`
    - if `hugoSymbol` is not in `hgnc_symbol` , try find in `previous_symbols` field of db collection
    - if `hugoSymbol` is not in `previous_symbols` , try find in `synonyms` field of db collection
    - return as `ensemblCanonical`
- if `ensemblCanonical` is not null, it should contains 4 transcript ids (mskcc, genomeNexus, enselbl, uniprot), get transcriptId by the isoformOverride,  find the transcriptId in `transcript_stable_id` field in `ensembl.biomart_transcripts` collection.
- If transcript found in `ensembl.biomart_transcripts`, return as canonical transcript
Please double check if you give the desired isoform.


### Why my annotation is failed?
There are couple of reasons that an annotation could fail. This is the logic we determine if the annotation is failed:
1. No `providedReferenceAllele` (length is 0, not `-`). `providedReferenceAllele` is directly parsed from input.
2. `providedReferenceAllele` is not correct. If the `providedReferenceAllele` doesn't match the response from VEP, that means this response is not for the giving input.
3. Length of `providedReferenceAllele` doesn't equal to `responseReferenceAllele`, that means this response is not for the giving input.