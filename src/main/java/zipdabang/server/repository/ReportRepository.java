package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
