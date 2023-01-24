package com.example.democloudstreamwebfux.broadcaster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BroadcastMessage {
    private Long id ;
    private String body ;
    
}
