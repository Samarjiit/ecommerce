package com.ecommerce.project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Table(name = "cart_items")
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;


    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    //Many cart items can reference the same product.
//    User A adds “iPhone 15” to cart → one CartItem.
//    User B adds “iPhone 15” to cart → another CartItem.
//    Both point to the same Product.
//    @JoinColumn(name = "product_id")
//    The cart_items table will have a column product_id that acts as a foreign key pointing to the products table.

    private Integer quantity;
    private double discount;
    private double productPrice;




}
