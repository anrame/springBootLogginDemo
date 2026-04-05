package com.example.order.mapper;

import org.mapstruct.Mapper;
import com.example.order.entity.Order;
import com.example.order.dto.OrderDTO;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDTO toDTO(Order order);
    Order toEntity(OrderDTO dto);
}
