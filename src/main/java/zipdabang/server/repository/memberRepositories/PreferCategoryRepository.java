package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.MemberPreferCategory;

public interface PreferCategoryRepository extends JpaRepository<MemberPreferCategory, Long> {
}