package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Comment;
import zipdabang.server.domain.recipe.ReportedComment;

public interface ReportedCommentRepository extends JpaRepository<ReportedComment, Long> {
    Boolean existsByReportIdAndReportedAndOwner(Report findReport, Comment findComment, Member member);
}
