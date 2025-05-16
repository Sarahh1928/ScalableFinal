package com.ecommerce.OrderService.services;

import com.ecommerce.OrderService.models.CartItem;
import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.models.RefundRequest;
import com.ecommerce.OrderService.models.enums.OrderStatus;
import com.ecommerce.OrderService.models.enums.RefundRequestStatus;
import com.ecommerce.OrderService.repositories.OrderRepository;
import com.ecommerce.OrderService.repositories.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderSeederService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRepository refundRequestRepository;

    public String seedOrders() {
        if (orderRepository.count() > 0) {
            return "⚠️ Orders already seeded.";
        }

        // Hardcoded cart items
        CartItem ci1 = new CartItem(); ci1.setProductId(1L); ci1.setQuantity(1); ci1.setPrice(150.0); ci1.setMerchantId(4L);
        CartItem ci2 = new CartItem(); ci2.setProductId(2L); ci2.setQuantity(2); ci2.setPrice(250.0); ci2.setMerchantId(4L);

        CartItem ci3 = new CartItem(); ci3.setProductId(6L); ci3.setQuantity(1); ci3.setPrice(150.0); ci3.setMerchantId(5L);
        CartItem ci4 = new CartItem(); ci4.setProductId(7L); ci4.setQuantity(2); ci4.setPrice(250.0); ci4.setMerchantId(5L);

        CartItem ci5 = new CartItem(); ci5.setProductId(11L); ci5.setQuantity(1); ci5.setPrice(150.0); ci5.setMerchantId(6L);
        CartItem ci6 = new CartItem(); ci6.setProductId(12L); ci6.setQuantity(2); ci6.setPrice(250.0); ci6.setMerchantId(6L);

        // Hardcoded orders
        Order o1 = new Order();
        o1.setUserId(1L);
        o1.setUserEmail("scalifyteam@gmail.com");
        o1.setMerchantId(4L);
        o1.setOrderProducts(List.of(ci1, ci2));
        o1.setStatus(OrderStatus.CANCELLED);
        o1.setTransactionId(2L);

        Order o2 = new Order();
        o2.setUserId(1L);
        o2.setUserEmail("scalifyteam@gmail.com");
        o2.setMerchantId(5L);
        o2.setOrderProducts(List.of(ci3, ci4));
        o2.setDeliveryDate(Date.valueOf(LocalDate.now().minusDays(9)));
        o2.setStatus(OrderStatus.REFUNDED);
        o2.setTransactionId(3L);

        Order o3 = new Order();
        o3.setUserId(2L);
        o3.setUserEmail("scalifyteam@gmail.com");
        o3.setMerchantId(6L);
        o3.setOrderProducts(List.of(ci5, ci6));
        o3.setStatus(OrderStatus.DELIVERED);
        o3.setTransactionId(5L);
        o3.setDeliveryDate(Date.valueOf(LocalDate.now().minusDays(2)));

        Order o4 = new Order();
        o4.setUserId(3L);
        o4.setUserEmail("scalifyteam@gmail.com");
        o4.setMerchantId(4L);
        o4.setOrderProducts(List.of(ci1, ci2));
        o4.setStatus(OrderStatus.SHIPPED);
        o4.setTransactionId(6L);
        o4.setDeliveryDate(Date.valueOf(LocalDate.now().plusDays(3)));

        Order o5 = new Order();
        o5.setUserId(3L);
        o5.setUserEmail("scalifyteam@gmail.com");
        o5.setMerchantId(5L);
        o5.setOrderProducts(List.of(ci3, ci4));
        o5.setStatus(OrderStatus.CONFIRMED);
        o5.setTransactionId(7L);

        List<Order> savedOrders = orderRepository.saveAll(List.of(o1, o2, o3,o4, o5));

        // Hardcoded refund requests for first 2 orders
        RefundRequest r1 = new RefundRequest();
        r1.setOrder(savedOrders.get(0));
        r1.setUserId(savedOrders.get(0).getUserId());
        r1.setMerchantId(savedOrders.get(0).getMerchantId());
        r1.setStatus(RefundRequestStatus.PENDING);

        RefundRequest r2 = new RefundRequest();
        r2.setOrder(savedOrders.get(1));
        r2.setUserId(savedOrders.get(1).getUserId());
        r2.setMerchantId(savedOrders.get(1).getMerchantId());
        r2.setStatus(RefundRequestStatus.ACCEPTED);

        refundRequestRepository.saveAll(List.of(r1, r2));

        // Link refund requests to orders and update
        savedOrders.get(0).setRefundRequest(r1);
        savedOrders.get(1).setRefundRequest(r2);
        orderRepository.saveAll(savedOrders.subList(0, 2));

        return "✅ Hardcoded orders and refunds seeded.";
    }
}
