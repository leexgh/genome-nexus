package org.cbioportal.genome_nexus.component.annotation;

import org.cbioportal.genome_nexus.model.IntegerRange;
import org.cbioportal.genome_nexus.model.TranscriptConsequence;
import org.cbioportal.genome_nexus.model.VariantAnnotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProteinPositionResolverTest
{
    @InjectMocks
    private ProteinPositionResolver proteinPositionResolver;

    @Mock
    private ProteinChangeResolver proteinChangeResolver;

    @Test
    public void resolveProteinChangeForDup()
    {
        VariantAnnotation annotation = new VariantAnnotation();
        annotation.setVariantId("4:g.105243050insGGCAGCGGATGATGAAGGTGTTGGGCCGGG");

        TranscriptConsequence transcriptWithDup = new TranscriptConsequence();
        transcriptWithDup.setProteinStart(78);
        transcriptWithDup.setProteinEnd(78);

        Mockito.when(this.proteinChangeResolver.resolveHgvspShort(annotation, transcriptWithDup)).thenReturn("P68_C77dup");

        IntegerRange proteinPos = proteinPositionResolver.resolve(annotation, transcriptWithDup);

        assertEquals("Protein start position should be derived from protein change in case of dup",
            proteinPos.getStart(), new Integer(68));

        assertEquals("Protein end position should be derived from protein change in case of dup",
            proteinPos.getEnd(), new Integer(77));

        Mockito.when(this.proteinChangeResolver.resolveHgvspShort(annotation, transcriptWithDup)).thenReturn("P*_C*dup");

        proteinPos = proteinPositionResolver.resolve(annotation, transcriptWithDup);

        assertEquals("Protein position should fall back to the reported values in case of invalid dup",
            proteinPos.getStart(), new Integer(78));
    }

    @Test
    public void resolveProteinChangeForNonDup()
    {
        VariantAnnotation annotation = new VariantAnnotation();
        annotation.setVariantId("4:g.105243050insGGCAGCGGATGATGAAGGTGTTGGGCCGGG");

        TranscriptConsequence transcriptWithDup = new TranscriptConsequence();
        transcriptWithDup.setProteinStart(78);
        transcriptWithDup.setProteinEnd(78);

        Mockito.when(this.proteinChangeResolver.resolveHgvspShort(annotation, transcriptWithDup)).thenReturn("P68_C77");

        IntegerRange proteinPos = proteinPositionResolver.resolve(annotation, transcriptWithDup);

        assertEquals("Protein start position should NOT be derived from protein change in regular cases",
            proteinPos.getStart(), new Integer(78));

        assertEquals("Protein end position should NOT be derived from protein change in regular cases",
            proteinPos.getEnd(), new Integer(78));
    }


    @Test
    public void resolveProteinChangeForSplice()
    {
        VariantAnnotation annotation = new VariantAnnotation();
        annotation.setVariantId("17:g.7577018C>A");

        TranscriptConsequence transcriptSplice = new TranscriptConsequence();
        transcriptSplice.setProteinStart(78);
        transcriptSplice.setProteinEnd(78);

        Mockito.when(this.proteinChangeResolver.resolveHgvspShort(annotation, transcriptSplice)).thenReturn("X307_splice");

        IntegerRange proteinPos = proteinPositionResolver.resolve(annotation, transcriptSplice);

        assertEquals("Protein start position should be derived from protein change for splice sites",
            proteinPos.getStart(), new Integer(307));

        assertEquals("Protein end position should be derived from protein change for splice sites",
            proteinPos.getEnd(), new Integer(307));
    }

    @Test
    public void resolveProteinChangeForStopCodonPosition()
    {
        VariantAnnotation annotation = new VariantAnnotation();
        annotation.setVariantId("7:g.116411872_116411900del");

        // VEP provides no protein_start for splice_region_variant intronic positions
        TranscriptConsequence transcriptNoProteinPos = new TranscriptConsequence();

        Mockito.when(this.proteinChangeResolver.resolveHgvspShort(annotation, transcriptNoProteinPos)).thenReturn("p.*963*");

        IntegerRange proteinPos = proteinPositionResolver.resolve(annotation, transcriptNoProteinPos);

        assertEquals("Protein start position should be extracted from p.*963* hgvspShort",
            proteinPos.getStart(), new Integer(963));

        assertEquals("Protein end position should be extracted from p.*963* hgvspShort",
            proteinPos.getEnd(), new Integer(963));

        // frameshift variant of same pattern
        Mockito.when(this.proteinChangeResolver.resolveHgvspShort(annotation, transcriptNoProteinPos)).thenReturn("p.*963fs*");

        proteinPos = proteinPositionResolver.resolve(annotation, transcriptNoProteinPos);

        assertEquals("Protein start position should be extracted from p.*963fs* hgvspShort",
            proteinPos.getStart(), new Integer(963));
    }
}
