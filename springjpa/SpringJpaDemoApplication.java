package com.demo.springjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Data JPA Demo application.
 *
 * Topics covered:
 *  1. Basic CRUD via JpaRepository
 *  2. Derived / keyword-based query methods
 *  3. Named queries (@NamedQuery / @NamedQueries)
 *  4. Dynamic queries with JPA Criteria API (Specification)
 *  5. @Query (JPQL & Native SQL)
 */
@SpringBootApplication
public class SpringJpaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJpaDemoApplication.class, args);
    }
}
