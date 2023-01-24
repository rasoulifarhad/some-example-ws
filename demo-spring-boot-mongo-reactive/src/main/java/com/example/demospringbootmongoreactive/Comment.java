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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    
    @Id
    private String id;
    private Slug post;
    private String content ;

    @CreatedDate
    private LocalDateTime createdDate;

    @CreatedBy
    private Username author ;
}
