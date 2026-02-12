package com.firomsa.inventory.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "selling_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal sellingPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "cost_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal costPrice;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Min(0)
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold;

    @NotNull
    @Builder.Default
    private boolean active = Boolean.TRUE;

    @ManyToMany(mappedBy = "products")
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_key")
    @Builder.Default
    private List<String> imageKeys = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public List<String> getImageKeys() {
        return new ArrayList<>(imageKeys);
    }

    public void setImageKeys(List<String> imageKeys) {
        this.imageKeys = imageKeys != null ? new ArrayList<>(imageKeys) : new ArrayList<>();
    }

    public Set<Category> getCategories() {
        return new HashSet<>(categories);
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories != null ? new HashSet<>(categories) : new HashSet<>();
    }
}
