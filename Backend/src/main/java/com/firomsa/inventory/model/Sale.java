package com.firomsa.inventory.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sales")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Sale {
    @Id
    @GeneratedValue
    private UUID id;
    @NotNull
    private Integer quantity;
    @NotNull
    private Double salePrice;
    @ManyToOne
    @JoinColumn(name = "sold_by_id", referencedColumnName = "id")
    @NotNull
    private User soldBy;
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @NotNull
    private Product product;
    @CreatedDate
    private LocalDateTime timestamp;
}
