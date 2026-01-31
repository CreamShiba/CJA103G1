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
    // Phase 1: Query Available Slots (Dynamic Calculation)
    // ===================================================================================
    /**
     * Get available slots for the next 30 days
     * Logic: Dynamic calculation, skip weekends, check database for locks
     */
    public List<AvailableSlotVO> getAvailableSlots(Integer techNo, Integer locationNo) {
        List<AvailableSlotVO> availableSlots = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Get the location
        InstallLocation location = locationRepository.findById(locationNo)
                .orElseThrow(() -> new IllegalArgumentException("場地不存在"));

        // Loop for next 30 days
        for (int i = 1; i <= 30; i++) {
            LocalDate targetDate = today.plusDays(i);
            DayOfWeek dayOfWeek = targetDate.getDayOfWeek();

            // Filter Rule 1: Skip Saturday and Sunday
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }

            // Check Morning (0) and Afternoon (1)
            checkAndAddSlot(availableSlots, location, targetDate, TimeSlot.MORNING, techNo);
            checkAndAddSlot(availableSlots, location, targetDate, TimeSlot.AFTERNOON, techNo);
        }

        return availableSlots;
    }

    private void checkAndAddSlot(List<AvailableSlotVO> slots, InstallLocation location,
            LocalDate date, TimeSlot timeSlot, Integer techNo) {

        // Filter Rule 2: Check LocationSchedule in DB
        Optional<LocationSchedule> scheduleOpt = scheduleRepository
                .findByLocationLocationNoAndAppointDateAndAppointTimeValue(
                        location.getLocationNo(), date, timeSlot.getCode());

        boolean isLocationAvailable = true;

        // If record exists and is_booked = 1 -> Not Available
        if (scheduleOpt.isPresent() && scheduleOpt.get().getIsBooked() == 1) {
            isLocationAvailable = false;
        }

        // Additional Check: Is technician already busy?
        if (isLocationAvailable) {
            List<InstallOrder> techOrders = orderRepository.findTechnicianActiveOrdersAtTime(
                    techNo, date, timeSlot.getCode());
            if (!techOrders.isEmpty()) {
                isLocationAvailable = false;
            }
        }

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
    // Phase 2: Create Soft Booking (No Locking)
    // ===================================================================================
    /**
     * Buyer creates an order (Queueing)
     * Logic: Check if locked (is_booked=1), if not -> Create PENDING order
     * DO NOT update LocationSchedule
     */
    @Transactional
    public Integer createOrder(BookingRequestDTO dto) {
        // 1. Basic Validation
        MembersVO member = memberRepository.findById(dto.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("會員不存在"));
        Technician technician = technicianRepository.findById(dto.getTechNo())
                .orElseThrow(() -> new IllegalArgumentException("技師不存在"));
        InstallLocation location = locationRepository.findById(dto.getLocationNo())
                .orElseThrow(() -> new IllegalArgumentException("場地不存在"));

        // 2. Check if locked (Hard Check)
        // We only block if it's explicitly booked (is_booked = 1)
        Optional<LocationSchedule> scheduleOpt = scheduleRepository
                .findByLocationLocationNoAndAppointDateAndAppointTimeValue(
                        dto.getLocationNo(), dto.getAppointDate(), dto.getAppointTime());

        if (scheduleOpt.isPresent() && scheduleOpt.get().getIsBooked() == 1) {
            throw new BookingConflictException("該時段已被預訂，請選擇其他時段");
        }

        // 3. Create Order (PENDING)
        InstallOrder order = new InstallOrder();
        order.setMember(member);
        order.setTechnician(technician);
        order.setLocation(location);
        order.setAppointDate(dto.getAppointDate());
        order.setAppointTime(TimeSlot.fromCode(dto.getAppointTime()));
        order.setVenueFee(location.getPricePer());

        // Calculate services total
        int servicesTotal = 0;
        List<InstallOrderDetail> details = new ArrayList<>();

        for (Integer tsNo : dto.getSelectedServiceIds()) {
            TechnicianService ts = technicianServiceRepository.findById(tsNo)
                    .orElseThrow(() -> new IllegalArgumentException("服務項目不存在: " + tsNo));

            servicesTotal += ts.getPrice();

            InstallOrderDetail detail = new InstallOrderDetail();
            detail.setTechnicianService(ts);
            detail.setPrice(ts.getPrice());
            detail.setInstallOrder(order); // Link will be set after saving order?
                                           // Ideally save order first or cascade
            details.add(detail);
        }

        order.setTotalPrice(servicesTotal + location.getPricePer());
        order.setPayStatus(PayStatus.UNPAID);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPayoutStatus(PayoutStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // Save Order
        InstallOrder savedOrder = orderRepository.save(order);

        // Save Details
        for (InstallOrderDetail detail : details) {
            detail.setInstallOrder(savedOrder);
            orderDetailRepository.save(detail);
        }

        // Note: We DO NOT touch LocationSchedule here. It remains 0 or null.

        return savedOrder.getInstallOrderNo();
    }

    // ===================================================================================
    // Phase 3: Confirm Order (Hard Locking)
    // ===================================================================================
    /**
     * Technician confirms order
     * Logic: Transactional, Race Condition Check, Lock Schedule
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Integer orderNo, Integer techNo) {
        log.info("Technician {} confirming order {}", techNo, orderNo);

        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (!order.getTechnician().getTechNo().equals(techNo)) {
            throw new IllegalArgumentException("非此訂單之技師，無法執行操作");
        }

        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalStateException("訂單狀態非待確認");
        }

        // 1. Check & Lock Location (PESSIMISTIC_WRITE recommended or explicit
        // check-then-act)
        // First, check if already booked by query
        Optional<LocationSchedule> scheduleOpt = scheduleRepository
                .findAndLock(
                        order.getLocation().getLocationNo(),
                        order.getAppointDate(),
                        order.getAppointTimeValue());

        LocationSchedule schedule;

        if (scheduleOpt.isPresent()) {
            schedule = scheduleOpt.get();
            if (schedule.getIsBooked() == 1) {
                // Already taken by someone else
                // Auto-reject this order? Or just throw exception?
                order.setOrderStatus(OrderStatus.CANCELLED); // Or specific REJECTED status
                orderRepository.save(order);
                throw new BookingConflictException("動作太慢！該時段剛被搶走 (場地衝突)");
            }
        } else {
            // Create new schedule record if not exists
            schedule = new LocationSchedule();
            schedule.setLocation(order.getLocation());
            schedule.setAppointDate(order.getAppointDate());
            schedule.setAppointTimeValue(order.getAppointTimeValue());
            schedule.setIsBooked(0); // Set to 1 later to be explicit
            schedule = scheduleRepository.save(schedule);
        }

        // 2. Check Technician Availability (Double Booking Check)
        // Find if *I* have confirmed another order at this time
        List<InstallOrder> myOrders = orderRepository.findTechnicianActiveOrdersAtTime(
                techNo, order.getAppointDate(), order.getAppointTimeValue());

        // Filter out the current order itself if it appeared (though it's pending, so
        // query should check accepted only)
        // The query `findTechnicianActiveOrdersAtTime` should exclude CANCELLED.
        // But we want to see if I have *other* confirmed/paid orders.
        // If I have any non-cancelled order at this time... wait, PENDING orders are
        // okay to coexist.
        // We only care if *I* have a LOCKED order (AWAITING_PAYMENT, PAID, COMPLETED).

        boolean isBusy = myOrders.stream().anyMatch(o -> !o.getInstallOrderNo().equals(orderNo) &&
                o.getOrderStatusValue() >= OrderStatus.AWAITING_PAYMENT.getCode());

        if (isBusy) {
            throw new TechnicianBusyException("您在該時段已有其他確認訂單 (自身衝突)");
        }

        // 3. EXECUTE LOCK
        schedule.setIsBooked(1);
        scheduleRepository.save(schedule);

        // 4. Update Order
        order.setSchedule(schedule);
        order.setOrderStatus(OrderStatus.AWAITING_PAYMENT);
        orderRepository.save(order);

        // 5. Cleanup other pending orders for same slot? (Optional)
        // For simplicity, we leave them. They will fail when technician tries to
        // confirm.
    }

    /**
     * Technician rejects order
     */
    @Transactional
    public void rejectOrder(Integer orderNo, Integer techNo) {
        InstallOrder order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (!order.getTechnician().getTechNo().equals(techNo)) {
            throw new IllegalArgumentException("無權限");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
