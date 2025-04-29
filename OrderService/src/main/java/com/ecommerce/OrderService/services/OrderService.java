package com.ecommerce.OrderService.services;

import com.ecommerce.OrderService.Clients.ProductServiceFeignClient;
import com.ecommerce.OrderService.Clients.UserServiceFeignClient;
import com.ecommerce.OrderService.Dto.UserSessionDTO;
import com.ecommerce.OrderService.models.Cart;
import com.ecommerce.OrderService.models.CartItem;
import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.models.RefundRequest;
import com.ecommerce.OrderService.models.enums.OrderStatus;
import com.ecommerce.OrderService.models.enums.RefundRequestStatus;
import com.ecommerce.OrderService.repositories.OrderRepository;
import com.ecommerce.OrderService.repositories.RefundRepository;
import com.ecommerce.OrderService.services.command.*;
import com.ecommerce.OrderService.services.observer.EmailNotificationObserver;
import com.ecommerce.OrderService.services.observer.OrderStatusObserver;
import com.ecommerce.OrderService.services.observer.OrderStatusSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class OrderService {

    private static final String ROLE_CUSTOMER = "CUSTOMER";

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Cart> cartRedisTemplate;
    private final RedisTemplate<String, UserSessionDTO> sessionRedisTemplate;
    private final OrderStatusSubject orderStatusSubject;
    private final EmailNotificationObserver emailNotificationObserver;
    private final CartService cartService;
    private final RefundRepository refundRepository;
    private final UserServiceFeignClient userServiceFeignClient;

    @Autowired
    public OrderService(
            @Qualifier("cartRedisTemplate") RedisTemplate<String, Cart> cartRedisTemplate,
            @Qualifier("userSessionDTORedisTemplate") RedisTemplate<String, UserSessionDTO> sessionRedisTemplate,
            OrderRepository orderRepository, OrderStatusSubject orderStatusSubject,
            EmailNotificationObserver emailNotificationObserver, CartService cartService,
            RefundRepository refundRepository, UserServiceFeignClient userServiceFeignClient) {
        this.cartRedisTemplate = cartRedisTemplate;
        this.sessionRedisTemplate = sessionRedisTemplate;
        this.orderRepository = orderRepository;
        this.orderStatusSubject = orderStatusSubject;
        this.emailNotificationObserver = emailNotificationObserver;
        this.cartService = cartService;
        this.refundRepository = refundRepository;
        this.userServiceFeignClient = userServiceFeignClient;
    }

    private UserSessionDTO getSession(String token) {
        UserSessionDTO session = sessionRedisTemplate.opsForValue().get(token);
        if (session == null) throw new RuntimeException("Session not found for token: " + token);
        if (!ROLE_CUSTOMER.equalsIgnoreCase(session.getRole())) throw new RuntimeException("Unauthorized role");
        return session;
    }

    private Cart getCart(String token) {
        Cart cart = cartRedisTemplate.opsForValue().get(token);
        if (cart == null) throw new RuntimeException("Cart not found for token: " + token);
        return cart;
    }

    @Transactional
    public void createOrder(String token) {
        getSession(token);
        Cart cart = getCart(token);

        Map<Long, List<CartItem>> itemsByMerchant = new HashMap<>();
        cart.getItems().values().forEach(item ->
                itemsByMerchant.computeIfAbsent(item.getMerchantId(), k -> new ArrayList<>()).add(item)
        );

        itemsByMerchant.forEach((merchantId, orderProducts) -> {
            Order order = new Order();
            order.setUserId(cart.getUserId());
            order.setMerchantId(merchantId);
            order.setOrderProducts(orderProducts);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setTotalPrice(calculateTotalPrice(orderProducts));
            order.setTotalItemCount(orderProducts.size());
            order.setUserEmail(cart.getUserEmail());
            orderRepository.save(order);
        });

        cartService.clearCart(token);
    }

    private double calculateTotalPrice(List<CartItem> items) {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    public Order updateOrder(Long orderId, Order updatedOrder) {
        Order existingOrder = getOrderById(orderId);
        boolean statusChanged = !existingOrder.getStatus().equals(updatedOrder.getStatus());

        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setOrderProducts(updatedOrder.getOrderProducts());
        existingOrder.setTotalPrice(updatedOrder.getTotalPrice());
        existingOrder.setTotalItemCount(updatedOrder.getTotalItemCount());

        Order savedOrder = orderRepository.save(existingOrder);
        if (statusChanged) orderStatusSubject.notifyObservers(savedOrder);

        return savedOrder;
    }

    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);
        orderRepository.delete(order);
    }

    public List<Order> getAllOrders(String token) {
        UserSessionDTO session = getSession(token);
        switch (session.getRole().toUpperCase()) {
            case "MERCHANT":
                return orderRepository.findByMerchantId(session.getUserId());

            case "CUSTOMER":
                return orderRepository.findByUserId(session.getUserId());

            case "ADMIN":
                return orderRepository.findAll();

            default:
                throw new IllegalArgumentException("Invalid user role: " + session.getRole());
        }
    }

    public void cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderCommandExecutor executor = new OrderCommandExecutor(Collections.singletonList(
                new CancelOrderCommand(order, orderRepository)));
        executor.executeCommands();
        updateOrderStatus(order);
    }

    public void requestRefund(String token, Long orderId) {
        UserSessionDTO session = getSession(token);
        Order order = getOrderById(orderId);

        if (!order.getUserId().equals(session.getUserId()))
            throw new RuntimeException("Unauthorized refund attempt");
        if (order.getStatus() == OrderStatus.REFUNDED)
            throw new RuntimeException("Order already refunded");

        RefundRequest refundRequest = new RefundRequest(session.getUserId(), order.getMerchantId(), order, RefundRequestStatus.PENDING);
        refundRepository.save(refundRequest);

        order.setStatus(OrderStatus.REFUND_PENDING);
        orderRepository.save(order);
        log.info("Refund requested for orderId: {}", orderId);
    }
    public void rejectRefund(String token, Long orderId) {
        Order order = getOrderById(orderId);

        // Validate refund request exists
        RefundRequest refundRequest = order.getRefundRequest();
        if (refundRequest == null || refundRequest.getId() == null) {
            throw new IllegalStateException("Refund request not found for order: " + orderId);
        }

        // Fetch refund from DB
        RefundRequest request = refundRepository.findById(refundRequest.getId())
                .orElseThrow(() -> new IllegalStateException("RefundRequest not found"));

        // Update status
        request.setStatus(RefundRequestStatus.REJECTED);
        refundRepository.save(request);

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        updateOrderStatus(order);
    }

    public List<RefundRequest> getRefundRequests(String token) {
        UserSessionDTO session = getSession(token);

        switch (session.getRole().toUpperCase()) {
            case "MERCHANT":
                return refundRepository.findByMerchantId(session.getUserId());

            case "CUSTOMER":
                return refundRepository.findByUserId(session.getUserId());

            case "ADMIN":
                return refundRepository.findAll();

            default:
                throw new IllegalArgumentException("Invalid user role: " + session.getRole());
        }
    }




    public void refundOrder(String token, Long orderId) {
        Order order = getOrderById(orderId);

        // Execute the refund command
        OrderCommandExecutor executor = new OrderCommandExecutor(Collections.singletonList(
                new RefundOrderCommand(order, orderRepository)));
        executor.executeCommands();

        // Validate refund request exists
        RefundRequest refundRequest = order.getRefundRequest();
        if (refundRequest == null || refundRequest.getId() == null) {
            throw new IllegalStateException("Refund request not found for order: " + orderId);
        }

        // Fetch refund from DB
        RefundRequest request = refundRepository.findById(refundRequest.getId())
                .orElseThrow(() -> new IllegalStateException("RefundRequest not found"));

        // Update status
        request.setStatus(RefundRequestStatus.ACCEPTED);
        refundRepository.save(request);

        // Update order status if needed
        updateOrderStatus(order);
    }


    public void shipOrder(Long orderId, Date deliveryDate) {
        Order order = getOrderById(orderId);
        OrderCommandExecutor executor = new OrderCommandExecutor(Collections.singletonList(
                new ShipOrderCommand(order, orderRepository)));
        executor.executeCommands();
        order.setDeliveryDate(deliveryDate);
        orderRepository.save(order);
        updateOrderStatus(order);
    }

    public void deliverOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderCommandExecutor executor = new OrderCommandExecutor(Collections.singletonList(
                new DeliverOrderCommand(order, orderRepository, emailNotificationObserver)));
        executor.executeCommands();
        order.setDeliveryDate(Date.valueOf(LocalDate.now()));
        orderRepository.save(order);
        updateOrderStatus(order);
    }

    public void updateOrderStatus(Order order) {
        orderStatusSubject.notifyObservers(order);
    }

    public String trackOrder(Long orderId) {
        Order order = getOrderById(orderId);
        switch(order.getStatus()){
            case DELIVERED -> {
                return "Order Status: " + order.getStatus() + " .It was Delivered on "+ order.getDeliveryDate();
            }
            case SHIPPED -> {
                return "Order Status: " + order.getStatus() + " .It will be Delivered on "+ order.getDeliveryDate();
            }
            case CONFIRMED -> {
                return "Order Status: " + order.getStatus() + " .Delivery Date will be determined upon Shipment!";
            }
            default -> {
                return "Order Status: " + order.getStatus();
            }
        }
    }
}