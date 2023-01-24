package com.example.reactivecctsaccountmanagementservice;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Document
@ToString
@NoArgsConstructor
public class Transaction {

    
    @Id
    @JsonProperty("transaction_id")
    private String transactionId ;
    private String date;

    @JsonProperty("amount_deducted")
    private double amountDeducted ;

    @JsonProperty("store_name")
    private String storeName ;

    @JsonProperty("store_id")
    private String storeId ;

    @JsonProperty("card_id")
    private String cardId ;

    @JsonProperty("transaction_location")
    private String transactionLocation;

    private TransactionStatus status ;

}

