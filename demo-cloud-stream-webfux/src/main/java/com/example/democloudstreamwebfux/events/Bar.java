package com.example.democloudstreamwebfux.events;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
// @NoArgsConstructor
public class Bar {
    
    public Bar(String bar) {
        id = UUID.randomUUID();
        this.bar = bar;
    }

    public Bar() {
        id = UUID.randomUUID();
    }
 
    @Id   
    @Column(nullable = false, updatable = false)
    private UUID id ;

    @Column(nullable = false)
    private String bar;
 
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
