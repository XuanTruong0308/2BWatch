package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Coupon;
import boiz.shop._2BShop.respository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    /**
     * Validate coupon code
     */
    public Coupon validateCoupon(String code, BigDecimal orderTotal) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);

        if (couponOpt.isEmpty()) {
            throw new RuntimeException("Mã giảm giá không tồn tại");
        }

        Coupon coupon = couponOpt.get();

        if (!coupon.isValid()) {
            throw new RuntimeException("Mã giảm giá đã hết hạn hoặc không còn hiệu lực");
        }

        if (!coupon.meetsMinimumOrder(orderTotal)) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này");
        }

        return coupon;
    }

    /**
     * Calculate discount amount
     */
    public BigDecimal calculateDiscount(String code, BigDecimal orderTotal) {
        try {
            Coupon coupon = validateCoupon(code, orderTotal);
            return coupon.calculateDiscount(orderTotal);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Mark coupon as used (increment usage count)
     */
    public void markCouponUsed(String code) {
        couponRepository.findByCode(code).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        });
    }
}
