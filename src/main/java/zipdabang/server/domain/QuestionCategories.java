package zipdabang.server.domain;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class QuestionCategories extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long questions;

    @OneToMany(mappedBy = "questionCategories", cascade = CascadeType.ALL)
    private List<Questions> questionsList;

    @OneToMany(mappedBy = "questionCategories", cascade = CascadeType.ALL)
    private List<FAQ> faqList;
}
