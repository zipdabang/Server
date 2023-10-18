package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.MemberViewMethod;

import java.util.Optional;

public interface MemberViewMethodRepository extends JpaRepository<MemberViewMethod, Long> {
    Optional<MemberViewMethod> findByMember(Member member);
}
