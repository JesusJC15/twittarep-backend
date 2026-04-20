package com.twittarep.backend.repository;

import com.twittarep.backend.model.PostDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<PostDocument, String> {
}
