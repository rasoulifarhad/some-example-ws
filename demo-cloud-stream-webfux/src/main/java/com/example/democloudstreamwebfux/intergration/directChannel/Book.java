package com.example.democloudstreamwebfux.intergration.directChannel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Setter
@Getter
public  class Book {

    public  enum Genre {
        fantasy,
        horror,
        romance,
        thriller
    }

    private long bookId ;
    private String title ;
    private Genre genre ;
}
