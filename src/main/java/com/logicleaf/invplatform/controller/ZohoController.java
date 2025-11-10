package com.logicleaf.invplatform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.logicleaf.invplatform.service.ZohoService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zoho")
public class ZohoController {

    @Autowired
    private ZohoService zohoService;

    @GetMapping("/auth")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<?> getZohoAuthUrl(@AuthenticationPrincipal UserDetails userDetails) {
        String authUrl = zohoService.getZohoAuthUrl(userDetails.getUsername());
        return ResponseEntity.ok(java.util.Collections.singletonMap("authUrl", authUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleZohoCallback(@RequestParam("code") String code,
            @RequestParam("state") String state) {
        try {
            zohoService.handleZohoCallback(code, state);
            return ResponseEntity.ok("Zoho account connected successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error connecting Zoho account: " + e.getMessage());
        }
    }

    @GetMapping("/salesorders")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<?> getSalesOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date) {
        try {
            JsonNode salesOrders = zohoService.fetchSalesOrdersForFounder(
                    userDetails.getUsername(),
                    start_date,
                    end_date);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Sales orders fetched successfully.",
                    "data", salesOrders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch sales orders: " + e.getMessage()));
        }
    }

    @GetMapping("/expenses")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<?> getExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date) {

        try {
            JsonNode expenses = zohoService.fetchExpensesForFounder(
                    userDetails.getUsername(),
                    start_date,
                    end_date);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Expenses fetched successfully.",
                    "data", expenses));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch expenses: " + e.getMessage()));
        }
    }



}