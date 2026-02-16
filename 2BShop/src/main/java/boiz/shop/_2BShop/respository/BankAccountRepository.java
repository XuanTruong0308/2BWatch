package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    List<BankAccount> findByIsActiveTrueOrderByDisplayOrderAsc();
}
