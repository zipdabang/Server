package zipdabang.server.domain.member;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.inform.Question;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.Scrap;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.GenderType;
import zipdabang.server.domain.enums.StatusType;
import zipdabang.server.domain.recipe.Comment;
import zipdabang.server.domain.recipe.Likes;

import zipdabang.server.web.dto.requestDto.MemberRequestDto;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Entity
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long memberId;

    private String name;

    @Column(unique = true, length = 30)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 18)
    private String phoneNum;

    @Column(length = 6)
    private String birth;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(length = 5)
    private String zipCode;

    private String address;

    private String detailAddress;

    private Long followers;
    private Long followings;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status = StatusType.ACTIVE;

    //inactive_date DATE

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(columnDefinition = "TEXT")
    private String profileUrl;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private InfoAgree infoAgree;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ReceiveInfo> receiveInfoList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Question> questionList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Comment> commentList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Likes> likesList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Scrap> scrapList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Recipe> recipeList;

    public Member updateInfo(MemberRequestDto.MemberInfoDto request, int age, GenderType gender, InfoAgree infoAgree) {
        this.name = request.getName();
        this.age = age;
        this.infoAgree = infoAgree;
        this.nickname =request.getNickname();
        this.gender = gender;
        this.birth = request.getBirth();
        this.phoneNum = request.getPhoneNum();
        this.zipCode = request.getZipCode();
        this.address = request.getAddress();
        this.detailAddress =request.getDetailAddress();
        return this;
    }
}
