package com.InventoryModule.InventoryModule.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InventoryKafkaMessage {
    UUID productId;
    UUID userId;
    Integer quantity;
}
