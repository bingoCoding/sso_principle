package com.bingo.pojo;

public class TokenInfo {
    private String userId;
    private String username;
    private String ssoClient; //来自登录请求的某应用系统标识
    private String globalId; //本次登录成功的全局会话sessionId

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSsoClient() {
        return ssoClient;
    }

    public void setSsoClient(String ssoClient) {
        this.ssoClient = ssoClient;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    @Override
    public String toString() {
        return "TokenInfo{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", ssoClient='" + ssoClient + '\'' +
                ", globalId='" + globalId + '\'' +
                '}';
    }
}
