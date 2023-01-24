package com.example.democloudstreamwebfux.events;

import java.util.UUID;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class BarDto {

    @NonNull
    private UUID id ;

    @NonNull
    private String bar ;
}
