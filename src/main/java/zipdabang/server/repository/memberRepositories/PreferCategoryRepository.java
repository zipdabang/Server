package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberPreferCategory;

import java.util.List;

public interface PreferCategoryRepository extends JpaRepository<MemberPreferCategory, Long> {
    List<MemberPreferCategory> findByMember(Member member);

    void deleteByMember(Member member);
}
