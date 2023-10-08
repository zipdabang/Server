package zipdabang.server.repository.memberRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zipdabang.server.domain.member.BlockedMember;
import zipdabang.server.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface BlockedMemberRepository extends JpaRepository<BlockedMember, Long> {
    List<BlockedMember> findByOwner(Member owner);

    @Query("select b.blocked from BlockedMember b where b.owner = :owner ")
    List<Member> findBlockedByOwner(Member owner);
    @Query("select b.blocked from BlockedMember b where b.owner = :owner ")
    Page<Member> findBlockedByOwner(@Param("owner") Member owner, PageRequest pageRequest);
    Optional<BlockedMember> findByOwnerAndBlocked(Member owner, Member blocked);

    boolean existsByOwnerAndBlocked(Member owner, Member blocked);

    void deleteByOwnerAndBlocked(Member owner, Member blocked);

}
