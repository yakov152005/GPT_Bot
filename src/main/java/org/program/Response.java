package org.program;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    private boolean success;
    private Integer errorCode;
    private String extra;
    private List<TaskModel> tasks;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public List<TaskModel> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
    }
}
