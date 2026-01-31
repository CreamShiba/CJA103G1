package com.karshop.install.service;

import com.karshop.install.entity.InstallOrder;
import com.karshop.install.enums.OrderStatus;
import com.karshop.install.enums.PayStatus;
import com.karshop.install.repository.InstallOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final InstallOrderRepository orderRepository;

    @Value("${ecpay.merchant.id}")
    private String merchantID;

    @Value("${ecpay.hash.key}")
    private String hashKey;

    @Value("${ecpay.hash.iv}")
    private String hashIV;

    @Value("${ecpay.return.url}")
    private String returnURL;

    @Value("${ecpay.action.url}")
    private String actionURL;

    /**
     * Generate ECPay HTML Form
     */
    public String generateECPayForm(Integer orderNo) {
        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (!OrderStatus.AWAITING_PAYMENT.equals(order.getOrderStatus())) {
            throw new IllegalStateException("訂單狀態非待付款");
        }

        // Generate unique trade no
        String tradeNo = "Inst" + orderNo + "T" + System.currentTimeMillis();
        order.setEcpayTradeNo(tradeNo);
        orderRepository.save(order);

        // Prepare ECPay params
        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", merchantID);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(order.getTotalPrice()));
        params.put("TradeDesc", "Karshop Installation Service");
        params.put("ItemName", "Installation Service Order #" + orderNo);
        params.put("ReturnURL", returnURL);
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        // Calculate CheckMacValue
        String checkMacValue = generateCheckMacValue(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        // Generate HTML Form
        StringBuilder html = new StringBuilder();
        html.append("<form id='ecpay-form' action='").append(actionURL).append("' method='POST'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
                    .append(entry.getValue()).append("'>");
        }
        // Auto submit script
        html.append("<script>document.getElementById('ecpay-form').submit();</script>");
        html.append("</form>");

        return html.toString();
    }

    /**
     * Handle ECPay Callback
     */
    @Transactional
    public void handleCallback(Map<String, String> params) {
        String merchantTradeNo = params.get("MerchantTradeNo");
        String rtnCode = params.get("RtnCode");

        log.info("Receive ECPay callback: TradeNo={}, RtnCode={}", merchantTradeNo, rtnCode);

        if ("1".equals(rtnCode)) { // 1 = Success
            // Find order by TradeNo (need to scan or query, here simplified)
            // Ideally add findByEcpayTradeNo(String) to repo
            // For now, let's assume we extract ID from TradeNo pattern "Inst{id}time"
            try {
                String idPart = merchantTradeNo.substring(4, merchantTradeNo.indexOf("T"));
                Integer orderNo = Integer.parseInt(idPart);

                InstallOrder order = orderRepository.findById(orderNo).orElse(null);
                if (order != null && order.getPayStatus() != PayStatus.PAID) {
                    order.setPayStatus(PayStatus.PAID);
                    order.setOrderStatus(OrderStatus.PAID_UNINSTALLED);
                    orderRepository.save(order);
                    log.info("Order {} paid successfully", orderNo);
                }
            } catch (Exception e) {
                log.error("Error processing callback", e);
            }
        }
    }

    // --- Helper for CheckMacValue ---
    private String generateCheckMacValue(Map<String, String> params, String key, String iv) {
        try {
            String raw = params.keySet().stream()
                    .map(k -> k + "=" + params.get(k))
                    .collect(Collectors.joining("&"));

            raw = "HashKey=" + key + "&" + raw + "&HashIV=" + iv;
            String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8.name()).toLowerCase();

            // Re-replace specific chars to match ECPay .NET logic (if needed, simplified
            // here)
            // ECPay spec says to replace %2d -> -, %5f -> _, %2e -> ., %21 -> !, %2a -> *,
            // %28 -> (, %29 -> )
            encoded = encoded.replace("%2d", "-")
                    .replace("%5f", "_")
                    .replace("%2e", ".")
                    .replace("%21", "!")
                    .replace("%2a", "*")
                    .replace("%28", "(")
                    .replace("%29", ")");

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(encoded.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1)
                    hex.append('0');
                hex.append(h);
            }
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Hash Generation Failed", e);
        }
    }
}
