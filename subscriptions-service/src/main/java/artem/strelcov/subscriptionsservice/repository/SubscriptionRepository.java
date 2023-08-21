package artem.strelcov.subscriptionsservice.repository;

import artem.strelcov.subscriptionsservice.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription,Integer> {

}
