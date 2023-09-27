package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.BlockedMember;
import zipdabang.server.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface BlockedMemberRepository extends JpaRepository<BlockedMember, Long> {
    List<BlockedMember> findByOwner(Member owner);
    Optional<BlockedMember> findByOwnerAndBlocked(Member owner, Member blocked);

    boolean existsByOwnerAndBlocked(Member owner, Member blocked);

}
