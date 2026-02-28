package com.firomsa.inventory.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.ProductUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.ProfileUpdateDTO;
import com.firomsa.inventory.v1.dto.RegisterRequestDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.dto.UserUpdateRequestDTO;
import com.firomsa.inventory.v1.mapper.CategoryMapper;
import com.firomsa.inventory.v1.mapper.ProductMapper;
import com.firomsa.inventory.v1.mapper.UserMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.util.stream.Stream;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureTestRestTemplate
@SuppressWarnings("resource")
public abstract class AbstractIntegrationTest {
    private static final String RUSTFS_ACCESS_KEY = "rustfsadmin";
    private static final String RUSTFS_SECRET_KEY = "rustfsadmin";
    private static final String TEST_BUCKET = "test-bucket";

    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    private static final GenericContainer<?> mailhog =
            new GenericContainer<>("mailhog/mailhog:latest").withExposedPorts(1025, 8025);

    private static final GenericContainer<?> s3 = new GenericContainer<>("rustfs/rustfs:latest")
            .withExposedPorts(9000, 9001).withEnv("RUSTFS_ACCESS_KEY", RUSTFS_ACCESS_KEY)
            .withEnv("RUSTFS_SECRET_KEY", RUSTFS_SECRET_KEY);

    private static void ensureBucketExists() {
        URI endpoint = URI.create("http://" + s3.getHost() + ":" + s3.getMappedPort(9000));

        try (S3Client s3Client = S3Client.builder().endpointOverride(endpoint)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create(RUSTFS_ACCESS_KEY, RUSTFS_SECRET_KEY)))
                .region(Region.US_EAST_1).serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build()) {
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(TEST_BUCKET).build());
            } catch (S3Exception exception) {
                if (exception.statusCode() == 404) {
                    s3Client.createBucket(
                            CreateBucketRequest.builder().bucket(TEST_BUCKET).build());
                } else {
                    throw exception;
                }
            }
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        Startables.deepStart(Stream.of(postgres, mailhog, s3)).join();
        ensureBucketExists();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.jdbc-url", postgres::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.mail.host", mailhog::getHost);
        registry.add("spring.mail.port", () -> mailhog.getMappedPort(1025));
        registry.add("spring.cloud.aws.s3.endpoint",
                () -> "http://" + s3.getHost() + ":" + s3.getMappedPort(9000));
        registry.add("spring.cloud.aws.s3.path-style-access-enabled", () -> true);
        registry.add("spring.cloud.aws.credentials.access-key", () -> RUSTFS_ACCESS_KEY);
        registry.add("spring.cloud.aws.credentials.secret-key", () -> RUSTFS_SECRET_KEY);
        registry.add("s3.bucket-name", () -> TEST_BUCKET);
    }

    @TestConfiguration
    static class MapperFallbackConfig {
        private static final DateTimeFormatter DATE_FORMATTER =
                DateTimeFormatter.ofPattern("dd.MM.yyyy");

        @Bean
        UserMapper userMapper() {
            return new UserMapper() {
                @Override
                public UserResponseDTO toDTO(User user) {
                    UserResponseDTO dto = new UserResponseDTO();
                    dto.setId(user.getId());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setPhone(user.getPhone());
                    dto.setRole(user.getRole() != null ? user.getRole().getName().name() : null);
                    dto.setCreatedAt(
                            user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER)
                                    : null);
                    dto.setActive(user.isActive());
                    dto.setEnabled(user.isEnabled());
                    dto.setProfilePictureUrl(null);
                    return dto;
                }

                @Override
                public User toModel(RegisterRequestDTO requestDTO) {
                    User user = new User();
                    user.setFirstName(requestDTO.firstName());
                    user.setLastName(requestDTO.lastName());
                    user.setUsername(requestDTO.username());
                    user.setPassword(requestDTO.password());
                    user.setEmail(requestDTO.email());
                    user.setPhone(requestDTO.phone());
                    return user;
                }

                @Override
                public void updateModelFromDTO(User user, UserUpdateRequestDTO requestDTO) {
                    user.setFirstName(requestDTO.firstName());
                    user.setLastName(requestDTO.lastName());
                    user.setUsername(requestDTO.username());
                    user.setPassword(requestDTO.password());
                    user.setEmail(requestDTO.email());
                    user.setPhone(requestDTO.phone());
                }

                @Override
                public void updateModelFromDTO(User user, ProfileUpdateDTO requestDTO) {
                    user.setFirstName(requestDTO.firstName());
                    user.setLastName(requestDTO.lastName());
                    user.setUsername(requestDTO.username());
                    user.setPassword(requestDTO.password());
                    user.setEmail(requestDTO.email());
                    user.setPhone(requestDTO.phone());
                }
            };
        }

        @Bean
        ProductMapper productMapper() {
            return new ProductMapper() {
                @Override
                public ProductResponseDTO toDTO(Product product) {
                    ProductResponseDTO dto = new ProductResponseDTO();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setSku(product.getSku());
                    dto.setDescription(product.getDescription());
                    dto.setSellingPrice(product.getSellingPrice());
                    dto.setCostPrice(product.getCostPrice());
                    dto.setQuantity(product.getQuantity());
                    dto.setLowStockThreshold(product.getLowStockThreshold());
                    dto.setActive(product.isActive());
                    dto.setCreatedAt(product.getCreatedAt());
                    dto.setUpdatedAt(product.getUpdatedAt());
                    dto.setCategoryIds(toCategoryIds(product.getCategories()));
                    dto.setImageUrls(null);
                    return dto;
                }

                @Override
                public Product toModel(ProductRequestDTO request) {
                    Product product = new Product();
                    product.setName(request.getName());
                    product.setSku(request.getSku());
                    product.setDescription(request.getDescription());
                    product.setSellingPrice(request.getSellingPrice());
                    product.setCostPrice(request.getCostPrice());
                    product.setQuantity(request.getQuantity());
                    product.setLowStockThreshold(request.getLowStockThreshold());
                    return product;
                }

                @Override
                public void updateModelFromDTO(ProductUpdateRequestDTO update, Product product) {
                    if (update.getName() != null) {
                        product.setName(update.getName());
                    }
                    if (update.getSku() != null) {
                        product.setSku(update.getSku());
                    }
                    if (update.getDescription() != null) {
                        product.setDescription(update.getDescription());
                    }
                    if (update.getSellingPrice() != null) {
                        product.setSellingPrice(update.getSellingPrice());
                    }
                    if (update.getCostPrice() != null) {
                        product.setCostPrice(update.getCostPrice());
                    }
                    if (update.getQuantity() != null) {
                        product.setQuantity(update.getQuantity());
                    }
                    if (update.getLowStockThreshold() != null) {
                        product.setLowStockThreshold(update.getLowStockThreshold());
                    }
                    if (update.getActive() != null) {
                        product.setActive(update.getActive());
                    }
                }

                @Override
                public Set<UUID> toCategoryIds(Set<Category> categories) {
                    if (categories == null || categories.isEmpty()) {
                        return Set.of();
                    }
                    return categories.stream().map(Category::getId).collect(Collectors.toSet());
                }
            };
        }

        @Bean
        CategoryMapper categoryMapper() {
            return new CategoryMapper() {
                @Override
                public CategoryResponseDTO toDTO(Category category) {
                    CategoryResponseDTO dto = new CategoryResponseDTO();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    dto.setCreatedAt(category.getCreatedAt());
                    dto.setUpdatedAt(category.getUpdatedAt());
                    dto.setProductIds(toProductIds(category.getProducts()));
                    return dto;
                }

                @Override
                public Category toModel(CategoryRequestDTO request) {
                    Category category = new Category();
                    category.setName(request.getName());
                    return category;
                }

                @Override
                public void updateModelFromDTO(CategoryUpdateRequestDTO update, Category category) {
                    if (update.getName() != null) {
                        category.setName(update.getName());
                    }
                }

                @Override
                public Set<UUID> toProductIds(Set<Product> products) {
                    if (products == null || products.isEmpty()) {
                        return Set.of();
                    }
                    return products.stream().map(Product::getId).collect(Collectors.toSet());
                }
            };
        }
    }


    @Test
    void verify() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
        assertThat(mailhog.isCreated()).isTrue();
        assertThat(mailhog.isRunning()).isTrue();
        assertThat(s3.isCreated()).isTrue();
        assertThat(s3.isRunning()).isTrue();
    }
}
