package zipdabang.server.domain;

import javax.persistence.*;

@Entity
public class ReceiveInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;

    @Column(length = 18)
    private String Phone_num;

    @Column(length = 5)
    private String zip_code;

    private String address;

    private String detail_address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private Users user;


    //payments
}
