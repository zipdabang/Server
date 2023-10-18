package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.etc.BannedWord;

public interface BannedWordRepository extends JpaRepository<BannedWord, Long> {

}
