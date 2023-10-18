package zipdabang.server.domain.etc;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.WordType;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@DiscriminatorColumn(name = "WordType")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class BannedWord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String word;

//    @Column(nullable = false)
//    @Enumerated(value = EnumType.STRING)
//    private WordType wordType;

    public BannedWord(String word) {
        this.word=word;
    }
}
