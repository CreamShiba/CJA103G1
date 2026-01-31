package com.karshop.install.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TechnicianServicesDTO {
    private List<Integer> serviceIds = new ArrayList<>();
    private Map<Integer, Integer> servicePrices = new HashMap<>();
}
