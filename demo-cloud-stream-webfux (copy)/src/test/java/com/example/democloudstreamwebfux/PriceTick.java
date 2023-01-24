package com.example.democloudstreamwebfux;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
// @AllArgsConstructor
public class PriceTick {

    private final int sequence ;
    private final Date date ;
    private final String instrument;
    private final double price ;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss");


    public PriceTick(int sequence, Date date, String instrument, double price) {
        this.sequence = sequence;
        this.date = date;
        this.instrument = instrument;
        this.price = price;
    }


    public boolean isLast() {
        return sequence >= 10 ;
    }



    
    
}
