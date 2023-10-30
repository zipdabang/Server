package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberReport;

import java.util.Optional;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {

    Optional<MemberReport> findByReporterAndReported(Member reporter, Member reported);
}
