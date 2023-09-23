package zipdabang.server.domain.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.DeregisterType;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@DynamicInsert
@DynamicUpdate
public class DeregisterReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "deregister_id")
    private Deregister deregister;

    @Enumerated(EnumType.STRING)
    private DeregisterType deregisterType;

}
