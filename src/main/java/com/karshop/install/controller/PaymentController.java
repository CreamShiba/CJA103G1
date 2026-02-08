package com.karshop.install.controller;

import com.karshop.install.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 綠界付款控制器 (ECPay Payment Controller)
 * 負責處理來自綠界伺服器的回傳通知 (Callback) 以及使用者的頁面導向 (ReturnURL)。
 */
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 處理綠界伺服器端通知 (NotifyURL / Server-to-Server Callback)
     * 這是綠界伺服器在後台發送的正式支付結果。
     * 備註：綠界伺服器無法直接連線至 'localhost'，測試時需搭配 ngrok 工具。
     * 
     * @param request 包含綠界 POST 過來的參數
     * @return 成功處理後必須回傳 "1|OK"，否則綠界會持續重試
     */
    @PostMapping("/notify")
    @ResponseBody
    public String handleNotify(HttpServletRequest request) {
        // 將 request 參數轉換為 Map 格式
        Map<String, String> params = convertRequestToMap(request);
        log.info("接收到綠界 Notify 通知: {}", params);

        // 呼叫 Service 處理訂單狀態更新
        paymentService.handleCallback(params);
        return "1|OK"; // 綠界規定的成功應答格式
    }

    /**
     * 處理綠界客戶端導向 (ReturnURL / OrderResultURL)
     * 當使用者在綠界完成付費後，綠界會引導使用者的瀏覽器透過 POST 方式回到此 URL。
     * 我們會再次驗證結果並顯示相對應的 UI 給使用者。
     * 
     * @param request 包含付款結果參數
     * @param session HTTP session
     * @return 導向至會員訂單列表
     */
    @PostMapping("/return")
    public String handleReturn(HttpServletRequest request) {
        // 轉換參數
        Map<String, String> params = convertRequestToMap(request);
        log.info("接收到綠界 Return 導向: {}", params);

        // 處理回傳邏輯（保險起見再做一次檢查與更新）
        paymentService.handleCallback(params);

        String rtnCode = params.get("RtnCode");
        String rtnMsg = params.get("RtnMsg");

        // 若 RtnCode 不為 1，代表付款未成功
        if (!"1".equals(rtnCode)) {
            // 付款失敗，導向訂單頁面並附帶錯誤訊息
            return "redirect:/member/orders?error=" +
                    java.net.URLEncoder.encode("付款失敗 (" + rtnCode + "): " + rtnMsg,
                            java.nio.charset.StandardCharsets.UTF_8);
        }

        // 付款完成，引導使用者回到訂單列表
        return "redirect:/member/orders";
    }

    /**
     * 開發環境專用：模擬付款成功 (Simulate Payment Success)
     * 用於 localhost 測試，模擬綠界回傳成功的場景。
     * 使用法: GET /payment/simulate-success/{orderId}
     * 
     * @param orderId 欲模擬支付成功的訂單編號
     * @return 導向至訂單列表
     */
    @GetMapping("/simulate-success/{orderId}")
    public String simulateSuccess(@PathVariable Integer orderId) {
        log.info("開發模式：模擬訂單 {} 付款成功", orderId);

        // 構建模擬綠界的參數 Map
        // 根據 PaymentService 的格式要求: "Inst" + orderNo + "T" + timestamp
        String mockTradeNo = "Inst" + orderId + "T" + System.currentTimeMillis();

        Map<String, String> mockParams = Map.of(
                "MerchantTradeNo", mockTradeNo,
                "RtnCode", "1" // 1 代表成功
        );

        // 直接調用 Service 的回傳處理邏輯
        paymentService.handleCallback(mockParams);

        return "redirect:/member/orders";
    }

    /**
     * 輔助方法：將 HttpServletRequest 的參數轉換為簡單的 Map<String, String>
     */
    private Map<String, String> convertRequestToMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0]));
    }
}
