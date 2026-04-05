package com.example.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.example.order.dto.OrderDTO;
import com.example.order.service.OrderService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderDTO dto) {
    	   OrderDTO response = service.create(dto);
    	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
