package zipdabang.server.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INFO_AGREE")
public class InfoAgree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private Boolean info_others_agree;

    private LocalDateTime info_agree_date;

    private Boolean info_agree_boolean;

    @OneToOne
    @JoinColumn(name="user_id", nullable = false)
    private Users user;
}
