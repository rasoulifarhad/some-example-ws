package com.example.democloudstreamwebfux.cloudstreamfunction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class StringPayload {

    private String payload;
    
}
