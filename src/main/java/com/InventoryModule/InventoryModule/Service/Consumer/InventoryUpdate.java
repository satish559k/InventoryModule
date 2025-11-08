package com.InventoryModule.InventoryModule.Service.Consumer;

import com.InventoryModule.InventoryModule.Entity.Product;
import com.InventoryModule.InventoryModule.Repository.ProductRepo;
import com.InventoryModule.InventoryModule.dto.InventoryKafkaMessage;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@AllArgsConstructor
public class InventoryUpdate {

    private final BlockingQueue<InventoryKafkaMessage> orderQueue = new LinkedBlockingQueue<>();
    private final ProductRepo productRepo;

    @KafkaListener(topics = {"${inventory.topicname}"}, groupId = "${inventory.group}")
    public void InventoryConsumer(HashMap<String,Object> message, Acknowledgment ack) {

        try{
            System.out.println("[Consumer Thread]Consumer received: "+ message);

            // Extract productId,user and quantityOrder
            UUID productId = UUID.fromString(message.get("productId").toString());
            UUID userId = UUID.fromString(message.get("userId").toString());
            int quantityOrder = Integer.parseInt(message.get("quantityOrder").toString());
            System.out.println("[Consumer Thread]Consumer received: productId:- "+ productId);

            InventoryKafkaMessage order = new InventoryKafkaMessage(productId, userId,quantityOrder);
            orderQueue.put(order);
            //commit message from kafka
            ack.acknowledge();
            log.info("[Consumer Thread]Consumer commited message");
        }
        catch(Exception e){
            System.out.println("Consumer error: " + e);
        }
    }


    @Async("workerExecutor")
    public void WorkerThread() throws InterruptedException {
        while (true) {
            try {
                if (!orderQueue.isEmpty()) {

                    // Process one product at a time
                    for (InventoryKafkaMessage order : orderQueue) {
                        Integer quantityOrder = order.getQuantity();
                        if (quantityOrder == null) continue;

                        System.out.println("[Worker Thread] Processing productId: " + order.getProductId());

                        Product data = productRepo.findByproductId(order.getProductId());
                        if (data != null) {
                            data.setQuantity(data.getQuantity() - quantityOrder);
                            productRepo.save(data);
                            log.info("Changes updated in DB for productId: " + order.getProductId());
                            orderQueue.remove(order);
                        }
                    }
                } else {
                    System.out.println("[Worker Thread] No work");
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                System.out.println("[Worker Thread] Error: " + e.getMessage());
                Thread.sleep(1000);
            }
        }
    }


    // Start worker thread after app is ready
    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void init() throws InterruptedException {
        WorkerThread(); // runs in separate thread because of @Async
    }

}
