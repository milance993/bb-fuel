package com.backbase.testing.dataloader.clients.productsummary;

import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.dto.ProductSummaryQueryParameters;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

public class ProductSummaryPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String PRODUCTSUMMARY_PRESENTATION_SERVICE = "product-summary-presentation-service";
    private static final String ENDPOINT_PRODUCTSUMMARY = "/productsummary";
    private static final String ENDPOINT_ARRANGEMENTS = ENDPOINT_PRODUCTSUMMARY + "/arrangements";
    private static final String ENDPOINT_CONTEXT_ARRANGEMENTS = ENDPOINT_PRODUCTSUMMARY + "/context/arrangements";

    public ProductSummaryPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + PRODUCTSUMMARY_PRESENTATION_SERVICE);
    }

    public Response getProductSummaryArrangements() {
        return requestSpec()
                .get(getPath(ENDPOINT_ARRANGEMENTS));
    }

    public Response getProductSummaryContextArrangements(ProductSummaryQueryParameters queryParameters) {
        return requestSpec()
                .queryParam("businessFunction", queryParameters.getBusinessFunction())
                .queryParam("resourceName", queryParameters.getResourceName())
                .queryParam("privilege", queryParameters.getPrivilege())
                .queryParam("externalTransferAllowed", queryParameters.getExternalTransferAllowed())
                .queryParam("creditAccount", queryParameters.getCreditAccount())
                .queryParam("debitAccount", queryParameters.getDebitAccount())
                .queryParam("from", queryParameters.getFrom())
                .queryParam("size", queryParameters.getSize())
                .queryParam("orderBy", queryParameters.getOrderBy())
                .queryParam("direction", queryParameters.getDirection())
                .queryParam("searchTerm", queryParameters.getSearchTerm())
                .get(getPath(ENDPOINT_CONTEXT_ARRANGEMENTS));
    }
}
