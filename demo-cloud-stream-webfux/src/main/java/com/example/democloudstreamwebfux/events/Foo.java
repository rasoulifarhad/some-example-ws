package com.example.democloudstreamwebfux.events;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
// @NoArgsConstructor
public class Foo {
    
    public Foo(String foo) {
        id = UUID.randomUUID();
        this.foo = foo;
    }

    public Foo() {
        id = UUID.randomUUID();
    }

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id ;

    @Column(nullable = false)
    private String foo;

    @Column(nullable = false, updatable = false )
    private OffsetDateTime dateCreated ;

    @Column(nullable = false)
    private OffsetDateTime lastUpdated ;

    @PrePersist
    public void prePersist() {
        this.dateCreated = OffsetDateTime.now() ;
        this.lastUpdated = this.dateCreated ;

    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = OffsetDateTime.now();
    }

    
}
