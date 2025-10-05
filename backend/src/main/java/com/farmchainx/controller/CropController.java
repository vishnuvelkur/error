package com.farmchainx.controller;

import com.farmchainx.model.*;
import com.farmchainx.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/crops")
public class CropController {

    @Autowired
    private FarmerCropRepository farmerCropRepository;

    @Autowired
    private DistributorCropRepository distributorCropRepository;

    @Autowired
    private RetailerCropRepository retailerCropRepository;

    @GetMapping
    public ResponseEntity<?> getUserCrops() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            List<Map<String, Object>> crops = new ArrayList<>();

            if (user.getRole() == UserRole.FARMER) {
                List<FarmerCrop> farmerCrops = farmerCropRepository.findByUser(user);
                for (FarmerCrop crop : farmerCrops) {
                    crops.add(convertFarmerCropToMap(crop));
                }
            } else if (user.getRole() == UserRole.DISTRIBUTOR) {
                List<DistributorCrop> distributorCrops = distributorCropRepository.findByUser(user);
                for (DistributorCrop crop : distributorCrops) {
                    crops.add(convertDistributorCropToMap(crop));
                }
            } else if (user.getRole() == UserRole.RETAILER) {
                List<RetailerCrop> retailerCrops = retailerCropRepository.findByUser(user);
                for (RetailerCrop crop : retailerCrops) {
                    crops.add(convertRetailerCropToMap(crop));
                }
            }

            return ResponseEntity.ok(crops);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching crops: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createCrop(@Valid @RequestBody Map<String, Object> cropData) {
        try {
            System.out.println("Creating crop with data: " + cropData);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            System.out.println("User: " + user.getEmail() + ", Role: " + user.getRole());

            if (user.getRole() == UserRole.FARMER) {
                FarmerCrop crop = new FarmerCrop();
                crop.setUser(user);
                crop.setName((String) cropData.get("name"));
                crop.setCropType((String) cropData.get("cropType"));
                crop.setHarvestDate(java.time.LocalDate.parse((String) cropData.get("harvestDate")));
                crop.setExpiryDate(java.time.LocalDate.parse((String) cropData.get("expiryDate")));
                crop.setSoilType((String) cropData.get("soilType"));
                crop.setPesticidesUsed((String) cropData.get("pesticidesUsed"));
                crop.setImageUrl((String) cropData.get("imageUrl"));
                crop.setFarmerId(user.getFarmerId());
                crop.setFarmerName(user.getName());
                crop.setFarmerLocation(user.getLocation());

                FarmerCrop savedCrop = farmerCropRepository.save(crop);
                System.out.println("Farmer crop saved with ID: " + savedCrop.getId());
                return ResponseEntity.ok(convertFarmerCropToMap(savedCrop));

            } else if (user.getRole() == UserRole.DISTRIBUTOR) {
                DistributorCrop crop = new DistributorCrop();
                crop.setUser(user);
                crop.setName((String) cropData.get("name"));
                crop.setCropType((String) cropData.get("cropType"));
                crop.setHarvestDate(java.time.LocalDate.parse((String) cropData.get("harvestDate")));
                crop.setExpiryDate(java.time.LocalDate.parse((String) cropData.get("expiryDate")));
                crop.setSoilType((String) cropData.get("soilType"));
                crop.setPesticidesUsed((String) cropData.get("pesticidesUsed"));
                crop.setImageUrl((String) cropData.get("imageUrl"));
                crop.setDistributorId(user.getDistributorId());
                crop.setDistributorName(user.getName());
                crop.setDistributorLocation(user.getLocation());

                DistributorCrop savedCrop = distributorCropRepository.save(crop);
                System.out.println("Distributor crop saved with ID: " + savedCrop.getId());
                return ResponseEntity.ok(convertDistributorCropToMap(savedCrop));

            } else if (user.getRole() == UserRole.RETAILER) {
                RetailerCrop crop = new RetailerCrop();
                crop.setUser(user);
                crop.setName((String) cropData.get("name"));
                crop.setCropType((String) cropData.get("cropType"));
                crop.setHarvestDate(java.time.LocalDate.parse((String) cropData.get("harvestDate")));
                crop.setExpiryDate(java.time.LocalDate.parse((String) cropData.get("expiryDate")));
                crop.setSoilType((String) cropData.get("soilType"));
                crop.setPesticidesUsed((String) cropData.get("pesticidesUsed"));
                crop.setImageUrl((String) cropData.get("imageUrl"));
                crop.setRetailerName(user.getName());
                crop.setRetailerLocationAddress(user.getLocation());

                RetailerCrop savedCrop = retailerCropRepository.save(crop);
                System.out.println("Retailer crop saved with ID: " + savedCrop.getId());
                return ResponseEntity.ok(convertRetailerCropToMap(savedCrop));
            }

            return ResponseEntity.badRequest().body("Invalid user role for creating crops");
        } catch (Exception e) {
            System.err.println("Error creating crop: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating crop: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCrop(@PathVariable Long id, @Valid @RequestBody Map<String, Object> cropData) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (user.getRole() == UserRole.FARMER) {
                FarmerCrop crop = farmerCropRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Crop not found"));

                crop.setName((String) cropData.get("name"));
                crop.setCropType((String) cropData.get("cropType"));
                crop.setHarvestDate(java.time.LocalDate.parse((String) cropData.get("harvestDate")));
                crop.setExpiryDate(java.time.LocalDate.parse((String) cropData.get("expiryDate")));
                crop.setSoilType((String) cropData.get("soilType"));
                crop.setPesticidesUsed((String) cropData.get("pesticidesUsed"));
                crop.setImageUrl((String) cropData.get("imageUrl"));

                FarmerCrop updatedCrop = farmerCropRepository.save(crop);
                return ResponseEntity.ok(convertFarmerCropToMap(updatedCrop));

            } else if (user.getRole() == UserRole.DISTRIBUTOR) {
                DistributorCrop crop = distributorCropRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Crop not found"));

                crop.setName((String) cropData.get("name"));
                crop.setCropType((String) cropData.get("cropType"));
                crop.setHarvestDate(java.time.LocalDate.parse((String) cropData.get("harvestDate")));
                crop.setExpiryDate(java.time.LocalDate.parse((String) cropData.get("expiryDate")));
                crop.setSoilType((String) cropData.get("soilType"));
                crop.setPesticidesUsed((String) cropData.get("pesticidesUsed"));
                crop.setImageUrl((String) cropData.get("imageUrl"));

                DistributorCrop updatedCrop = distributorCropRepository.save(crop);
                return ResponseEntity.ok(convertDistributorCropToMap(updatedCrop));

            } else if (user.getRole() == UserRole.RETAILER) {
                RetailerCrop crop = retailerCropRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Crop not found"));

                crop.setName((String) cropData.get("name"));
                crop.setCropType((String) cropData.get("cropType"));
                crop.setHarvestDate(java.time.LocalDate.parse((String) cropData.get("harvestDate")));
                crop.setExpiryDate(java.time.LocalDate.parse((String) cropData.get("expiryDate")));
                crop.setSoilType((String) cropData.get("soilType"));
                crop.setPesticidesUsed((String) cropData.get("pesticidesUsed"));
                crop.setImageUrl((String) cropData.get("imageUrl"));

                RetailerCrop updatedCrop = retailerCropRepository.save(crop);
                return ResponseEntity.ok(convertRetailerCropToMap(updatedCrop));
            }

            return ResponseEntity.badRequest().body("Invalid user role");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating crop: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCrop(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            if (user.getRole() == UserRole.FARMER) {
                farmerCropRepository.deleteById(id);
            } else if (user.getRole() == UserRole.DISTRIBUTOR) {
                distributorCropRepository.deleteById(id);
            } else if (user.getRole() == UserRole.RETAILER) {
                retailerCropRepository.deleteById(id);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting crop: " + e.getMessage());
        }
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<?> getCropsByFarmerId(@PathVariable String farmerId) {
        try {
            List<FarmerCrop> farmerCrops = farmerCropRepository.findByFarmerId(farmerId);
            List<Map<String, Object>> crops = new ArrayList<>();
            for (FarmerCrop crop : farmerCrops) {
                crops.add(convertFarmerCropToMap(crop));
            }
            return ResponseEntity.ok(crops);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching crops: " + e.getMessage());
        }
    }

    @GetMapping("/distributor/{distributorId}")
    public ResponseEntity<?> getCropsByDistributorId(@PathVariable String distributorId) {
        try {
            List<DistributorCrop> distributorCrops = distributorCropRepository.findByDistributorId(distributorId);
            List<Map<String, Object>> crops = new ArrayList<>();
            for (DistributorCrop crop : distributorCrops) {
                crops.add(convertDistributorCropToMap(crop));
            }
            return ResponseEntity.ok(crops);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching crops: " + e.getMessage());
        }
    }

    private Map<String, Object> convertFarmerCropToMap(FarmerCrop crop) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", crop.getId());
        map.put("name", crop.getName());
        map.put("cropType", crop.getCropType());
        map.put("harvestDate", crop.getHarvestDate());
        map.put("expiryDate", crop.getExpiryDate());
        map.put("soilType", crop.getSoilType());
        map.put("pesticidesUsed", crop.getPesticidesUsed());
        map.put("imageUrl", crop.getImageUrl());
        map.put("farmerId", crop.getFarmerId());
        map.put("farmerName", crop.getFarmerName());
        map.put("farmerLocation", crop.getFarmerLocation());
        map.put("createdAt", crop.getCreatedAt());
        return map;
    }

    private Map<String, Object> convertDistributorCropToMap(DistributorCrop crop) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", crop.getId());
        map.put("name", crop.getName());
        map.put("cropType", crop.getCropType());
        map.put("harvestDate", crop.getHarvestDate());
        map.put("expiryDate", crop.getExpiryDate());
        map.put("soilType", crop.getSoilType());
        map.put("pesticidesUsed", crop.getPesticidesUsed());
        map.put("imageUrl", crop.getImageUrl());
        map.put("farmerId", crop.getFarmerId());
        map.put("farmerName", crop.getFarmerName());
        map.put("farmerLocation", crop.getFarmerLocation());
        map.put("distributorId", crop.getDistributorId());
        map.put("distributorName", crop.getDistributorName());
        map.put("distributorLocation", crop.getDistributorLocation());
        map.put("distributorReceivedDate", crop.getDistributorReceivedDate());
        map.put("sentToRetailer", crop.getSentToRetailer());
        map.put("retailerLocation", crop.getRetailerLocation());
        map.put("createdAt", crop.getCreatedAt());
        return map;
    }

    private Map<String, Object> convertRetailerCropToMap(RetailerCrop crop) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", crop.getId());
        map.put("name", crop.getName());
        map.put("cropType", crop.getCropType());
        map.put("harvestDate", crop.getHarvestDate());
        map.put("expiryDate", crop.getExpiryDate());
        map.put("soilType", crop.getSoilType());
        map.put("pesticidesUsed", crop.getPesticidesUsed());
        map.put("imageUrl", crop.getImageUrl());
        map.put("farmerId", crop.getFarmerId());
        map.put("farmerName", crop.getFarmerName());
        map.put("farmerLocation", crop.getFarmerLocation());
        map.put("distributorId", crop.getDistributorId());
        map.put("distributorName", crop.getDistributorName());
        map.put("distributorLocation", crop.getDistributorLocation());
        map.put("distributorReceivedDate", crop.getDistributorReceivedDate());
        map.put("retailerName", crop.getRetailerName());
        map.put("retailerLocationAddress", crop.getRetailerLocationAddress());
        map.put("retailerReceivedDate", crop.getRetailerReceivedDate());
        map.put("receivedFromDistributor", crop.getReceivedFromDistributor());
        map.put("createdAt", crop.getCreatedAt());
        return map;
    }
}
