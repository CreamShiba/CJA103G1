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

/**
 * 綠界支付服務 (ECPay Payment Service)
 * 負責產生綠界付款表單、驗證回傳參數、以及更新訂單付款狀態。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final InstallOrderRepository orderRepository;

    // 從 application.properties 讀取綠界配置
    @Value("${ecpay.merchant.id}")
    private String merchantID;

    @Value("${ecpay.hash.key}")
    private String hashKey;

    @Value("${ecpay.hash.iv}")
    private String hashIV;

    @Value("${ecpay.return.url}")
    private String returnURL;

    @Value("${ecpay.notify.url}")
    private String notifyURL;

    @Value("${ecpay.action.url}")
    private String actionURL;

    /**
     * 產生綠界自動提交的 HTML 表單 (Generate ECPay HTML Form)
     * 
     * @param orderNo 訂單編號
     * @return 包含自動提交腳本的 HTML 表單字串
     */
    public String generateECPayForm(Integer orderNo) {
        // 1. 查找訂單並驗證狀態
        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (!OrderStatus.AWAITING_PAYMENT.equals(order.getOrderStatus())) {
            throw new IllegalStateException("訂單狀態非待付款");
        }

        // 2. 產生唯一的交易編號 (MerchantTradeNo)
        // 綠界規定同一特店交易編號不可重複，且長度限制於 20 字元內 (此處使用 Inst + ID + T + 時間戳)
        String tradeNo = "Inst" + orderNo + "T" + System.currentTimeMillis();
        order.setEcpayTradeNo(tradeNo);
        orderRepository.save(order);

        // 3. 準備綠界所需參數
        // 使用 TreeMap 讓 Key 自動按字母排序，方便後續計算 CheckMacValue
        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", merchantID);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(order.getTotalPrice()));
        params.put("TradeDesc", "Karshop Installation Service");
        params.put("ItemName", "安裝服務訂單 #" + orderNo);

        // ReturnURL: 伺服器端回傳 (NotifyURL)，需公開 IP 綠界才連得到
        params.put("ReturnURL", notifyURL);

        // OrderResultURL: 客戶端回傳 (使用者完成後瀏覽器會 POST 回此 URL)
        // 在 localhost 測試時，雖然綠界伺服器連不到你，但使用者的瀏覽器在本地是可以 POST 回來的
        params.put("OrderResultURL", returnURL);
        params.put("ClientBackURL", returnURL);

        params.put("NeedExtraPaidInfo", "N");
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        // 4. 計算檢查碼 (CheckMacValue)
        String checkMacValue = generateCheckMacValue(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        // 5. 構建前端 HTML 表單
        StringBuilder html = new StringBuilder();
        html.append("<form id='ecpay-form' action='").append(actionURL).append("' method='POST'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
                    .append(entry.getValue()).append("'>");
        }
        // 加入自動提交腳本，讓使用者點擊按鈕後立即跳轉至綠界
        html.append("<script>document.getElementById('ecpay-form').submit();</script>");
        html.append("</form>");

        return html.toString();
    }

    /**
     * 處理綠界回傳結果 (Handle ECPay Callback)
     * 
     * @param params 綠界回傳的參數對應表
     */
    @Transactional
    public void handleCallback(Map<String, String> params) {
        String merchantTradeNo = params.get("MerchantTradeNo");
        String rtnCode = params.get("RtnCode");

        log.info("接收綠界回傳：TradeNo={}, RtnCode={}", merchantTradeNo, rtnCode);

        // RtnCode 為 1 代表付款成功
        if ("1".equals(rtnCode)) {
            // 解析交易編號以獲取原始訂單 ID (格式: Inst{id}T{time})
            try {
                int tIndex = merchantTradeNo.indexOf("T");
                String idPart = merchantTradeNo.substring(4, tIndex);
                Integer orderNo = Integer.parseInt(idPart);

                // 獲取訂單並更新狀態
                InstallOrder order = orderRepository.findById(orderNo).orElse(null);
                if (order == null) {
                    log.error("回傳處理失敗：找不到訂單 ID {}", orderNo);
                    throw new IllegalStateException("Order not found: " + orderNo);
                }

                // 檢查是否已更新過，避免重複處理 (Idempotency)
                if (order.getPayStatus() != PayStatus.PAID) {
                    order.setPayStatus(PayStatus.PAID);
                    // 同時更新訂單狀態為「已付款/待安裝」
                    order.setOrderStatus(OrderStatus.PAID_UNINSTALLED);
                    orderRepository.save(order);
                    log.info("訂單 {} 已成功完成付款程序", orderNo);
                }
            } catch (Exception e) {
                log.error("交易編號解析錯誤: {}", merchantTradeNo, e);
            }
        } else {
            log.warn("綠界回傳付款失敗，RtnCode={}", rtnCode);
        }
    }

    /**
     * 產生檢查碼 (Helper for CheckMacValue)
     * 遵循綠界官方演算法：排序 -> 字串拼接 -> 加上 Key/IV -> URL Encode -> SHA256 -> Hex UpperCase
     */
    private String generateCheckMacValue(Map<String, String> params, String key, String iv) {
        try {
            // 1. 將參數按字母順序拼接成 key1=value1&key2=value2...
            String raw = params.keySet().stream()
                    .map(k -> k + "=" + params.get(k))
                    .collect(Collectors.joining("&"));

            // 2. 前後加上 HashKey 與 HashIV
            raw = "HashKey=" + key + "&" + raw + "&HashIV=" + iv;

            // 3. 進行 URL Encode 並轉小寫
            String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8.name()).toLowerCase();

            // 4. 依照綠界規範，將 Encode 後的特定字元替換回原始樣貌
            encoded = encoded.replace("%2d", "-")
                    .replace("%5f", "_")
                    .replace("%2e", ".")
                    .replace("%21", "!")
                    .replace("%2a", "*")
                    .replace("%28", "(")
                    .replace("%29", ")");

            // 5. 進行 SHA-256 加密
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(encoded.getBytes(StandardCharsets.UTF_8));

            // 6. 轉成 16 進位字串並改為大寫
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1)
                    hex.append('0');
                hex.append(h);
            }
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("綠界檢查碼產生失敗", e);
        }
    }
}
