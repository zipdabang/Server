package zipdabang.server.domain;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.GenderType;
import zipdabang.server.domain.enums.StatusType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Entity
public class Users extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userId;

    private String name;

    @Column(unique = true, length = 30)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 18)
    private String phoneNum;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    private String address;
    @Column(length = 5)
    private String zipCode;

    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status = StatusType.ACTIVE;

    //inactive_date DATE

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(columnDefinition = "TEXT")
    private String profileUrl;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private InfoAgree infoAgree;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReceiveInfo> receiveInfoList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Questions> questionsList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comments> commentsList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Likes> likesList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Scraps> scrapsList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Recipes> recipesList;
}
