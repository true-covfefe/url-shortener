package com.thufir.repository;

import com.thufir.entity.HashUrl;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HashUrlRepository extends MongoRepository<HashUrl, String> {
    HashUrl findByHash(String hash);
}
