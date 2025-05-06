package com.ecommerce.OrderService.services;

import com.ecommerce.OrderService.Clients.PaymentServiceFeignClient;
import com.ecommerce.OrderService.Clients.ProductServiceFeignClient;
import com.ecommerce.OrderService.Clients.UserServiceFeignClient;
import com.ecommerce.OrderService.Dto.PaymentMethodDTO;
import com.ecommerce.OrderService.Dto.PaymentRequestDTO;
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
    private static final String ROLE_MERCHANT = "MERCHANT";
    private static final String ROLE_ADMIN = "ADMIN";

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Cart> cartRedisTemplate;
    private final RedisTemplate<String, UserSessionDTO> sessionRedisTemplate;
    private final OrderStatusSubject orderStatusSubject;
    private final EmailNotificationObserver emailNotificationObserver;
    private final CartService cartService;
    private final RefundRepository refundRepository;
    private final ProductServiceFeignClient productServiceFeignClient;
    private final PaymentServiceFeignClient paymentServiceFeignClient;

    @Autowired
    public OrderService(
            @Qualifier("cartRedisTemplate") RedisTemplate<String, Cart> cartRedisTemplate,
            @Qualifier("userSessionDTORedisTemplate") RedisTemplate<String, UserSessionDTO> sessionRedisTemplate,
            OrderRepository orderRepository, OrderStatusSubject orderStatusSubject,
            EmailNotificationObserver emailNotificationObserver, CartService cartService,
            RefundRepository refundRepository, PaymentServiceFeignClient paymentServiceFeignClient, ProductServiceFeignClient productServiceFeignClient) {
        this.cartRedisTemplate = cartRedisTemplate;
        this.sessionRedisTemplate = sessionRedisTemplate;
        this.orderRepository = orderRepository;
        this.orderStatusSubject = orderStatusSubject;
        this.emailNotificationObserver = emailNotificationObserver;
        this.cartService = cartService;
        this.refundRepository = refundRepository;
        this.paymentServiceFeignClient = paymentServiceFeignClient;
        this.productServiceFeignClient = productServiceFeignClient;
    }

    private UserSessionDTO getSession(String token) {
        UserSessionDTO session = sessionRedisTemplate.opsForValue().get(token);
        if (session == null) throw new RuntimeException("Session not found for token: " + token);
        return session;
    }

    private Cart getCart(String token) {
        Cart cart = cartRedisTemplate.opsForValue().get(token);
        if (cart == null) throw new RuntimeException("Cart not found for token: " + token);
        return cart;
    }

    @Transactional
    public void checkoutOrder(String token, PaymentMethodDTO paymentMethod, PaymentRequestDTO paymentRequest) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!"CUSTOMER".equals(userSessionDTO.getRole())) {
            throw new RuntimeException("Unauthorized role");
        }

        Cart cart = getCart(token);

        try {
            for (CartItem item : cart.getItems().values()) {
                productServiceFeignClient.removeStock(
                        item.getProductId(),
                        item.getQuantity(),
                        "Bearer " + token // Include Bearer prefix
                );
            }

            // If all stock updates succeed, proceed to payment
            paymentServiceFeignClient.createPayment(
                    cart.getUserId(),
                    userSessionDTO.getEmail(),
                    paymentMethod,
                    cart.getTotalPrice(),
                    paymentRequest,
                    token
            );

        } catch (Exception ex) {
            // If anything fails during stock removal or payment, rollback will happen due to @Transactional
            throw new RuntimeException("Checkout failed: " + ex.getMessage(), ex);
        }
    }


    public void returnStock(String token) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!"CUSTOMER".equals(userSessionDTO.getRole())) {
            throw new RuntimeException("Unauthorized role");
        }

        Cart cart = getCart(token);

        for (CartItem item : cart.getItems().values()) {
            productServiceFeignClient.addStock(
                    item.getProductId(),
                    item.getQuantity(),
                    "Bearer " + token // Include Bearer prefix
            );
        }


    }

    @Transactional
    public void createOrder(String token, Long transactionId) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_CUSTOMER)) {
            throw new RuntimeException("Unauthorized role");
        }
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
            order.setTransactionId(transactionId);
            orderRepository.save(order);
        });

        cartService.clearCart(token);
    }

    private double calculateTotalPrice(List<CartItem> items) {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public Order getOrderById(String token, Long orderId) {
        UserSessionDTO userSessionDTO = getSession(token);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        switch (userSessionDTO.getRole()) {
            case ROLE_CUSTOMER:
                if (order.getUserId().equals(userSessionDTO.getUserId())) return order;
            case ROLE_MERCHANT:
                if (order.getMerchantId().equals(userSessionDTO.getUserId())) return order;
            case ROLE_ADMIN:
                return order;
            default:
                throw new RuntimeException("You don't have permission to access this order");
        }

    }

    public Order updateOrder(String token, Long orderId, Order updatedOrder) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_MERCHANT) || !userSessionDTO.getRole().equals(ROLE_ADMIN)) {
            throw new RuntimeException("You are not allowed to update this order");
        }
        Order existingOrder = getOrderById(token, orderId);
        boolean statusChanged = !existingOrder.getStatus().equals(updatedOrder.getStatus());

        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setOrderProducts(updatedOrder.getOrderProducts());
        existingOrder.setTotalPrice(updatedOrder.getTotalPrice());
        existingOrder.setTotalItemCount(updatedOrder.getTotalItemCount());

        Order savedOrder = orderRepository.save(existingOrder);
        if (statusChanged) orderStatusSubject.notifyObservers(savedOrder);

        return savedOrder;
    }

    public void deleteOrder(String token, Long orderId) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_MERCHANT) || !userSessionDTO.getRole().equals(ROLE_ADMIN)) {
            throw new RuntimeException("You are not allowed to delete this order");
        }
        Order order = getOrderById(token, orderId);
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

    public void cancelOrder(String token, Long orderId) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_MERCHANT) || !userSessionDTO.getRole().equals(ROLE_ADMIN)) {
            throw new RuntimeException("You are not allowed to update this order");
        }
        Order order = getOrderById(token, orderId);
        OrderCommandExecutor executor = new OrderCommandExecutor(Collections.singletonList(
                new CancelOrderCommand(order, orderRepository)));
        executor.executeCommands();
        updateOrderStatus(order);
    }

    public void requestRefund(String token, Long orderId) {
        UserSessionDTO session = getSession(token);
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_CUSTOMER)) {
            throw new RuntimeException("You are not allowed to refund this order");
        }

        Order order = getOrderById(token, orderId);

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
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_MERCHANT)) {
            throw new RuntimeException("You are not allowed to reject this refund Request");
        }

        Order order = getOrderById(token, orderId);

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
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_MERCHANT)) {
            throw new RuntimeException("You are not allowed to accept this refund Request");
        }
        Order order = getOrderById(token, orderId);

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

        paymentServiceFeignClient.refundPayment(order.getTransactionId());
        // Update status
        request.setStatus(RefundRequestStatus.ACCEPTED);
        refundRepository.save(request);

        // Update order status if needed
        updateOrderStatus(order);
    }


    public void shipOrder(String token, Long orderId, Date deliveryDate) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_MERCHANT)) {
            throw new RuntimeException("You are not allowed to update this order");
        }
        Order order = getOrderById(token, orderId);
        OrderCommandExecutor executor = new OrderCommandExecutor(Collections.singletonList(
                new ShipOrderCommand(order, orderRepository)));
        executor.executeCommands();
        order.setDeliveryDate(deliveryDate);
        orderRepository.save(order);
        updateOrderStatus(order);
    }

    public void deliverOrder(String token, Long orderId) {
        UserSessionDTO userSessionDTO = getSession(token);
        if (!userSessionDTO.getRole().equals(ROLE_MERCHANT)) {
            throw new RuntimeException("You are not allowed to update this order");
        }
        Order order = getOrderById(token, orderId);
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

    public String trackOrder(String token, Long orderId) {
        Order order = getOrderById(token, orderId);
        switch (order.getStatus()) {
            case DELIVERED -> {
                return "Order Status: " + order.getStatus() + " .It was Delivered on " + order.getDeliveryDate();
            }
            case SHIPPED -> {
                return "Order Status: " + order.getStatus() + " .It will be Delivered on " + order.getDeliveryDate();
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