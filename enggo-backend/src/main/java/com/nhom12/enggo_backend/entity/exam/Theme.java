package com.nhom12.enggo_backend.entity.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "themes")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "theme_name", nullable = false, unique = true, length = 100)
    String themeName;

    @Column(nullable = false, length = 50)
    String category;

    @Column(name = "theme_description", columnDefinition = "text")
    String themeDescription;

    @Column(name = "is_active")
    Boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    LocalDateTime updatedAt;
}
