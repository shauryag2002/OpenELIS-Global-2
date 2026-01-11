package org.openelisglobal.resultvalidation.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.common.services.IStatusService;
import org.openelisglobal.resultvalidation.bean.AnalysisItem;
import org.openelisglobal.resultvalidation.util.ResultsValidationRetroCIUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

public class ResultValidationRetroCRestControllerTest extends BaseWebContextSensitiveTest {

    @Autowired
    private ResultValidationRetroCRestController controller;

    private ResultsValidationRetroCIUtility resultsValidationUtility;
    private IStatusService statusService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        resultsValidationUtility = mock(ResultsValidationRetroCIUtility.class);
        statusService = mock(IStatusService.class);

        ReflectionTestUtils.setField(controller, "resultsValidationUtility", resultsValidationUtility);
        ReflectionTestUtils.setField(controller, "statusService", statusService);
    }

    @Test
    public void testGetRetroCIValidationData_Success() throws Exception {
        List<AnalysisItem> mockList = new ArrayList<>();
        AnalysisItem item = new AnalysisItem();
        item.setAccessionNumber("12345");
        mockList.add(item);

        when(statusService.getStatusID(any(org.openelisglobal.common.services.StatusService.AnalysisStatus.class)))
                .thenReturn("1");

        when(resultsValidationUtility.getResultValidationList(eq("Immunology"), any(), anyList())).thenReturn(mockList);

        mockMvc.perform(get("/rest/validation/retroci").param("type", "immunology")).andExpect(status().isOk())
                .andExpect(jsonPath("$.resultList[0].accessionNumber").value("12345"));
    }

    @Test
    public void testGetRetroCIValidationData_WithNullType() throws Exception {
        mockMvc.perform(get("/rest/validation/retroci")).andExpect(status().isOk())
                .andExpect(jsonPath("$.resultList").isEmpty()).andExpect(jsonPath("$.testSection").doesNotExist());
    }

    @Test
    public void testGetRetroCIValidationData_WithEmptyType() throws Exception {
        mockMvc.perform(get("/rest/validation/retroci").param("type", "")).andExpect(status().isOk())
                .andExpect(jsonPath("$.resultList").isEmpty()).andExpect(jsonPath("$.testSection").value(""));
    }

    @Test
    public void testGetRetroCIValidationData_WithInvalidStatusId() throws Exception {
        when(statusService.getStatusID(any(org.openelisglobal.common.services.StatusService.AnalysisStatus.class))).thenReturn("invalid-id");

        // Should not throw exception, just empty list or partial list
        mockMvc.perform(get("/rest/validation/retroci").param("type", "immunology")).andExpect(status().isOk());
    }
}
