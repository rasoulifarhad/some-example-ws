package com.example.demotestspringautoconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.example.demotestspringautoconfig.bans.MyBean1;
import com.example.demotestspringautoconfig.bans.MyBean2;
import com.example.demotestspringautoconfig.bans.MyBean4;
import com.example.demotestspringautoconfig.bans.MyBean5;
import com.example.demotestspringautoconfig.bans.MyBean6;
import com.example.demotestspringautoconfig.bans.MyConfiguration;
import com.example.demotestspringautoconfig.bans.MyConfigurationOverride;

@SpringBootTest
public class AutoConfigurationTest {
    static String ME_1 = "I'm MyBean1";
    static String ME_2 = "I'm MyBean2";
    static String ME_3 = "I'm MyBean3";
    static String ME_4 = "I'm MyBean4";
    static String ME_5 = "I'm MyBean5";
    static String ME_6 = "I'm MyBean6";

    static String ME_OVERRIDE_2 = "I'm MyBean2 overridden";
    
    @Test
    public void testMyBean1() {

        final ApplicationContextRunner contextRunner = new ApplicationContextRunner();
        contextRunner
                    .withUserConfiguration(MyConfiguration.class)
                    .withClassLoader(new FilteredClassLoader(MyBean2.class))
                    .run((context) ->{
                        MyBean1 myBean1 = context.getBean(MyBean1.class);
                        assertEquals(ME_1, myBean1.me());
                    } );
    }

    @Test 
    public void testMybean2Negetive() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner();
        contextRunner
                      .withPropertyValues("myBean2.enabled")
                    //   .withPropertyValues("myBean2.enabled=false")
                    //   .withPropertyValues("myBean2.enabled=true")
                    //   .withPropertyValues("myBean2.enabled=")
                    //   .withPropertyValues("myBean2.enabled=gffg")
                    .withUserConfiguration(MyConfiguration.class)
                      .run((context) -> {
                        MyBean2 myBean2 = context.getBean(MyBean2.class);
                        assertEquals(ME_2, myBean2.me());
                      });
    }

    @Test
    public void testMyBean5() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner() ;
        contextRunner
                     .withUserConfiguration(MyConfiguration.class)
                    //  .withPropertyValues("myBean5.disabled=false")
                     .withPropertyValues("myBean5.disabled=true")
                     .run((context) -> {
                        MyBean5 myBean5 = context.getBean(MyBean5.class);
                        assertEquals(ME_5, myBean5.me());
                     });
    }

    @Test
    public void testMyBean4Negative() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner() ;
        contextRunner
                     .withUserConfiguration(MyConfiguration.class)
                     .withPropertyValues("multipleBean.enabled")
                     .run((context) -> {
                        MyBean4 myBean4 = context.getBean(MyBean4.class);
                        assertEquals(ME_4, myBean4.me());
                     });
    }

    @Test
    public void testMyBean4Positive() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner() ;
        contextRunner
                     .withUserConfiguration(MyConfiguration.class)
                     .withPropertyValues("multipleBean.enabled","myBean2.enabled")
                     .run((context) -> {
                        MyBean4 myBean4 = context.getBean(MyBean4.class);
                        assertEquals(ME_4, myBean4.me());
                     });
    }

    @Test
    public void testMyBean6() {
        ApplicationContextRunner contextRunner= new ApplicationContextRunner() ;
        contextRunner
                     .withUserConfiguration(MyConfiguration.class)
                     .run((context) -> {
                        MyBean6 myBean6 = context.getBean(MyBean6.class);
                        assertEquals(ME_6, myBean6.me());
                     });
    }

    @Test
    public void testOrder() {

        ApplicationContextRunner contextRunner= new ApplicationContextRunner() ;
        contextRunner
                     .withAllowBeanDefinitionOverriding(true)
                     .withUserConfiguration(MyConfiguration.class,MyConfigurationOverride.class)
                     .withPropertyValues("myBean2.enabled")
                     .run((context) -> {
                        MyBean2 myBean2 = context.getBean(MyBean2.class);
                        assertEquals(ME_OVERRIDE_2, myBean2.me());
                     });
    }

    @Test
    public void  testOrderMultiple() {
        ApplicationContextRunner contextRunner= new ApplicationContextRunner() ;
        contextRunner
                     .withAllowBeanDefinitionOverriding(true)
                     .withUserConfiguration(MyConfiguration.class,MyConfigurationOverride.class)
                     .withPropertyValues("myBean2.enabled", "multiple.beans")
                     .run((context) -> {
                        MyBean2 myBean2 = (MyBean2) context.getBean("myBean2");
                        assertEquals("ME_OVERRIDE_2", myBean2.me());
                     });

    }
        
}
