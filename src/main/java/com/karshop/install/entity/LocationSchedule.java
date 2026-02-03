package com.karshop.install.entity;

import com.karshop.install.enums.TimeSlot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "location_schedule", uniqueConstraints = @UniqueConstraint(columnNames = { "location_no", "appoint_date",
        "appoint_time" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_no")
    private Integer scheduleNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_no", nullable = false)
    private InstallLocation location;

    @Column(name = "appoint_date", nullable = false)
    private LocalDate appointDate;

    @Column(name = "appoint_time", nullable = false)
    private Integer appointTimeValue;

    @Column(name = "is_booked", nullable = false)
    private Integer isBooked = 0;

    // Transient field for enum conversion
    @Transient
    public TimeSlot getAppointTime() {
        return TimeSlot.fromCode(appointTimeValue);
    }

    @Transient
    public void setAppointTime(TimeSlot timeSlot) {
        this.appointTimeValue = timeSlot.getCode();
    }
}
