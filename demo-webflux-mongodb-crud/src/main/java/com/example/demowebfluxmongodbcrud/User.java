package com.example.demowebfluxmongodbcrud;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = {"id","name","department"})
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(value = "users")
public class User {
    
    @Id
    private String id ;
    private String name ;
    private int age ;
    private double salary;
    private String department ;
}
