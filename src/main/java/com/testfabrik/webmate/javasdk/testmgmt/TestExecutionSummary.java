package com.testfabrik.webmate.javasdk.testmgmt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.testfabrik.webmate.javasdk.user.UserId;
import org.joda.time.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestExecutionSummary {

    @JsonProperty("id")
    private TestExecutionId testExecutionId;
    private TestId testId;
    private int version;
    private UserId creator;
    private TestExecutionExecutionStatus executionStatus;
    private TestExecutionEvaluationStatus evaluationStatus;
    private DateTime creationTime;

    // For jackson
    public TestExecutionSummary() { }

    public TestExecutionSummary(TestExecutionId testExecutionId, TestId testId, int version, UserId creator,
                                TestExecutionExecutionStatus executionStatus,
                                TestExecutionEvaluationStatus evaluationStatus, DateTime creationTime) {
        this.testExecutionId = testExecutionId;
        this.testId = testId;
        this.version = version;
        this.creator = creator;
        this.executionStatus = executionStatus;
        this.evaluationStatus = evaluationStatus;
        this.creationTime = creationTime;
    }

    public TestExecutionId getTestExecutionId() {
        return testExecutionId;
    }

    public void setTestExecutionId(TestExecutionId testExecutionId) {
        this.testExecutionId = testExecutionId;
    }

    public TestId getTestId() {
        return testId;
    }

    public void setTestId(TestId testId) {
        this.testId = testId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public UserId getCreator() {
        return creator;
    }

    public void setCreator(UserId creator) {
        this.creator = creator;
    }

    public TestExecutionExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(TestExecutionExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public TestExecutionEvaluationStatus getEvaluationStatus() {
        return evaluationStatus;
    }

    public void setEvaluationStatus(TestExecutionEvaluationStatus evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }
}
