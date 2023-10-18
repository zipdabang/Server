package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zipdabang.server.domain.etc.SlangWord;

import java.util.List;

public interface SlangWordRepository extends JpaRepository<SlangWord, Long> {
    boolean existsByWord(String word);

    @Query("select s from SlangWord s where :nickname like concat('%',s.word,'%')")
    List<SlangWord> qExistsByWordContains(@Param("nickname") String nickname);

}
