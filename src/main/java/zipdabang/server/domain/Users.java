package zipdabang.server.domain;

import javax.persistence.*;
import zipdabang.server.domain.enums.GenderType;
import zipdabang.server.domain.enums.StatusType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long user_id;

    private String name;

    @Column(unique = true, length = 30)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 18)
    private String phone_num;

    private int age;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(length = 5)
    private String zip_code;

    private String address;

    private String detail_address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status = StatusType.ACTIVE;

    //inactive_date DATE

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(columnDefinition = "TEXT")
    private String profile_url;

    //created_at DATETIME
    private LocalDateTime created_at;

    //updated_at DATETIME
    private LocalDateTime updated_at;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private InfoAgree infoAgree;

    @OneToMany(mappedBy = "user")
    private List<ReceiveInfo> receiveInfoList;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private List<Questions> questionsList;

    @OneToMany(mappedBy = "user")
    private List<Comments> commentsList;

    @OneToMany(mappedBy = "user")
    private List<Likes> likesList;

    @OneToMany(mappedBy = "user")
    private List<Scraps> scrapsList;

    @OneToMany(mappedBy = "user")
    private List<Recipes> recipesList;
}
