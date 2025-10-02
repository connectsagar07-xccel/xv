package com.logicleaf.invplatform.service;

import jakarta.servlet.http.HttpSession;

public interface ZohoService {
    String getZohoAuthUrl();
    void handleCallback(String code, HttpSession session);
    Object getExpenses(HttpSession session) throws Exception;
    Object getSalesOrders(HttpSession session) throws Exception;
}