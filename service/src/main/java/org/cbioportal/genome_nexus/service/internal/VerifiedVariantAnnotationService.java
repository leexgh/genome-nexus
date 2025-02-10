/*
 * Copyright (c) 2021 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.genome_nexus.service.internal;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.genome_nexus.component.annotation.NotationConverter;
import org.cbioportal.genome_nexus.model.AnnotationField;
import org.cbioportal.genome_nexus.model.GenomicLocation;
import org.cbioportal.genome_nexus.model.VariantAnnotation;
import org.cbioportal.genome_nexus.service.exception.VariantAnnotationNotFoundException;
import org.cbioportal.genome_nexus.service.exception.VariantAnnotationWebServiceException;
import org.cbioportal.genome_nexus.util.GenomicVariantUtil;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

@Service
public class VerifiedVariantAnnotationService
{
    private static final Log LOG = LogFactory.getLog(VerifiedVariantAnnotationService.class);
    private final HgvsVariantAnnotationService hgvsVariantAnnotationService;
    private final NotationConverter notationConverter;
    
    @Autowired
    public VerifiedVariantAnnotationService(
        HgvsVariantAnnotationService hgvsVariantAnnotationService,
        NotationConverter notationConverter)
    {
        this.hgvsVariantAnnotationService = hgvsVariantAnnotationService;
        this.notationConverter = notationConverter;
    }

    public VariantAnnotation getHgvsAnnotation(String variant)
            throws VariantAnnotationNotFoundException, VariantAnnotationWebServiceException
    {
        VariantAnnotation annotation = hgvsVariantAnnotationService.getAnnotation(variant);
        VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(
            annotation,
            variant,
            GenomicVariantUtil.providedReferenceAlleleFromHgvs(variant));
        return verifiedAnnotation;
    }

    
    public List<VariantAnnotation> getHgvsAnnotations(List<String> variants)
    {
        List<VariantAnnotation> annotations = hgvsVariantAnnotationService.getAnnotations(variants);
        for (int index = 0; index < annotations.size(); index = index + 1) {
            VariantAnnotation annotation = annotations.get(index);
            VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(
                annotation,
                variants.get(index),
                GenomicVariantUtil.providedReferenceAlleleFromHgvs(variants.get(index)));
            annotations.set(index, verifiedAnnotation);
        }
        return annotations;
    }

    
    public VariantAnnotation getHgvsAnnotation(String variant, String isoformOverrideSource, Map<String, String> token, List<AnnotationField> fields)
            throws VariantAnnotationWebServiceException, VariantAnnotationNotFoundException
    {
        VariantAnnotation annotation = hgvsVariantAnnotationService.getAnnotation(variant, isoformOverrideSource, token, fields);
        VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(
            annotation,
            variant, 
            GenomicVariantUtil.providedReferenceAlleleFromHgvs(variant)
        );
        return verifiedAnnotation;
    }

    
    public List<VariantAnnotation> getHgvsAnnotations(List<String> variants, String isoformOverrideSource, Map<String, String> token, List<AnnotationField> fields)
    {
        List<VariantAnnotation> annotations = hgvsVariantAnnotationService.getAnnotations(variants, isoformOverrideSource, token, fields);
        for (int index = 0; index < annotations.size(); index = index + 1) {
            VariantAnnotation annotation = annotations.get(index);
            VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(
                annotation,
                variants.get(index),
                GenomicVariantUtil.providedReferenceAlleleFromHgvs(variants.get(index)));
            annotations.set(index, verifiedAnnotation);
        }
        return annotations;
    }

    public VariantAnnotation getGenomicLocationAnnotation(GenomicLocation genomicLocation)
        throws VariantAnnotationNotFoundException, VariantAnnotationWebServiceException
    {
        String hgvs = notationConverter.genomicToHgvs(genomicLocation);
        VariantAnnotation annotation = hgvsVariantAnnotationService.getAnnotation(hgvs);
        annotation.setOriginalVariantQuery(genomicLocation.toString());
        VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(annotation, hgvs, genomicLocation.getReferenceAllele());
        return verifiedAnnotation;
    }

    public List<VariantAnnotation> getGenomicLocationAnnotations(List<GenomicLocation> genomicLocations)
    {
        List<String> hgvs = notationConverter.genomicToHgvs(genomicLocations);
        List<VariantAnnotation> annotations = hgvsVariantAnnotationService.getAnnotations(hgvs);
        for (int index = 0; index < annotations.size(); index = index + 1) {
            VariantAnnotation annotation = annotations.get(index);
            annotation.setOriginalVariantQuery(genomicLocations.get(index).toString());
            VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(
                annotation,
                hgvs.get(index),
                genomicLocations.get(index).getReferenceAllele());
            annotations.set(index, verifiedAnnotation);
        }
        return annotations;
    }

    public VariantAnnotation getGenomicLocationAnnotation(GenomicLocation genomicLocation,
                                                String isoformOverrideSource,
                                                Map<String, String> token,
                                                List<AnnotationField> fields) throws VariantAnnotationWebServiceException, VariantAnnotationNotFoundException
    {
        String hgvs = notationConverter.genomicToHgvs(genomicLocation);
        VariantAnnotation annotation = hgvsVariantAnnotationService.getAnnotation(notationConverter.genomicToHgvs(genomicLocation), isoformOverrideSource, token, fields);
        annotation.setOriginalVariantQuery(genomicLocation.toString());
        VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(annotation, hgvs, genomicLocation.getReferenceAllele());
        return verifiedAnnotation;
    }

    public List<VariantAnnotation> getGenomicLocationAnnotations(List<GenomicLocation> genomicLocations,
                                                  String isoformOverrideSource,
                                                  Map<String, String> token,
                                                  List<AnnotationField> fields)
    {
        List<String> hgvs = notationConverter.genomicToHgvs(genomicLocations);
        List<VariantAnnotation> annotations = hgvsVariantAnnotationService.getAnnotations(hgvs, isoformOverrideSource, token, fields);;
        for (int index = 0; index < annotations.size(); index = index + 1) {
            VariantAnnotation annotation = annotations.get(index);
            annotation.setOriginalVariantQuery(genomicLocations.get(index).toString());
            VariantAnnotation verifiedAnnotation = verifyOrFailAnnotation(
                annotation,
                hgvs.get(index),
                genomicLocations.get(index).getReferenceAllele());
            annotations.set(index, verifiedAnnotation);
        }
        return annotations;
    }

    private VariantAnnotation verifyOrFailAnnotation(VariantAnnotation annotation, String hgvs, String ref)
    {
        if (ref.length() == 0) {
            // no comparison possible : allele not specified in query
            return annotation;
        }
        LOG.debug("verifying providedReferenceAllele : '" + ref + "'");
        String responseReferenceAllele = getReferenceAlleleFromAnnotation(annotation);
        if (responseReferenceAllele.length() != ref.length()) {
            // for altered length Deletion-Insertion responses, recover full reference allele with followup query
            String followUpVariant = constructFollowUpQuery(hgvs);
            if (followUpVariant.length() > 0) {
                try {
                    LOG.debug("performing followup annotation request to get VEP genome assembly sequence : '" + ref + "'");
                    VariantAnnotation followUpAnnotation = hgvsVariantAnnotationService.getAnnotation(followUpVariant);
                    responseReferenceAllele = getReferenceAlleleFromAnnotation(followUpAnnotation);
                } catch (VariantAnnotationNotFoundException|VariantAnnotationWebServiceException vae) {
                    // followup validation failed - could not verify provided allele, so accept failure
                    LOG.debug("followup annotation request failed - Reference_Allele could not be verified");
                }
            }
        }
        if (ref.equals(responseReferenceAllele)) {
            // validation complete
            return annotation;
        }
        // return annotation failure
        if (annotation.getErrorMessage() == null) {
            annotation.setErrorMessage( String.format("Reference allele extracted from response (%s) does not match given reference allele (%s)", responseReferenceAllele.length() == 0 ? "-" : responseReferenceAllele, ref.length() == 0 ? "-" : ref));
        }
        return createFailedAnnotation(annotation.getOriginalVariantQuery(), annotation.getVariant(), annotation.getErrorMessage());
    }

    private String constructFollowUpQuery(String originalQuery)
    {
        // create a deletion variant covering the referenced genome positions
        String followUpQuery = originalQuery.replaceFirst("ins.*|del.*|[A|T|C|G]>[A|T|C|G]","");
        if (followUpQuery.length() == originalQuery.length()) {
            return "";
        }
        return followUpQuery + "del";
    }

    private String getReferenceAlleleFromAnnotation(VariantAnnotation annotation)
    {
        String alleleString = annotation.getAlleleString();
        if (alleleString == null) {
            // maybe original annotation attempt failed
            return "";
        }
        int slashPosition = alleleString.indexOf('/');
        if (slashPosition == -1 || slashPosition == 0) {
            return "";
        }
        return alleleString.substring(0,slashPosition);

    }

    private VariantAnnotation createFailedAnnotation(String originalVariantQuery, String originalVariant, String errorMessage)
    {
        VariantAnnotation annotation = new VariantAnnotation();
        if (originalVariantQuery != null && originalVariantQuery.length() > 0) {
            annotation.setOriginalVariantQuery(originalVariantQuery);
        }
        if (originalVariant != null && originalVariant.length() > 0) {
            annotation.setVariant(originalVariant);
        }
        annotation.setSuccessfullyAnnotated(false);
        annotation.setErrorMessage(errorMessage);
        return annotation;
    }
}
