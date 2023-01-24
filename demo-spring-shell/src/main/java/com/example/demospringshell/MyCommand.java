package com.example.demospringshell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class MyCommand {
    
    @ShellMethod("Add two integer together.")
    public int add(int a , int b ) {
        return a + b ;
    }

    @ShellMethod("subbbbb")
    public int doSub(int op1 , int op2) {
        return op1 - op2 ;
    }
}
