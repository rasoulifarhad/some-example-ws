package com.example.democloudstreamwebfux.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.checkerframework.checker.units.qual.degrees;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FunctionalTest  {


    public static class Converter implements BiFunction<Double,Double,Double> {

        @Override
        public Double apply(Double conversionRate, Double value) {
            return conversionRate * value;
        }
    }
    
    @Test
    public void converterTest() {
        
        Converter mileTokmgConverter = new Converter() ;

        Double tenMileToKmg = mileTokmgConverter.apply(1.609, 10.0) ;
        Double twoebtyMileToKmg = mileTokmgConverter.apply(1.609, 20.0) ;
        Double fiftyMileToKmg = mileTokmgConverter.apply(1.609, 50.0) ;


        log.info("10 mile is {} kmg" ,tenMileToKmg);
        log.info("20 mile is {} kmg" ,twoebtyMileToKmg);
        log.info("50 mile is {} kmg" ,fiftyMileToKmg);
    }


    @FunctionalInterface
    public interface ExtendedBiFunction<T,U,R> extends BiFunction<T,U,R> {

        default Function<U,R> curry1(T t){

            return u -> apply(t, u) ; 
        }

        default Function<T,R> curry2(U u) {

            return t -> apply(t, u);
        }

        default <V> ExtendedBiFunction<V,U,R> compose1(Function<? super V , ? extends T> before) {

            return (v, u) -> apply(before.apply(v),u); 

        }

        default <V> ExtendedBiFunction<T,V,R> compose2(Function<? super V , ? extends U> before) {

            return (t, v) -> apply(t, before.apply(v)); 

        }
    }

    public static class ExtendedConverter implements ExtendedBiFunction<Double,Double,Double> {

        @Override
        public Double apply(Double conversionRate, Double value) {
            return conversionRate * value ;
        }
        
    }

    @Test
    public void curry1Test() {

        ExtendedConverter extendedConverter = new ExtendedConverter();

        Function<Double,Double> mile2KmConverter = extendedConverter.curry1(1.609);

        log.info("{} mile is {} kmg" ,10,mile2KmConverter.apply(10.0));
        log.info("{} mile is {} kmg" ,20 , mile2KmConverter.apply(20.0));
        log.info("{} mile is {} kmg" ,50, mile2KmConverter.apply(50.0));
    }

    @Test
    public void curry1Ounce2gram() {

        ExtendedConverter extendedConverter = new ExtendedConverter();

        Function<Double,Double> ou2grConverter = extendedConverter.curry1(28.345);

        log.info("{} ounces is {} grams" , 10 , ou2grConverter.apply(10.0));
        log.info("{} ounces is {} grams" , 20 , ou2grConverter.apply(20.0));
        log.info("{} ounces is {} grams" , 50 , ou2grConverter.apply(50.0));
    }

    /**
     * F = C * 9/5 + 32
     */
    @Test
    public void functionCompositionCelsius2Farenheit() {

        ExtendedConverter extendedConverter = new ExtendedConverter() ;

        Function<Double,Double> celsius2FarenheitConverter = 
                                            extendedConverter
                                                        .curry1(9.5/5)
                                                        .andThen(n -> n + 32 )
                                                        ;
        log.info("--");
        log.info("{} celsius is {} farenheit" , 10.0 , celsius2FarenheitConverter.apply(10.0));
        log.info("{} celsius is {} farenheit" , 20.0 , celsius2FarenheitConverter.apply(20.0));
        log.info("{} celsius is {} farenheit" , 50.0 , celsius2FarenheitConverter.apply(50.0));
    }
    
    /**
     * C = ( F - 32 ) * 5/9
     * subtraction has to be performed  before the multiplication by the conversion rate.
     */
    @Test
    public void functionCompositionFarenheit2Celsius() {

        ExtendedConverter extendedConverter = new ExtendedConverter();

        Function<Double,Double> farenheit2CelsiusConverter = 
                                                extendedConverter
                                                            .compose2((Double n) -> n - 32  )
                                                            .curry1(5.0/9)
                                                            ;
        log.info("--");
        log.info("{} farenheit is {} celsius." , 10.0 , farenheit2CelsiusConverter.apply(10.0));
        log.info("{} farenheit is {} celsius." , 20.0 , farenheit2CelsiusConverter.apply(20.0));
        log.info("{} farenheit is {} celsius." , 50.0 , farenheit2CelsiusConverter.apply(50.0));

    }
}
