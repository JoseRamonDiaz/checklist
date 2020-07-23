package com.jrda.ws_authentication.dao.document;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChecklistRepository extends MongoRepository<Checklist, String> {
}
