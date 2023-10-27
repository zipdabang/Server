package zipdabang.server.domain.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.SocialType;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Deregister extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 18)
    private String phoneNum;

    private String email;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private boolean passedSevenDays=false;

    @OneToMany(mappedBy = "deregister", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeregisterReason> deregisterReasonList;

    private String feedback;

}
