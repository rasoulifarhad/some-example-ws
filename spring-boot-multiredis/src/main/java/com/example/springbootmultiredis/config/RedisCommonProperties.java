package com.example.springbootmultiredis.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RedisCommonProperties {
    private String host ;
    private int port;
    private int database ;

}
