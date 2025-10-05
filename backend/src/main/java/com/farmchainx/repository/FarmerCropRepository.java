package com.farmchainx.repository;

import com.farmchainx.model.FarmerCrop;
import com.farmchainx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmerCropRepository extends JpaRepository<FarmerCrop, Long> {
    List<FarmerCrop> findByUser(User user);
    List<FarmerCrop> findByFarmerId(String farmerId);
}
