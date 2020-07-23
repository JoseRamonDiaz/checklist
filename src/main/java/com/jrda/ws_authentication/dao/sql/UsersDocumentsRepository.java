package com.jrda.ws_authentication.dao.sql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsersDocumentsRepository extends JpaRepository<UsersDocuments, UsrDocsId> {
    List<UsersDocuments> findByUserId(long userId);
}
