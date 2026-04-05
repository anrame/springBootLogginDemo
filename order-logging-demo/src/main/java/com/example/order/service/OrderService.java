package com.example.order.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.order.dto.OrderDTO;
import com.example.order.entity.Order;
import com.example.order.mapper.OrderMapper;
import com.example.order.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repo;
    private final OrderMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderDTO create(OrderDTO dto) {
        log.info("event=ORDER_REQUEST userId={} amount={}", dto.getUserId(), dto.getAmount());

        if (dto.getAmount() <= 0) {
            log.warn("event=INVALID_AMOUNT amount={}", dto.getAmount());
            throw new IllegalArgumentException("Amount must be > 0");
        }

        Order saved = repo.save(mapper.toEntity(dto));
        log.info("event=ORDER_CREATED id={}", saved.getId());

        return mapper.toDTO(saved);
    }
}
