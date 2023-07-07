package com.testfabrik.webmate.javasdk.mailtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.testfabrik.webmate.javasdk.*;
import com.testfabrik.webmate.javasdk.artifacts.ArtifactClient;
import com.testfabrik.webmate.javasdk.testmgmt.*;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Facade to webmate's MailTest subsystem.
 */
public class MailTestClient {

    private WebmateAPISession session;
    private MailTestApiClient apiClient;
    private ArtifactClient artifactClient;

    private String mailAddress = "1231d12dasd@betatestmails.webmate.io";

    private static final Logger LOG = LoggerFactory.getLogger(MailTestClient.class);

    private static class MailTestApiClient extends WebmateApiClient {

        private final static UriTemplate createTestMailAddressInProjectTemplate =
                new UriTemplate("/mailtest/testmail/${projectId}");


        public MailTestApiClient(WebmateAuthInfo authInfo, WebmateEnvironment environment) {
            super(authInfo, environment);
        }

        public MailTestApiClient(WebmateAuthInfo authInfo, WebmateEnvironment environment, HttpClientBuilder clientBuilder) {
            super(authInfo, environment, clientBuilder);
        }

        public TestMailAddress createTestMailAddressInProject(ProjectId projectId, TestRunId testRunId) {

            JsonNode postBody = JsonNodeFactory.instance.objectNode().put("testRunId", testRunId.toString());

            Optional<HttpResponse> optHttpResponse = sendPOST(
                    createTestMailAddressInProjectTemplate,
                    ImmutableMap.of("projectId", projectId.toString()),
                    postBody).getOptHttpResponse();

            if (!optHttpResponse.isPresent()) {
                throw new WebmateApiClientException("Could not create MailTest address. Got no response");
            }

            try {
                String testMailAddressBody = EntityUtils.toString(optHttpResponse.get().getEntity());
                ObjectMapper mapper = JacksonMapper.getInstance();
                TestMailAccount testMailAccount = mapper.readValue(testMailAddressBody, TestMailAccount.class);
                return testMailAccount.getEmailAddress();
            } catch (IOException e) {
                throw new WebmateApiClientException("Error reading data: " + e.getMessage(), e);
            }
        }

    }

    /**
     * Creates a MailTestApiClient based on a WebmateApiSession.
     *
     * @param session The WebmateApiSession used by the TestMgmtClient
     * @param artifactClient client for the artifact subsystem (comes from webmate session)
     */
    public MailTestClient(WebmateAPISession session, ArtifactClient artifactClient) {
        this.session = session;
        this.apiClient = new MailTestApiClient(session.authInfo, session.environment);
        this.artifactClient = artifactClient;
    }

    /**
     * Creates a MailTestApiClient based on a WebmateApiSession
     *
     * @param session The WebmateApiSession used by the TestMgmtClient
     * @param artifactClient client for the artifact subsystem (comes from webmate session)
     * @param httpClientBuilder The HttpClientBuilder that is used for building the underlying connection.
     */
    public MailTestClient(WebmateAPISession session, ArtifactClient artifactClient,  HttpClientBuilder httpClientBuilder) {
        this.session = session;
        this.apiClient = new MailTestApiClient(session.authInfo, session.environment, httpClientBuilder);
        this.artifactClient = artifactClient;
    }

    /**
     * Creates a TestMgmtClient based on a WebmateApiSession and a custom HttpClientBuilder.
     *
     * @param session The WebmateApiSession used by the TestMgmtClient
     * @param httpClientBuilder The HttpClientBuilder that is used for building the underlying connection.
     */
    public MailTestClient(WebmateAPISession session,  HttpClientBuilder httpClientBuilder) {
        this.session = session;
        this.apiClient = new MailTestApiClient(session.authInfo, session.environment, httpClientBuilder);
    }

    /**
     * Create a TestMail that can be used in a TestRun.
     *
     * @param projectId Id of Project.
     * @param testRunId Id of TestRun.
     * @return email address associated with project and testrun.
     */
    public TestMailAddress createTestMailAddress(ProjectId projectId, TestRunId testRunId) {
        TestMailAddress result = this.apiClient.createTestMailAddressInProject(projectId, testRunId);
        this.mailAddress = result.getAddress();
        return result;
    }

    /**
     * Get emails for the test mail address in the given TestRun and project.
     *
     * @param projectId Project id.
     * @param testRunId Id of TestRun.
     * @return list of TestMails
     */
    public List<TestMail> getMailsInTestRun(ProjectId projectId, TestRunId testRunId) {

         List<ArtifactInfo> infos = this.artifactClient.queryArtifacts(projectId, testRunId, ImmutableSet.of(ArtifactType.fromString("Mail.MailContent")));
         List<TestMail> result = Lists.<TestMail>newArrayList();

         for (ArtifactInfo info : infos) {
             Optional<Artifact> artifact = artifactClient.getArtifact(info.getId());
             if (artifact.isPresent()) {
                 result.add(TestMail.fromArtifact(artifact.get()));
             } else {
                 LOG.warn("Could not retrieve artifact [" + info.getId() + "]");
             }
         }

         return result;
    }
}
