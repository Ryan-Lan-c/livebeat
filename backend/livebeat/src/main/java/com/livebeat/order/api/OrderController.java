package com.livebeat.order.api;

import com.livebeat.order.api.dto.CreateOrderRequest;
import com.livebeat.order.application.dto.OrderResponse;
import com.livebeat.order.application.service.OrderService;
import com.livebeat.shared.ApiVersion;
import com.livebeat.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * [order] 訂單 REST API 控制器
 *
 * 負責：使用者下單、查詢自身訂單、sandbox 付款
 * 對應路由：POST /api/v1/orders, GET /api/v1/orders/{id}, POST /api/v1/orders/{id}/pay
 * 權限：USER（登入購票者）
 * 依賴：OrderService
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 建立訂單（同步落地）：成功回 201；售罄回 409；庫存未就緒回 503 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.createOrder(request, principal.userId());
    }

    /** 查詢自身訂單；非本人訂單一律回 404（fail-closed） */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public OrderResponse getOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.getOrder(id, principal.userId());
    }

    /** Sandbox 付款：PENDING 訂單轉 PAID 並出票；非可付款狀態回 409 */
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('USER')")
    public OrderResponse payOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return orderService.payOrder(id, principal.userId());
    }
}
