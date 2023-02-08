package com.testfabrik.webmate.javasdk.testmgmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TestRunExecutionStatus {
    CREATED("created"),
    RUNNING("running"),
    STOPPED("stopped"),
    FAILED("failed"),
    COMPLETED("completed");

    private final String value;

    @JsonCreator
    TestRunExecutionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
