package com.jrda.ws_authentication.dao.sql;

import java.io.Serializable;

public class UsrDocsId implements Serializable {
    private long userId;
    private String documentId;

    public UsrDocsId(){}

    public UsrDocsId(long userId, String documentId) {
        this.userId = userId;
        this.documentId = documentId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
