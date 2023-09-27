package zipdabang.server.repository.memberRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Inquery;
import zipdabang.server.domain.member.Member;

public interface InqueryRepository extends JpaRepository<Inquery, Long> {

    Page<Inquery> findByMember(Member member, PageRequest pageRequest);
}
