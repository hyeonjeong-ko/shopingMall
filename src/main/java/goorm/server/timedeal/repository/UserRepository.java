package goorm.server.timedeal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import goorm.server.timedeal.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByLoginId(String loginId);

}
