package com.testfabrik.webmate.javasdk.packagemgmt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.blobs.BlobClient;
import com.testfabrik.webmate.javasdk.blobs.BlobId;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Facade to the webmate Device subsystem.
 */
public class PackageMgmtClient {

    private WebmateAPISession session;
    private PackageMgmtApiClient apiClient;


    private static final Logger LOG = LoggerFactory.getLogger(PackageMgmtClient.class);

    private static class PackageMgmtApiClient extends WebmateApiClient {

        private final static UriTemplate createPackageTemplate = new UriTemplate("/projects/${projectId}/packages");

        private final static UriTemplate getPackageTemplate = new UriTemplate("/package/packages/${packageId}");


        public PackageMgmtApiClient(WebmateAuthInfo authInfo, WebmateEnvironment environment) {
            super(authInfo, environment);
        }

        public PackageMgmtApiClient(WebmateAuthInfo authInfo, WebmateEnvironment environment, HttpClientBuilder httpClientBuilder) {
            super (authInfo, environment, httpClientBuilder);
        }


        public Package createPackage(ProjectId projectId, BlobId blobId, String packageName, String extension) {
            Map<String, String> packageData = ImmutableMap.of("blobId", blobId.toString(), "name", packageName, "extension", extension);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            Optional<HttpResponse> r = sendPOST(createPackageTemplate, ImmutableMap.of("projectId", projectId.toString()), mapper.valueToTree(packageData)).getOptHttpResponse();

            if (!r.isPresent()) {
                throw new WebmateApiClientException("Could not create package. Got no response");
            }

            Package appPackage;
            try {
                String packageJson = EntityUtils.toString(r.get().getEntity());
                ObjectMapper pMapper = JacksonMapper.getInstance();
                appPackage = pMapper.readValue(packageJson, Package.class);
            } catch (IOException e) {
                throw new WebmateApiClientException("Error reading Package data: " + e.getMessage(), e);
            }
            return appPackage;
        }

        public Package getPackage(PackageId packageId) {
            Optional<HttpResponse> r = sendGET(getPackageTemplate, ImmutableMap.of("packageId", packageId.toString())).getOptHttpResponse();

            if (!r.isPresent()) {
                throw new WebmateApiClientException("Could not get package. Got no response");
            }

            Package aPackage;
            try {
                String packageJson = EntityUtils.toString(r.get().getEntity());
                ObjectMapper pMapper = JacksonMapper.getInstance();
                pMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                aPackage = pMapper.readValue(packageJson, Package.class);
            } catch (IOException e) {
                throw new WebmateApiClientException("Error reading Package data: " + e.getMessage(), e);
            }
            return aPackage;
        }

    }

    /**
     * Creates a PackageMgmtClient based on a WebmateApiSession.
     * @param session The WebmateApiSession the PackageMgmtClient is supposed to be based on.
     */
    public PackageMgmtClient(WebmateAPISession session) {
        this.session = session;
        this.apiClient = new PackageMgmtApiClient(session.authInfo, session.environment);
    }

    /**
     * Creates a PackageMgmtClient based on a WebmateApiSession and a custom HttpClientBuilder.
     * @param session The WebmateApiSession the PackageMgmtClient is supposed to be based on.
     * @param httpClientBuilder The HttpClientBuilder that is used for building the underlying connection.
     */
    public PackageMgmtClient(WebmateAPISession session, HttpClientBuilder httpClientBuilder) {
        this.session = session;
        this.apiClient = new PackageMgmtApiClient(session.authInfo, session.environment, httpClientBuilder);
    }


    /**
     * Creates a new package in a Project with the given ProjectId and packageData.
     * @param projectId Project where the new device will be deployed in.
     * @param blobid Blob where the data is saved.
     * @param packageName The name of the package
     * @param extension The package file format
     */
    public Package createPackage(ProjectId projectId, BlobId blobid, String packageName, String extension) {
        return this.apiClient.createPackage(projectId, blobid, packageName, extension);
    }

    /**
     * Get the package for a given PackageId
     * @param packageId  Id of the package
     * @return The package as Json
     */
    public Package getPackage(PackageId packageId){
        return this.apiClient.getPackage(packageId);
    }

    public Package uploadApplicationPackage(ProjectId projectId, byte[] appPackage, String packageName, String extension) {
        String contentType = extension.equals("apk") ? "application/vnd.android.package-archive" : "application/x-ios-app";
        BlobClient blobClient = new BlobClient(this.session);
        BlobId blobId = blobClient.putBlob(projectId, appPackage, Optional.of(contentType));
        return this.createPackage(projectId, blobId, packageName, extension);
    }

}
