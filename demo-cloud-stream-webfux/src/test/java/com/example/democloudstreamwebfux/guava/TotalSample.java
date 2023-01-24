package com.example.democloudstreamwebfux.guava;

import java.util.List;

public class TotalSample {
    
    public static int totalValues(List<Integer> numbers) {
        int total = 0 ;
        for (Integer num : numbers) {
            total+= num ;
        }
        return total ;
    }

    public static int totalEvenValues(List<Integer> numbers) {

        int total = 0 ;
        for (Integer number : numbers) {
            if (number % 2 == 0 ) {
                total+= number ;
            }
            
        }
        return total ;

    }

    public static int totalOddValues(List<Integer> numbers)  {

        int total = 0 ;

        for (Integer number : numbers) {
            if( number % 2 != 0 ) {
                total += number ;
            }
        }
        return total;

    }
}
