package com.haigeek.fileserver.repository;

import com.haigeek.fileserver.model.MyFile;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * MyFile 存储库.
 * 
 * @since 1.0.0 2017年3月28日
 * @author <a href="https://waylau.com">Way Lau</a> 
 */
public interface FileRepository extends MongoRepository<MyFile, String> {
}
