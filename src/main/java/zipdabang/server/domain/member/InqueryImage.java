package zipdabang.server.domain.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@DynamicInsert
@DynamicUpdate
@Entity
public class InqueryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquery_id")
    private Inquery inquery;

    public void setInquery(Inquery inquery){
        if (this.inquery != null)
            inquery.getInqueryImageList().remove(this);
        this.inquery = inquery;
        inquery.getInqueryImageList().add(this);
    }
}
