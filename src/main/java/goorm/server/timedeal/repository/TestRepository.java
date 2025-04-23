package goorm.server.timedeal.repository;

import goorm.server.timedeal.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Long> {
}
