package com.backbase.ct.bbfuel.client.approval;

import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostApprovalTypeRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostBulkApprovalTypesAssignmentRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostPolicyAssignmentBulkRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostPolicyRequest;
import static com.backbase.ct.bbfuel.data.ApprovalsDataGenerator.createPostPolicyRequestWithZeroPolicyItems;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.approval.integration.spec.IntegrationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.integration.spec.IntegrationDeletePolicyAssignmentResponse;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPolicyItemDto;
import com.backbase.dbs.approval.integration.spec.IntegrationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostBulkApprovalTypeAssignmentResponse;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyAssignmentBulkRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyRequest;
import com.backbase.dbs.approval.integration.spec.IntegrationPostPolicyResponse;
import com.backbase.dbs.approval.spec.PostApprovalTypeRequest;
import com.backbase.dbs.approval.spec.PostApprovalTypeResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalIntegrationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String APPROVAL_TYPES = "/approval-types";
    private static final String APPROVAL_TYPE_ASSIGNMENTS = "/approval-type-assignments";
    private static final String APPROVAL_TYPE_ASSIGNMENTS_BULK = APPROVAL_TYPE_ASSIGNMENTS + "/bulk";
    private static final String POLICIES = "/policies";
    private static final String POLICY_ASSIGNMENTS = "/policy-assignments";
    private static final String POLICY_ASSIGNMENTS_BULK = POLICY_ASSIGNMENTS + "/bulk";

    @PostConstruct
    public void init() {
        setBaseUri(config.getDbs().getApprovals());
        setVersion(SERVICE_VERSION);
    }

    public Response createApprovalType(PostApprovalTypeRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(APPROVAL_TYPES));
    }

    public Response deleteApprovalType(String approvalTypeId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .delete(getPath(APPROVAL_TYPES + "/" + approvalTypeId));
    }

    public Response assignApprovalTypes(IntegrationPostBulkApprovalTypeAssignmentRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(APPROVAL_TYPE_ASSIGNMENTS_BULK));
    }

    public Response deleteApprovalTypeAssignment(String jobProfileId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .delete(getPath(APPROVAL_TYPE_ASSIGNMENTS + "/" + jobProfileId));
    }

    public Response createPolicy(IntegrationPostPolicyRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(POLICIES));
    }

    public Response assignPolicies(IntegrationPostPolicyAssignmentBulkRequest body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(POLICY_ASSIGNMENTS_BULK));
    }

    public Response deletePolicy(String policyId) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .delete(getPath(POLICIES + "/" + policyId));
    }

    public String createApprovalType(String name, Integer rank) {
        return createApprovalType(createPostApprovalTypeRequest(name, rank))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(PostApprovalTypeResponse.class)
            .getApprovalType()
            .getId();
    }

    public IntegrationPostBulkApprovalTypeAssignmentResponse assignApprovalTypes(
        List<IntegrationApprovalTypeAssignmentDto> approvalTypeAssignmentDtos) {
        return assignApprovalTypes(createPostBulkApprovalTypesAssignmentRequest(approvalTypeAssignmentDtos))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(IntegrationPostBulkApprovalTypeAssignmentResponse.class);
    }

    public String createPolicy(String policyName, List<IntegrationPolicyItemDto> policyItems) {
        return createPolicy(createPostPolicyRequest(policyName, policyItems))
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(IntegrationPostPolicyResponse.class)
            .getPolicy()
            .getId();
    }

    public String createZeroApprovalPolicy() {
        return createPolicy(createPostPolicyRequestWithZeroPolicyItems())
            .then()
            .statusCode(SC_CREATED)
            .extract()
            .as(IntegrationPostPolicyResponse.class)
            .getPolicy()
            .getId();
    }

    public void assignPolicies(List<IntegrationPolicyAssignmentRequest> policyAssignmentRequests) {
        assignPolicies(createPostPolicyAssignmentBulkRequest(policyAssignmentRequests))
            .then()
            .statusCode(SC_NO_CONTENT);
    }

    public IntegrationDeletePolicyAssignmentResponse deletePolicyAssignment(String externalServiceAgreementId,
        String resource,
        String function) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .queryParam("externalServiceAgreementId", externalServiceAgreementId)
            .queryParam("resource", resource)
            .queryParam("function", function)
            .delete(getPath(POLICY_ASSIGNMENTS))
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(IntegrationDeletePolicyAssignmentResponse.class);
    }

}
