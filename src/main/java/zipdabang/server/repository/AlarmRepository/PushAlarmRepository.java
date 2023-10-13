package zipdabang.server.repository.AlarmRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.inform.PushAlarm;
import zipdabang.server.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface PushAlarmRepository extends JpaRepository<PushAlarm, Long> {

    Page<PushAlarm> findByOwnerMember(Member member, PageRequest pageRequest);


    List<PushAlarm> findByTitleAndOwnerMemberAndIsConfirmedFalse(String s, Member member);
}
