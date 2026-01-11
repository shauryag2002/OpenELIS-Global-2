package org.openelisglobal.resultvalidation.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.openelisglobal.common.services.IStatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.resultvalidation.bean.AnalysisItem;
import org.openelisglobal.resultvalidation.form.ResultValidationForm;
import org.openelisglobal.resultvalidation.util.ResultsValidationRetroCIUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/validation")
public class ResultValidationRetroCRestController {

    @Autowired
    private ResultsValidationRetroCIUtility resultsValidationUtility;

    @Autowired
    private IStatusService statusService;

    @GetMapping("/retroci")
    public ResultValidationForm getRetroCIValidationData(HttpServletRequest request,
            @RequestParam(value = "type", required = false) String testSectionName,
            @RequestParam(value = "test", required = false) String testName) {

        ResultValidationForm form = new ResultValidationForm();
        List<AnalysisItem> resultList = new ArrayList<>();

        if (testSectionName != null) {
            // Capitalize first letter as per legacy logic if needed, but safer to respect
            // input or DB expectation
            // BaseResultValidationRetroCIController had mappings.
            // "immunology" -> "result.validation.immunology.title"
            // "Immunology" -> "Immunology"
            String sectionName = mapTestSectionName(testSectionName);

            List<Integer> statuses = getValidationStatus(testSectionName);
            resultList = resultsValidationUtility.getResultValidationList(sectionName, testName, statuses);
        }

        form.setResultList(resultList);
        form.setTestSection(testSectionName);
        return form;
    }

    /**
     * Maps the test section name to the name expected by the database/utility.
     * Logic derived from legacy BaseResultValidationRetroCIController and
     * ResultsValidationRetroCIUtility usage.
     */
    private String mapTestSectionName(String section) {
        if (section == null || section.isEmpty())
            return section;

        // Handle capitalization (e.g. "immunology" -> "Immunology")
        String capitalized = section.substring(0, 1).toUpperCase() + section.substring(1);

        if ("MolecularBio".equalsIgnoreCase(section))
            return "Biologie Moleculaire";
        if ("Mycology".equalsIgnoreCase(section))
            return "mycology";

        return capitalized;
    }

    private List<Integer> getValidationStatus(String testSection) {
        List<Integer> validationStatus = new ArrayList<>();

        if ("serology".equalsIgnoreCase(testSection)) {
            addStatusIfValid(validationStatus, AnalysisStatus.TechnicalAcceptance);
            addStatusIfValid(validationStatus, AnalysisStatus.Canceled);
        } else {
            addStatusIfValid(validationStatus, AnalysisStatus.TechnicalAcceptance);
            if (ConfigurationProperties.getInstance()
                    .isPropertyValueEqual(ConfigurationProperties.Property.VALIDATE_REJECTED_TESTS, "true")) {
                addStatusIfValid(validationStatus, AnalysisStatus.TechnicalRejected);
            }
        }
        return validationStatus;
    }

    private void addStatusIfValid(List<Integer> list, AnalysisStatus status) {
        try {
            String id = statusService.getStatusID(status);
            if (id != null) {
                list.add(Integer.parseInt(id));
            }
        } catch (NumberFormatException e) {
            // Log or ignore invalid status IDs
        }
    }
}
