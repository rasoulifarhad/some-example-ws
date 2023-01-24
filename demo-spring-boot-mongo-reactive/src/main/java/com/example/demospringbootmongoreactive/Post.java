package com.example.demospringbootmongoreactive;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {
    
    @Id
    private String id ;
    private String title ;
    private String content;
    private String slug ;

    @CreatedDate
    private LocalDateTime createdDate ;

    @CreatedBy
    private Username author ;



}
