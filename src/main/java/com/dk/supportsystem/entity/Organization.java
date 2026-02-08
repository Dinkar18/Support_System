package com.dk.supportsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Organization extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
}
