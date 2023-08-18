package zipdabang.server.domain.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@DynamicInsert
@DynamicUpdate
@Entity
public class Terms {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String termsTitle;

    private String termsBody;

    @OneToMany(mappedBy = "terms", cascade = CascadeType.ALL)
    private List<TermsAgree> termsAgreeList;
}
