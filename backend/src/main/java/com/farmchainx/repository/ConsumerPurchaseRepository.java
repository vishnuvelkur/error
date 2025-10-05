package com.farmchainx.repository;

import com.farmchainx.model.ConsumerPurchase;
import com.farmchainx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerPurchaseRepository extends JpaRepository<ConsumerPurchase, Long> {
    List<ConsumerPurchase> findByUser(User user);
}
