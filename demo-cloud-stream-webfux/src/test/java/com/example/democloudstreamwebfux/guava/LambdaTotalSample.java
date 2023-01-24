package com.example.democloudstreamwebfux.guava;

import java.util.List;
import java.util.function.Predicate;

public class LambdaTotalSample {
    
    public static int totalValues(List<Integer> numbers , Predicate<Integer> selector ) {

        int total = 0 ;

        for (Integer number : numbers) {
            
            if(selector.test(number)) {
                total += number;
            }
        }
        return total ;
    }

    public static boolean isOdd(int number) {

        return number % 2 != 0 ;

    }

    public static boolean isEven(int number) {

        return number % 2 == 0 ;

    }

    public static boolean isNumber(int number) {

        return true;

    }


}