package org.openelisglobal.resultvalidation.controller.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openelisglobal.common.services.IStatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.resultvalidation.bean.AnalysisItem;
import org.openelisglobal.resultvalidation.form.ResultValidationForm;
import org.openelisglobal.resultvalidation.util.ResultsValidationRetroCIUtility;

@RunWith(MockitoJUnitRunner.class)
public class ResultValidationRetroCRestControllerUnitTest {

    @InjectMocks
    private ResultValidationRetroCRestController controller;

    @Mock
    private ResultsValidationRetroCIUtility resultsValidationUtility;

    @Mock
    private IStatusService statusService;

    @Mock
    private HttpServletRequest request;

    @Test
    public void testGetRetroCIValidationData_Success() {
        List<AnalysisItem> mockList = new ArrayList<>();
        AnalysisItem item = new AnalysisItem();
        item.setAccessionNumber("12345");
        mockList.add(item);

        when(statusService.getStatusID(any(AnalysisStatus.class))).thenReturn("1");
        // "serology" maps to "Serology"
        when(resultsValidationUtility.getResultValidationList(eq("Serology"), any(), anyList())).thenReturn(mockList);

        // Usage of "serology" avoids ConfigurationProperties.getInstance() static call
        // which requires Spring context
        ResultValidationForm form = controller.getRetroCIValidationData(request, "serology", null);

        assertNotNull(form);
        assertEquals(1, form.getResultList().size());
        assertEquals("12345", form.getResultList().get(0).getAccessionNumber());
        assertEquals("serology", form.getTestSection());
    }

    @Test
    public void testGetRetroCIValidationData_WithNullType() {
        ResultValidationForm form = controller.getRetroCIValidationData(request, null, null);
        assertNotNull(form);
        assertEquals(0, form.getResultList().size());
        assertEquals(null, form.getTestSection());
    }
}
