package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.InqueryImage;

public interface InqueryImageRepository extends JpaRepository<InqueryImage, Long> {
}
