package com.jrda.checklist.dao.document;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChecklistRepository extends MongoRepository<Checklist, String> {
}
