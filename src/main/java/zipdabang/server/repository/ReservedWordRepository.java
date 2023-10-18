package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.etc.ReservedWord;

public interface ReservedWordRepository extends JpaRepository<ReservedWord, Long> {
    boolean existsByWord(String word);

}
