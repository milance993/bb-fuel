package com.backbase.testing.dataloader.clients.accessgroup;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.data.DataAccessGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.FunctionAccessGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

public class AccessGroupPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ACCESSGROUP_PRESENTATION_SERVICE = "accessgroup-presentation-service";
    private static final String ENDPOINT_ACCESSGROUPS = "/accessgroups";
    private static final String ENDPOINT_CONFIG_FUNCTIONS = ENDPOINT_ACCESSGROUPS + "/config/functions";
    private static final String ENDPOINT_FUNCTION_BY_LEGAL_ENTITY_ID = ENDPOINT_ACCESSGROUPS + "/function?legalEntityId=%s";
    private static final String ENDPOINT_DATA_BY_LEGAL_ENTITY_ID_AND_TYPE = ENDPOINT_ACCESSGROUPS + "/data?legalEntityId=%s&type=%s";
    private static final String ENDPOINT_PRIVILEGES_ARRANGEMENTS_BY_FUNCTIONS = ENDPOINT_ACCESSGROUPS + "/users/privileges/arrangements?userId=%s&functionName=%s&resourceName=%s&privilegeName=%s";
    private static final String ENDPOINT_USER_CONTEXT = ENDPOINT_ACCESSGROUPS + "/usercontext";
    private static final String ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS = ENDPOINT_USER_CONTEXT + "/serviceagreements";
    private static final String ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID = ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS + "/%s/legalentities";

    public AccessGroupPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + ACCESSGROUP_PRESENTATION_SERVICE);
    }

    public Response retrieveFunctionGroupsByLegalEntity(String internalLegalEntityId) {
        return requestSpec()
                .get(String.format(getPath(ENDPOINT_FUNCTION_BY_LEGAL_ENTITY_ID), internalLegalEntityId));
    }

    public String getFunctionGroupIdByLegalEntityIdAndFunctionName(String internalLegalEntityId, String functionName) {
        String functionId = getFunctionIdForFunctionName(retrieveFunctions()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionsGetResponseBody[].class), functionName);

        FunctionAccessGroupsGetResponseBody[] functionGroups = retrieveFunctionGroupsByLegalEntity(internalLegalEntityId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(FunctionAccessGroupsGetResponseBody[].class);

        FunctionAccessGroupsGetResponseBody functionGroup = Arrays.stream(functionGroups)
                .filter(fg -> fg.getPermissions()
                        .get(0)
                        .getFunctionId()
                        .equals(functionId))
                .findFirst()
                .orElse(null);

        if (functionGroup == null) {
            return null;
        } else {
            return functionGroup.getFunctionAccessGroupId();
        }
    }

    public Response retrieveDataGroupsByLegalEntityAndType(String internalLegalEntityId, String type) {
        return requestSpec()
                .get(String.format(getPath(ENDPOINT_DATA_BY_LEGAL_ENTITY_ID_AND_TYPE), internalLegalEntityId, type));
    }

    public List<String> retrieveAllDataGroupIdsByLegalEntity(String internalLegalEntityId) {
        DataAccessGroupsGetResponseBody[] dataGroups = retrieveDataGroupsByLegalEntityAndType(internalLegalEntityId, "arrangements")
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(DataAccessGroupsGetResponseBody[].class);

        List<String> dataGroupIds = new ArrayList<>();
        Arrays.stream(dataGroups)
                .forEach(dg -> dataGroupIds.add(dg.getDataAccessGroupId()));

        return dataGroupIds;
    }

    public Response getListOfArrangementsWithPrivilegesForUser(String internalUserId, String functionName, String resourceName, String privilege) {
        return requestSpec()
                .get(String.format(getPath(ENDPOINT_PRIVILEGES_ARRANGEMENTS_BY_FUNCTIONS), internalUserId, functionName, resourceName, privilege));
    }

    public Response postUserContext(UserContextPostRequestBody userContextPostRequestBody) {
        Response response = requestSpec()
                .contentType(ContentType.JSON)
                .body(userContextPostRequestBody)
                .post(getPath(ENDPOINT_USER_CONTEXT));

        Map<String, String> cookies = new HashMap<>(response.then()
                .extract()
                .cookies());
        setUpCookies(cookies);

        return response;
    }

    public Response getServiceAgreementsForUserContext() {
        return requestSpec()
                .contentType(ContentType.JSON)
                .get(getPath(ENDPOINT_USER_CONTEXT_SERVICE_AGREEMENTS));
    }

    public Response getLegalEntitiesForServiceAgreements(String serviceAgreementId) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .get(String.format(getPath(ENDPOINT_USER_CONTEXT_LEGAL_ENTITIES_BY_SERVICE_AGREEMENT_ID), serviceAgreementId));
    }

    public void selectContextBasedOnMasterServiceAgreement() {
        String serviceAgreementId = null;

        ServiceAgreementGetResponseBody[] serviceAgreementGetResponseBodies = getServiceAgreementsForUserContext()
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(ServiceAgreementGetResponseBody[].class);

        ServiceAgreementGetResponseBody masterServiceAgreement = Arrays.stream(serviceAgreementGetResponseBodies)
                .filter(ServiceAgreementGetResponseBody::getIsMaster)
                .findFirst()
                .orElse(null);

        if (masterServiceAgreement != null) {
            serviceAgreementId = serviceAgreementGetResponseBodies[0].getId();
        }

        String legalEntityId = getLegalEntitiesForServiceAgreements(serviceAgreementId)
                .then()
                .statusCode(SC_OK)
                .extract()
                .as(LegalEntitiesGetResponseBody[].class)[0].getId();

        postUserContext(new UserContextPostRequestBody()
                .withServiceAgreementId(serviceAgreementId)
                .withLegalEntityId(legalEntityId))
                .then()
                .statusCode(SC_NO_CONTENT);
    }

    private Response retrieveFunctions() {
        return requestSpec()
                .get(getPath(ENDPOINT_CONFIG_FUNCTIONS));
    }

    private String getFunctionIdForFunctionName(FunctionsGetResponseBody[] functions, String functionName) {
        Optional<String> functionId = Arrays.stream(functions)
                .filter(e -> e.getName()
                        .equals(functionName))
                .map(FunctionsGetResponseBody::getFunctionId)
                .findFirst();
        return functionId.orElse(null);
    }
}