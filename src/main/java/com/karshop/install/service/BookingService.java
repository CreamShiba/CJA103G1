package com.karshop.install.service;

import com.karshop.install.dto.BookingRequestDTO;
import com.karshop.install.entity.*;
import com.karshop.members.model.MembersVO;
import com.karshop.install.enums.OrderStatus;
import com.karshop.install.enums.PayStatus;
import com.karshop.install.enums.PayoutStatus;
import com.karshop.install.enums.TimeSlot;
import com.karshop.install.exception.BookingConflictException;
import com.karshop.install.exception.TechnicianBusyException;
import com.karshop.install.repository.*;
import com.karshop.members.model.MembersRepository;
import com.karshop.install.vo.AvailableSlotVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 預約與訂單核心邏輯服務 (Booking & Order Core Service)
 * 實作了三階段訂單處理流程：
 * 1. 動態時段查詢 (Dynamic Slots)
 * 2. 軟性下單 (Soft Booking / Queueing)
 * 3. 硬性鎖定 (Hard Locking / Technician Confirm)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final InstallLocationRepository locationRepository;
    private final LocationScheduleRepository scheduleRepository;
    private final InstallOrderRepository orderRepository;
    private final InstallOrderDetailRepository orderDetailRepository;
    private final MembersRepository memberRepository;
    private final TechnicianRepository technicianRepository;
    private final TechnicianServiceRepository technicianServiceRepository;

    // ===================================================================================
    // 第一階段：查詢可用時段 (Phase 1: Query Available Slots)
    // ===================================================================================
    /**
     * 獲取未來 30 天內所有可預約的時段
     * 邏輯：動態計算 -> 過濾假日 -> 檢查場地排程 -> 檢查技師忙碌狀態
     * 
     * @param techNo     技師編號
     * @param locationNo 安裝據點編號
     * @return 可預約時段列表
     */
    public List<AvailableSlotVO> getAvailableSlots(Integer techNo, Integer locationNo) {
        List<AvailableSlotVO> availableSlots = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 取得指定的場地，若不存在拋出異常
        InstallLocation location = locationRepository.findById(locationNo)
                .orElseThrow(() -> new IllegalArgumentException("場地不存在"));

        // 遞迴計算未來 30 天
        for (int i = 1; i <= 30; i++) {
            LocalDate targetDate = today.plusDays(i);
            DayOfWeek dayOfWeek = targetDate.getDayOfWeek();

            // 規則 1：過濾週六與週日 (固定休息日)
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }

            // 分別檢查該日期的 上午 (0) 與 下午 (1) 時段
            checkAndAddSlot(availableSlots, location, targetDate, TimeSlot.MORNING, techNo);
            checkAndAddSlot(availableSlots, location, targetDate, TimeSlot.AFTERNOON, techNo);
        }

        return availableSlots;
    }

    /**
     * 輔助方法：實作可用性檢查邏輯
     */
    private void checkAndAddSlot(List<AvailableSlotVO> slots, InstallLocation location,
            LocalDate date, TimeSlot timeSlot, Integer techNo) {

        // 規則 2：檢查資料庫中的據點排程表 (LocationSchedule)
        Optional<LocationSchedule> scheduleOpt = scheduleRepository
                .findByLocationLocationNoAndAppointDateAndAppointTimeValue(
                        location.getLocationNo(), date, timeSlot.getCode());

        boolean isLocationAvailable = true;

        // 如果資料庫已有紀錄且 is_booked 為 1 (代表技師已確認該時段的其他訂單)
        if (scheduleOpt.isPresent() && scheduleOpt.get().getIsBooked() == 1) {
            isLocationAvailable = false;
        }

        // 規則 3：檢查技師是否在該時段已有「其他」已確認或已付款的訂單
        if (isLocationAvailable) {
            List<InstallOrder> techOrders = orderRepository.findTechnicianActiveOrdersAtTime(
                    techNo, date, timeSlot.getCode());
            if (!techOrders.isEmpty()) {
                isLocationAvailable = false;
            }
        }

        // 若所有檢查皆通過，封裝成 VO 並加入返回列表
        if (isLocationAvailable) {
            AvailableSlotVO vo = new AvailableSlotVO();
            vo.setDate(date);
            vo.setTimeSlot(timeSlot.getCode());
            vo.setTimeSlotDesc(timeSlot.getDescription());
            vo.setLocationNo(location.getLocationNo());
            vo.setLocationName(location.getLocationName());
            vo.setIsAvailable(true);
            slots.add(vo);
        }
    }

    // ===================================================================================
    // 第二階段：軟性下單 (Phase 2: Create Soft Booking)
    // ===================================================================================
    /**
     * 買家建立預約訂單 (排隊機制)
     * 邏輯：檢查據點是否已被鎖定，若未鎖定 -> 建立「待確認 (PENDING)」訂單。
     * 重要：此階段「不更新」LocationSchedule，允許多位買家競爭同一個時段。
     * 
     * @param dto 預約請求資料
     * @return 產生的訂單主鍵
     */
    @Transactional
    public Integer createOrder(BookingRequestDTO dto) {
        // 1. 基本數據校驗
        MembersVO member = memberRepository.findById(dto.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("會員不存在"));
        Technician technician = technicianRepository.findById(dto.getTechNo())
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));
        InstallLocation location = locationRepository.findById(dto.getLocationNo())
                .orElseThrow(() -> new IllegalArgumentException("場地不存在"));

        // 2. 嚴格檢查據點是否已被其他人搶先「鎖定」
        Optional<LocationSchedule> scheduleOpt = scheduleRepository
                .findByLocationLocationNoAndAppointDateAndAppointTimeValue(
                        dto.getLocationNo(), dto.getAppointDate(), dto.getAppointTime());

        if (scheduleOpt.isPresent() && scheduleOpt.get().getIsBooked() == 1) {
            throw new BookingConflictException("該時段已被預訂，請選擇其他時段");
        }

        // 3. 封裝並建立訂單主檔 (狀態設為 PENDING)
        InstallOrder order = new InstallOrder();
        order.setMember(member);
        order.setTechnician(technician);
        order.setLocation(location);
        order.setAppointDate(dto.getAppointDate());
        order.setAppointTime(TimeSlot.fromCode(dto.getAppointTime()));
        order.setVenueFee(location.getPricePer()); // 鎖定當下的據點租借費用

        // 4. 計算服務項目金額並準備明細檔
        int servicesTotal = 0;
        List<InstallOrderDetail> details = new ArrayList<>();

        for (Integer tsNo : dto.getSelectedServiceIds()) {
            TechnicianService ts = technicianServiceRepository.findById(tsNo)
                    .orElseThrow(() -> new IllegalArgumentException("服務項目不存在: " + tsNo));

            servicesTotal += ts.getPrice();

            InstallOrderDetail detail = new InstallOrderDetail();
            detail.setTechnicianService(ts);
            detail.setPrice(ts.getPrice());
            detail.setInstallOrder(order);
            details.add(detail);
        }

        // 5. 設定總價與預設狀態
        order.setTotalPrice(servicesTotal + location.getPricePer());
        order.setPayStatus(PayStatus.UNPAID);
        order.setOrderStatus(OrderStatus.PENDING); // 關鍵：初始狀態為待確認
        order.setPayoutStatus(PayoutStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // 6. 持久化數據 (Order & Detail)
        InstallOrder savedOrder = orderRepository.save(order);
        for (InstallOrderDetail detail : details) {
            detail.setInstallOrder(savedOrder);
            orderDetailRepository.save(detail);
        }

        // 注意：此處完全不更動 LocationSchedule。
        return savedOrder.getInstallOrderNo();
    }

    // ===================================================================================
    // 第三階段：硬性鎖定 (Phase 3: Confirm Order / Hard Locking)
    // ===================================================================================
    /**
     * 技師接單確認
     * 邏輯：具有併發安全性檢查。技師點擊確認後，系統會正式鎖定據點排程。
     * 若同一時段有多筆 PENDING 訂單，第一筆被技師確認的會成功，其餘將在確認時失敗。
     * 
     * @param orderNo 訂單編號
     * @param techNo  執行確認操作的技師編號
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Integer orderNo, Integer techNo) {
        log.info("技師 {} 正在確認訂單 {}", techNo, orderNo);

        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        // 權限與狀態檢查
        if (!order.getTechnician().getTechNo().equals(techNo)) {
            throw new IllegalArgumentException("非此訂單之技師，無法執行操作");
        }
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalStateException("訂單狀態非待確認");
        }

        // 1. 檢查並鎖定場地排程 (Lock Location)
        // 使用資料庫排他鎖 (Pessimistic Lock) 或手動原子性檢查
        Optional<LocationSchedule> scheduleOpt = scheduleRepository
                .findAndLock(
                        order.getLocation().getLocationNo(),
                        order.getAppointDate(),
                        order.getAppointTimeValue());

        LocationSchedule schedule;

        if (scheduleOpt.isPresent()) {
            schedule = scheduleOpt.get();
            // 若已被鎖定，代表別的技師或別的訂單剛才搶先一步確認了該據點時段
            if (schedule.getIsBooked() == 1) {
                order.setOrderStatus(OrderStatus.CANCELLED); // 自動取消此訂單
                orderRepository.save(order);
                throw new BookingConflictException("動作太慢！該時段剛被搶走 (場地衝突)");
            }
        } else {
            // 若尚無紀錄，則建立新的排程紀錄
            schedule = new LocationSchedule();
            schedule.setLocation(order.getLocation());
            schedule.setAppointDate(order.getAppointDate());
            schedule.setAppointTimeValue(order.getAppointTimeValue());
            schedule.setIsBooked(0); // 先存，稍後再設為 1 以示嚴謹
            schedule = scheduleRepository.save(schedule);
        }

        // 2. 檢查技師自身排程是否有衝突 (Self-Booking Check)
        // 技師不能在同一個時間點於不同地方（或同地方）承接兩筆確認訂單
        List<InstallOrder> myOrders = orderRepository.findTechnicianActiveOrdersAtTime(
                techNo, order.getAppointDate(), order.getAppointTimeValue());

        // 過濾掉當前這筆訂單，檢查是否有其他「已確認但未完成」的訂單
        boolean isBusy = myOrders.stream().anyMatch(o -> !o.getInstallOrderNo().equals(orderNo) &&
                o.getOrderStatusValue() >= OrderStatus.AWAITING_PAYMENT.getCode());

        if (isBusy) {
            throw new TechnicianBusyException("您在該時段已有其他確認訂單 (自身衝突)");
        }

        // 3. 執行正式瑣定
        schedule.setIsBooked(1);
        scheduleRepository.save(schedule);

        // 4. 更新訂單狀態至「待付款」
        order.setSchedule(schedule);
        order.setOrderStatus(OrderStatus.AWAITING_PAYMENT);
        orderRepository.save(order);

        // 備註：此時其餘競爭同一個 slot 的 PENDING 訂單依然存在，但在下一個技師試圖確認時會被第 1 步擋掉。
    }

    /**
     * 技師拒絕接單
     */
    @Transactional
    public void rejectOrder(Integer orderNo, Integer techNo) {
        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (!order.getTechnician().getTechNo().equals(techNo)) {
            throw new IllegalArgumentException("無權限");
        }

        // 將訂單狀態設為已取消
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
