package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.service.ZohoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/zoho")
@RequiredArgsConstructor
public class ZohoController {

    private final ZohoService zohoService;

    @GetMapping("/auth")
    public void initiateZohoAuth(HttpServletResponse response) throws IOException {
        response.sendRedirect(zohoService.getZohoAuthUrl());
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleZohoCallback(@RequestParam("code") String code, HttpSession session) {
        zohoService.handleCallback(code, session);
        return ResponseEntity.ok("Zoho authentication successful. You can now fetch data.");
    }

    @GetMapping("/expenses")
    public ResponseEntity<Object> getExpenses(HttpSession session) {
        try {
            return ResponseEntity.ok(zohoService.getExpenses(session));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch expenses: " + e.getMessage());
        }
    }

    @GetMapping("/sales-orders")
    public ResponseEntity<Object> getSalesOrders(HttpSession session) {
        try {
            return ResponseEntity.ok(zohoService.getSalesOrders(session));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch sales orders: " + e.getMessage());
        }
    }
}