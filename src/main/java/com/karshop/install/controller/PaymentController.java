package com.karshop.install.controller;

import com.karshop.install.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * ECPay Server-to-Server Callback (NotifyURL)
     * This is where ECPay sends the official payment result in the background.
     * Note: ECPay server cannot reach 'localhost'. You need ngrok for this to work
     * with real ECPay.
     */
    @PostMapping("/notify")
    @ResponseBody
    public String handleNotify(HttpServletRequest request) {
        Map<String, String> params = convertRequestToMap(request);
        log.info("Received ECPay Notify: {}", params);

        paymentService.handleCallback(params);
        return "1|OK"; // ECPay expects this response
    }

    /**
     * ECPay Client Redirect (ReturnURL)
     * User is redirected here by ECPay after payment.
     * We process the result (just in case Notify didn't arrive) and show the UI.
     */
    @PostMapping("/return")
    public String handleReturn(HttpServletRequest request, HttpSession session) {
        Map<String, String> params = convertRequestToMap(request);
        log.info("Received ECPay Return: {}", params);

        paymentService.handleCallback(params);

        String rtnCode = params.get("RtnCode");
        String rtnMsg = params.get("RtnMsg");

        if (!"1".equals(rtnCode)) {
            // Payment failed
            return "redirect:/member/orders?error=" +
                    java.net.URLEncoder.encode("Payment Failed (" + rtnCode + "): " + rtnMsg,
                            java.nio.charset.StandardCharsets.UTF_8);
        }

        // Redirect user back to their order list
        return "redirect:/member/orders";
    }

    /**
     * DEV ONLY: Simulate Payment Success
     * Useful for localhost testing where ECPay cannot callback to us.
     * Usage: /payment/simulate-success/{orderId}
     */
    @GetMapping("/simulate-success/{orderId}")
    public String simulateSuccess(@PathVariable Integer orderId) {
        log.info("DEV: Simulating success for order {}", orderId);

        // Construct a mock param map mimicking ECPay
        // We only really need MerchantTradeNo to contain the ID properly
        // Pattern from PaymentService: "Inst" + orderNo + "T" + timestamp

        // Wait, PaymentService extracts ID from tradeNo.
        // We can't easily mock the exact tradeNo unless we look it up,
        // BUT PaymentService logic is:
        // String idPart = merchantTradeNo.substring(4, merchantTradeNo.indexOf("T"));
        // So we just need to construct a fake string that parses correctly.

        String mockTradeNo = "Inst" + orderId + "T" + System.currentTimeMillis();

        Map<String, String> mockParams = Map.of(
                "MerchantTradeNo", mockTradeNo,
                "RtnCode", "1" // Success
        );

        paymentService.handleCallback(mockParams);

        return "redirect:/member/orders";
    }

    private Map<String, String> convertRequestToMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0]));
    }
}
