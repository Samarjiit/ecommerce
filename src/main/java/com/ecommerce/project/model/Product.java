package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @NotBlank
    @Size(min = 3,message = "Product name must contains atleast 3 characters")
    private String productName;

    @NotBlank
    @Size(min = 6,message = "Product description must contains atleast 6 characters")
    private String description;
    private Integer quantity;
    private double price;
    private double discount;
    private double specialPrice;
    private String image;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    //user will persist in product table with name seller_id
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @OneToMany(mappedBy = "product",cascade = {CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.EAGER)
    private List<CartItem>products=new ArrayList<>();

//    One Product → Many CartItems
//    A single product (e.g., "iPhone 15") can appear in many cart items (across different users’ carts).
//    mappedBy = "product"
//    This tells JPA that the relationship is controlled by the product field inside CartItem.
//    i.e., CartItem has the foreign key (product_id).
//    cascade = {PERSIST, MERGE}
//    When you save/update a product, its related cart items are also saved/updated.
//    Notice that REMOVE is not included → deleting a product will not automatically delete all its cart items (to prevent accidental cart corruption).
//    fetch = FetchType.EAGER
//    Whenever you load a product, JPA will also fetch all its cart items immediately.
//        Example: If you query Product p = productRepo.findById(1L), it will also load every cart item containing that product.


}
