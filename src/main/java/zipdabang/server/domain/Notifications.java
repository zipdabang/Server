package zipdabang.server.domain;

import javax.persistence.*;

@Entity
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
