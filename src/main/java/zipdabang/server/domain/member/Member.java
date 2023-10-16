package zipdabang.server.domain.member;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.inform.PushAlarm;
import zipdabang.server.domain.inform.Question;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.domain.recipe.Scrap;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.GenderType;
import zipdabang.server.domain.enums.StatusType;
import zipdabang.server.domain.recipe.Comment;
import zipdabang.server.domain.recipe.Likes;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;


import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String email;

    @Column(length = 18)
    private String phoneNum;

    private String caption;

    @Column(length = 6)
    private String birth;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(columnDefinition = "boolean default false")
    private boolean isBarista;

    @Column(length = 5)
    private String zipCode;

    private String address;

    private String detailAddress;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status = StatusType.ACTIVE;

    private LocalDateTime inactivatedAt;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(columnDefinition = "TEXT")
    private String profileUrl;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<TermsAgree> termsAgree;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<FcmToken> fcmTokenList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Inquery> inqueryList;


    // 나를 따르는 놈들
    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL)
    private List<Follow> myFollowerList;

    // 내가 따르는 놈들
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL)
    private List<Follow> myFollowingList;

    @OneToMany(mappedBy = "ownerMember", cascade = CascadeType.ALL)
    private List<PushAlarm> pushAlarmList;

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
    public void setBasicInfo(int age, MemberResponseDto.MemberBasicInfoDto memberBasicInfoDto) {
        this.age=age;
        this.name = memberBasicInfoDto.getName();
        this.birth = memberBasicInfoDto.getBirth();
        this.gender = memberBasicInfoDto.getGenderType();
        this.phoneNum = memberBasicInfoDto.getPhoneNum();
    }

    public void setDetailInfo(MemberResponseDto.MemberDetailInfoDto memberDetailInfoDto) {
        this.zipCode = memberDetailInfoDto.getZipCode();
        this.address = memberDetailInfoDto.getAddress();
        this.detailAddress = memberDetailInfoDto.getDetailAddress();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void inactivateStatus(){
        this.status = StatusType.INACTIVE;}

    public void deleteMemberInfo(){
        String delete = "X";
        this.name=delete;
        this.email=delete;
        this.phoneNum=delete;
        this.caption=delete;
        this.birth=delete;
        this.age=0;
        this.gender=GenderType.DELETED;
        this.isBarista=false;
        this.zipCode=delete;
        this.address=delete;
        this.detailAddress=delete;
        this.socialType=SocialType.DELETED;
        this.status=StatusType.DEREGISTER;
    }
}
