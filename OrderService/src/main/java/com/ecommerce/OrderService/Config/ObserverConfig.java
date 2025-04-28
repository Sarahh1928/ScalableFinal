package com.ecommerce.OrderService.Config;

import com.ecommerce.OrderService.services.observer.OrderStatusSubject;
import com.ecommerce.OrderService.services.observer.EmailNotificationObserver;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObserverConfig {

    @Autowired
    private OrderStatusSubject orderStatusSubject;

    @Autowired
    private EmailNotificationObserver emailNotificationObserver;

    @PostConstruct
    public void configureObservers() {
        // Register the observer with the subject
        orderStatusSubject.addObserver(emailNotificationObserver);
    }
}
