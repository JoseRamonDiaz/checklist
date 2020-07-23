package com.jrda.ws_authentication.dao.sql;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(UsrDocsId.class)
public class UsersDocuments {
    @Id
    private long userId;
    @Id
    private String documentId;

    private char[] permissions;

    public UsersDocuments(){}

    public UsersDocuments(long userId, String documentId, char[] permissions) {
        this.userId = userId;
        this.documentId = documentId;
        this.permissions = permissions;
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

    public char[] getPermissions() {
        return permissions;
    }

    public void setPermissions(char[] permissions) {
        this.permissions = permissions;
    }
}
