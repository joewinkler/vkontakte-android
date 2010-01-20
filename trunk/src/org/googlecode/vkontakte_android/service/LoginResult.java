package org.googlecode.vkontakte_android.service;

public class LoginResult {
    private boolean success;
    private  Exception cause;

    public LoginResult() {
    }

    public LoginResult(boolean success, Exception cause) {
        this.success = success;
        this.cause = cause;
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getCause() {
        return cause;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCause(Exception cause) {
        this.cause = cause;
    }
}
