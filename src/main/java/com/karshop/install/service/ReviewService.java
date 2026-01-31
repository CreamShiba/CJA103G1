package com.karshop.install.service;

import com.karshop.install.entity.InstallOrder;
import com.karshop.install.entity.Technician;
import com.karshop.install.entity.TechnicianReview;
import com.karshop.install.enums.OrderStatus;
import com.karshop.install.repository.InstallOrderRepository;
import com.karshop.install.repository.TechnicianRepository;
import com.karshop.install.repository.TechnicianReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final TechnicianReviewRepository reviewRepository;
    private final InstallOrderRepository orderRepository;
    private final TechnicianRepository technicianRepository;

    @Transactional(readOnly = true)
    public boolean hasReviewed(Integer orderId) {
        return reviewRepository.existsByInstallOrderInstallOrderNo(orderId);
    }

    @Transactional
    public void submitReview(Integer orderId, Integer rating, String comment) {
        if (reviewRepository.existsByInstallOrderInstallOrderNo(orderId)) {
            throw new RuntimeException("This order has already been reviewed.");
        }

        InstallOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Can only review completed orders.");
        }

        TechnicianReview review = new TechnicianReview();
        review.setInstallOrder(order);
        review.setMember(order.getMember());
        review.setTechnician(order.getTechnician());
        review.setRatingStar(rating);
        review.setReviewContent(comment);

        reviewRepository.save(review);

        // Update Technician's average rating (optional but good practice)
        updateTechnicianRating(order.getTechnician());
    }

    private void updateTechnicianRating(Technician technician) {
        java.util.List<TechnicianReview> reviews = reviewRepository.findByTechnicianTechNo(technician.getTechNo());

        int totalStars = reviews.stream().mapToInt(TechnicianReview::getRatingStar).sum();
        int count = reviews.size();

        technician.setRatingStar(totalStars);
        technician.setRatingAmount(count);

        technicianRepository.save(technician);
    }
}
