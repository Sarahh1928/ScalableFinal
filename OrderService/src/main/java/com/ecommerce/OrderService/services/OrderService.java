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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java. sql. Date;
import java.time.LocalDate;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Cart> cartRedisTemplate;
    private final RedisTemplate<String, UserSessionDTO> sessionRedisTemplate;
    private final OrderStatusSubject orderStatusSubject;
    private final EmailNotificationObserver emailNotificationObserver;
    private final CartService cartService;
    private final RefundRepository refundRepository;
    private final UserServiceFeignClient userServiceFeignClient;

    // Constructor injection for dependencies
    @Autowired
    public OrderService(
            @Qualifier("cartRedisTemplate") RedisTemplate<String, Cart> cartRedisTemplate,
            @Qualifier("userSessionDTORedisTemplate") RedisTemplate<String, UserSessionDTO> sessionRedisTemplate,
            OrderRepository orderRepository, OrderStatusSubject orderStatusSubject, EmailNotificationObserver emailNotificationObserver, CartService cartService, RefundRepository refundRepository, UserServiceFeignClient userServiceFeignClient) {  // Injecting OrderRepository
        this.cartRedisTemplate = cartRedisTemplate;
        this.sessionRedisTemplate = sessionRedisTemplate;
        this.orderRepository = orderRepository;
        this.orderStatusSubject = orderStatusSubject;
        this.emailNotificationObserver = emailNotificationObserver;
        this.cartService = cartService;
        this.refundRepository = refundRepository;
        this.userServiceFeignClient = userServiceFeignClient;
    }

    public UserSessionDTO getSession(String token) {
        // Fetch the session for the token
        UserSessionDTO session = sessionRedisTemplate.opsForValue().get(token);
        if (session == null) {
            throw new RuntimeException("Session not found in cache for token: " + token);
        } else if (!session.getRole().equalsIgnoreCase("CUSTOMER")) {
            throw new RuntimeException("You are not a customer");
        }
        return session;
    }

    public void placeOrder(String token) {
        getSession(token);

        // Step 2: Get the cart from Redis using the token
        Cart cart = cartRedisTemplate.opsForValue().get(token);

        // Step 3: If cart is null, handle it (cart not found in Redis, maybe throw exception or return)
        if (cart == null) {
            throw new RuntimeException("Cart not found for token: " + token);
        }

        //API TO PAYMENT CART TOTAL QUANTITy
    }

    public void createOrder(String token) {
        // Step 1: Get the session for the token
        getSession(token);

        // Step 2: Get the cart from Redis using the token
        Cart cart = cartRedisTemplate.opsForValue().get(token);

        // Step 3: If cart is null, handle it (cart not found in Redis, maybe throw exception or return)
        if (cart == null) {
            throw new RuntimeException("Cart not found for token: " + token);
        }

        // Step 4: Group cart items by merchantId
        Map<Long, List<CartItem>> itemsByMerchant = new HashMap<>();

        // Iterate through the cart items and group by merchantId
        for (Map.Entry<Long, CartItem> entry : cart.getItems().entrySet()) {
            CartItem item = entry.getValue();
            Long merchantId = item.getMerchantId();

            // Add item to the appropriate merchant group
            itemsByMerchant
                    .computeIfAbsent(merchantId, k -> new ArrayList<>())
                    .add(item);
        }

        // Step 5: Create an order for each merchant
        for (Map.Entry<Long, List<CartItem>> entry : itemsByMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<CartItem> orderProducts = entry.getValue();

            // Create a new order
            Order order = new Order();
            order.setUserId(cart.getUserId());
            order.setMerchantId(merchantId);
            order.setOrderProducts(orderProducts);
            order.setStatus(OrderStatus.CONFIRMED);  // Default status
            order.setTotalPrice(calculateTotalPrice(orderProducts));  // Calculate total price
            order.setTotalItemCount(orderProducts.size());  // Calculate total item count
            order.setUserEmail(cart.getUserEmail());

            // Save order to the database (using OrderRepository)
            orderRepository.save(order);  // Save to DB
            cartService.clearCart(token);
        }
    }

    // Helper method to calculate the total price of the order
    private double calculateTotalPrice(List<CartItem> orderProducts) {
        double totalPrice = 0.0;
        for (CartItem item : orderProducts) {
            totalPrice += item.getTotalPrice();  // Add the total price of each cart item
        }
        return totalPrice;
    }
    // ---- CRUD Methods ----

    // 1. Read Order by ID
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    // 2. Update Order (e.g., change status or other fields)
    public Order updateOrder(Long orderId, Order updatedOrder) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        boolean statusChanged = !existingOrder.getStatus().equals(updatedOrder.getStatus());

        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setOrderProducts(updatedOrder.getOrderProducts());
        existingOrder.setTotalPrice(updatedOrder.getTotalPrice());
        existingOrder.setTotalItemCount(updatedOrder.getTotalItemCount());

        Order savedOrder = orderRepository.save(existingOrder);

        if (statusChanged) {
            orderStatusSubject.notifyObservers(savedOrder); // 👈 Only notify if status actually changed
        }

        return savedOrder;
    }

    public void updateOrderStatus(Order order) {
        orderStatusSubject.notifyObservers(order); // 👈 Only notify if status actually changed
    }

    // 3. Delete Order by ID
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        orderRepository.delete(order);
    }

    // 4. List all Orders (Optional, could be useful for Admin to list all orders)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        CancelOrderCommand cancelOrderCommand = new CancelOrderCommand(order, orderRepository);
        List<OrderCommand> commands = new ArrayList<>();
        commands.add(cancelOrderCommand);

        OrderCommandExecutor executor = new OrderCommandExecutor(commands);
        executor.executeCommands();  // Execute the cancel command
        updateOrderStatus(order);
    }

    public void requestRefund(String token, Long orderId) {
        // Get user session from the token
        UserSessionDTO session = getSession(token);
        Long userId = session.getUserId();

        // Retrieve the order by its ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Check if the order belongs to the user
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("You cannot request a refund for an order that is not yours.");
        }

        // Retrieve the merchant ID associated with the order
        Long merchantId = order.getMerchantId();

        // Check if the order is already refunded or in a state where a refund can't be processed (optional)
        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new RuntimeException("This order has already been refunded.");
        }

        // Create the RefundRequest
        RefundRequest refundRequest = new RefundRequest(userId, merchantId, order, RefundRequestStatus.PENDING);

        // Save the refund request to the database
        refundRepository.save(refundRequest);
        order.setStatus(OrderStatus.REFUND_PENDING);
        orderRepository.save(order);
        System.out.println("Refund request submitted successfully for order ID: " + orderId);
    }

    // Method to refund an order
    public void refundOrder(String token, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        RefundOrderCommand refundOrderCommand = new RefundOrderCommand(order, orderRepository);
        List<OrderCommand> commands = new ArrayList<>();
        commands.add(refundOrderCommand);

        OrderCommandExecutor executor = new OrderCommandExecutor(commands);
        executor.executeCommands();  // Execute the refund command

        userServiceFeignClient.deposit(token, order.getUserId(),order.getTotalPrice());
        updateOrderStatus(order);
    }

    // Method to ship an order
    public void shipOrder(Long orderId, Date deliveryDate) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        ShipOrderCommand shipOrderCommand = new ShipOrderCommand(order, orderRepository);
        List<OrderCommand> commands = new ArrayList<>();
        commands.add(shipOrderCommand);

        OrderCommandExecutor executor = new OrderCommandExecutor(commands);
        executor.executeCommands();  // Execute the ship command
        order.setDeliveryDate(deliveryDate);
        orderRepository.save(order);
        updateOrderStatus(order);
    }

    public void deliverOrder(Long orderId) {
        // Find the order by ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Create a new DeliverOrderCommand
        DeliverOrderCommand deliverOrderCommand = new DeliverOrderCommand(order, orderRepository, emailNotificationObserver);

        // List of commands to execute
        List<OrderCommand> commands = new ArrayList<>();
        commands.add(deliverOrderCommand);

        // Create an executor to execute the commands
        OrderCommandExecutor executor = new OrderCommandExecutor(commands);
        executor.executeCommands();  // Execute the deliver order command
        order.setDeliveryDate(Date.valueOf(LocalDate.now()));
        orderRepository.save(order);
        updateOrderStatus(order);

    }


}
