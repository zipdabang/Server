package zipdabang.server.domain;

import javax.persistence.*;

@Entity
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String thumbnail_url;

    @Column(columnDefinition = "TEXT")
    private String description;
}
