package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.PasswordResetToken;
import boiz.shop._2BShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(
            User user,
            LocalDateTime currentTime);

    void deleteByExpiryDateBefore(LocalDateTime currentTime);

    void deleteByUser(User user);
}
