package com.example.demospringbootmongoreactive;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slug implements Serializable {

    private String slug ;
    
}
