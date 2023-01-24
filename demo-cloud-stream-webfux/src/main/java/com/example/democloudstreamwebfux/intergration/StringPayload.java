package com.example.democloudstreamwebfux.intergration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@ToString
public class StringPayload {
    String payload ;
}
