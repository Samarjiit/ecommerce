package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional //all operation should work if one failed it should roll back
    public OrderDTO placeOrder(String emailId,Long addressId,String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {

        //Getting User cart - so that i can map the product into the order
        Cart cart=cartRepository.findCartByEmail(emailId);
        if(cart==null){
            throw new ResourceNotFoundException("Cart","email",emailId);
        }

        Address address=addressRepository.findById(addressId).orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        //create a new order with payment info
        Order order=new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);
        payment=paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder=orderRepository.save(order);

        //get items from the cart into the order items
        List<CartItem> cartItems=cart.getCartItems();
        if(cartItems.isEmpty())throw new APIException("Cart is empty");

        //transfer all the cart item to order items
        List<OrderItem>orderItems=new ArrayList<>();
        for(CartItem cartItem:cartItems){
            OrderItem orderItem=new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems=orderItemRepository.saveAll(orderItems);


        //update product stock
        cart.getCartItems().forEach(item->{
            int quantity=item.getQuantity();
            Product product=item.getProduct();
            product.setQuantity(product.getQuantity()-quantity);
            productRepository.save(product);



        //clear the cart
        cartService.deleteProductFromCart(cart.getCartId(),item.getProduct().getProductId());

        });

        //send back the order summary
        OrderDTO orderDTO=modelMapper.map(savedOrder,OrderDTO.class);
        orderItems.forEach(item->
                orderDTO.getOrderItems().add(
                        modelMapper.map(item, OrderItemDTO.class)));

        orderDTO.setAddressId(addressId);
        return orderDTO;
    }
}
//place order functionality
//So once the order hits the system, it comes in through the controller, wherein the user sends in the payment method.
// Which payment method needs to be followed along with the order request information. What is order request DTO information
// . We have all these fields like address ID, payment method, payment gateway related information. Okay. Now one thing we are
// not accepting over here is the product information. Why? Because how the functionality will work in application is cart is moved
// like all the products in the cart is moved to order. That's what an order is okay? If a user does not want to order one product in the cart,
// he can get it removed from the cart. Okay. So cart is converted to order. Okay. So that we already have in the system. So we don't need product
// information. Now we are getting the logged in user email. And we are passing in all the information to that of the order service. Now what is order
// service. Do so here in order service okay. We have this method which is quietly accepting everything it is getting the user's card first,
// because card is of course that is being converted to order. It is getting the address because we need the object. Okay, then we are creating
// the order over here. Then order needs payment information. So we are creating the payment information like the payment object, setting
// the order against the payment and saving the payment information. We have the saved payment information over here which we are setting
// that of order okay. And then order is also saved. So payment is saved first here and then order is saved. So payment is saved here.
// And then order is saved. Okay. We have saved order object. Now what we do is our job. Now next is to transform the cart items to that of order items.
// Okay. Actually our job is to create order items and save it. Okay, but how do you create order items through cart items? That's the only way, right?
// So you get cart items, okay. And then you start creating a list of order items when you are you're transforming every cart item. So every cart item you're transforming
// and you're getting the information and creating the list of order items populating the list of order items. Okay. Then you're saving the order items
// as well. Okay. And then the post order related tasks start coming in, like updating the stock, clearing the cart, and sending back the order summary.
// So what we do is we update the stock when we get the product, and we reduce the product stock by the quantity ordered and we save it back.
// This we do for every cart item. Right. Because those are the products that were ordered. And then as we update the stock we delete the item from the
// cart as well. So cart is cleared in the end. And then we create this order summary. And then we return this to the user.