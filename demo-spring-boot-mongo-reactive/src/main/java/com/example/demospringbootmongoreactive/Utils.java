package com.example.demospringbootmongoreactive;

public final class Utils {
    
    private Utils() {

    }

    public static String slugify(String source ) {

        String result = source.toLowerCase() ;

        result = result.replaceAll("\r\n", "");
        result = result.replaceAll("\n", "");
        result = result.replaceAll("\r", "");
        result = result.replaceAll("[\\s]+", "-") ;
        return result ;
    }
}
