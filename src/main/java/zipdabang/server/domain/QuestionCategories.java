package zipdabang.server.domain;

import javax.persistence.*;
import java.util.List;

@Entity
public class QuestionCategories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String tinytext;

    @OneToMany(mappedBy = "questionCategories")
    private List<Questions> questionsList;

    @OneToMany(mappedBy = "questionCategories")
    private List<FAQ> faqList;
}
