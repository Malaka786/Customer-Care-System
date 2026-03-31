package lk.sliit.customer_care.repository;

import lk.sliit.customer_care.modelentity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
}
