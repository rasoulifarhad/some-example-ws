package com.example.demotestspringautoconfig.bans;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

public class MyConfiguration {
    
    @Bean
    @ConditionalOnClass(MyBean2.class)
    public MyBean1 myBean1() {
        return new MyBean1();
    }

    @Bean
    @ConditionalOnProperty("myBean2.enabled")
    public MyBean2 myBean2() {
        return new MyBean2() ;
    }

    @Bean
    @ConditionalOnProperty(value = "myBean5.disabled" , havingValue = "false")
    public MyBean5 myBean5() {
        return new MyBean5() ;
    }

    @Bean
    @ConditionalOnProperty("multipleBean.enabled")
    @ConditionalOnBean({MyBean1.class,MyBean2.class})
    public MyBean4 myBean4() {
        return new MyBean4() ;
    }

    @Bean
    @Conditional(MyBeansOrPropertyCondition.class)
    public MyBean6 myBean6() {
        return new MyBean6();
    }

}
