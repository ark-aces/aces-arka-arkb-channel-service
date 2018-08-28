package com.arkaces.arka_arkb_channel_service.service_capacity;

import com.arkaces.aces_server.aces_service.server_info.Capacity;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ServerInfo {
    private String name;
    private String description;
    private String version;
    private String websiteUrl;
    private String instructions;
    private List<Capacity> capacities;
    private String flatFee;
    private String percentFee;
    private JsonNode inputSchema;
    private JsonNode outputSchema;
    private List<String> interfaces;
}