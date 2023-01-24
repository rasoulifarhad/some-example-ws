package com.example.demospringbootmongoreactive;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class SlugifyTest {

    @Test
    public void testSlugify() {
        assertTrue(Utils.slugify("Hello World").equals("hello-world"));
        assertTrue(Utils.slugify("Hello \n World").equals("hello-world"));
    }
    
}
