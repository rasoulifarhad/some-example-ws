package com.example.reactivecctsaccountmanagementservice;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository  extends ReactiveMongoRepository<Transaction,String>  /*MongoRepository<Transaction,String>*/ {
    
}
