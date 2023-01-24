package com.example.demotestspringautoconfig.bans;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class MyBeansOrPropertyCondition extends AnyNestedCondition {

    public MyBeansOrPropertyCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(MyBean1.class)
    static class MyBean1ExistCondition {

    }
    
    @ConditionalOnBean(MyBean2.class)
    static class MyBean2ExistCondition {

    }

    @ConditionalOnProperty("multipleBeans.enabled")
    static class MultipleBeansPropertyExists {
        
    }
}
