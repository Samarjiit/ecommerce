package com.ecommerce.project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE},orphanRemoval = true)
    private List<CartItem> cartItems=new ArrayList<>();

    private Double totalPrice=0.0;

}

//A cart can contain multiple items (1-to-many relationship).
//mappedBy = "cart" → The CartItem entity has a field named cart which owns the relationship.
//cascade → What happens to cartItems when you modify the cart:
//PERSIST → When you save the cart, items are saved too.
//MERGE → When you update the cart, items are updated too.
//REMOVE → When you delete the cart, items are deleted too.
//orphanRemoval = true → If an item is removed from cartItems list, it will also be deleted from DB.
