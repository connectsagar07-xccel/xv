package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.service.ZohoService;
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
    public ResponseEntity<?> handleZohoCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        try {
            zohoService.handleZohoCallback(code, state);
            return ResponseEntity.ok("Zoho account connected successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error connecting Zoho account: " + e.getMessage());
        }
    }

    @GetMapping("/salesorders")
    public String getSalesOrders(@RequestParam String organizationId) {
        return zohoService.fetchSalesOrders(organizationId);
    }

}