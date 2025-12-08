package com.rdc.weflow_server.entity.step;

public enum StepRequestStatus {

    REQUESTED,
    APPROVED,
    REJECTED,
    CHANGE_REQUESTED,
    CANCELED;

    public boolean isEditable() {
        return this == REQUESTED || this == CHANGE_REQUESTED;
    }
}
