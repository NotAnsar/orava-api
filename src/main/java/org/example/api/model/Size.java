package org.example.api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "sizes")
public class Size {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "fullname")
    private String fullname;

    @OneToMany(mappedBy = "size")
    private List<Product> products;
}