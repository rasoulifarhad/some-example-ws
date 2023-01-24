package com.example.democloudstreamwebfux.events;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FooRepository extends JpaRepository<Foo, UUID> {
    
}
