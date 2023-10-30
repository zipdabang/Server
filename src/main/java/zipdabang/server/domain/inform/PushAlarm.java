package zipdabang.server.domain.inform;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class PushAlarm extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String body;

    private Boolean isConfirmed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member ownerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member targetMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe targetRecipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private Notification targetNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AlarmCategory alarmCategory;

    public void setMember(Member member){
        if (this.ownerMember != null)
            this.ownerMember.getPushAlarmList().remove(this);
        this.ownerMember = member;
        member.getPushAlarmList().add(this);
    }

    public void setRecipe(Recipe recipe){
        if (this.targetRecipe != null)
            this.targetRecipe.getPushAlarmList().remove(this);
        this.targetRecipe = recipe;
        recipe.getPushAlarmList().add(this);
    }
}
