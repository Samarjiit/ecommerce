package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements  CartService{


    @Autowired
    CartRepository cartRepository;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        //find existing cart or create
        Cart cart=createCart();

        //retrieve product details from db
        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        //perform validation
                //check if product is already exist in the cart
                //qty is in stock or not
                //if request qty is more that stock
        CartItem cartItem =cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(),productId);
        if(cartItem!=null)throw new APIException("Product "+ product.getProductName() + "already exisit in the cart");
        if(product.getQuantity()==0)throw new APIException(product.getProductName() + "is not available");
        if(product.getQuantity()<quantity)throw new APIException("Please make an order of the "+ product.getProductName()
                + "less than or equal to the quantity " +product.getQuantity());

        //create cart item
        CartItem newCartItem=new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        //save cart item
        cartItemRepository.save(newCartItem);

        //do post cart item save related task
         product.setQuantity(product.getQuantity());
         cart.setTotalPrice(cart.getTotalPrice()+ (product.getSpecialPrice()*quantity));
         cartRepository.save(cart);

        //return updated cart information
        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);

        List<CartItem> cartItems=cart.getCartItems();

        Stream<ProductDTO> productStream=cartItems.stream().map(item->{
            ProductDTO map=modelMapper.map(item.getProduct(),ProductDTO.class);
            map.setQuantity(item.getQuantity()); //update the qty from cart item
            return  map;
        });

        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart>carts=cartRepository.findAll();
        if(carts.size()==0)throw new APIException("No cart exists");

        List<CartDTO>cartDTOs= carts.stream().map(cart -> {
            CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);

            //So earlier we were just updating the or we were mapping the product to the product DTO.
            //But here we are mapping the product to the product video and then updating the quantity in the product
            List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity()); // Set the quantity from CartItem
                return productDTO;
            }).collect(Collectors.toList());

            cartDTO.setProducts(products);
            return cartDTO;
        }).collect(Collectors.toList());

        return cartDTOs;
    }


    //So first what we are  fetching the cart over here using the cart repository.
    // And we are doing this with the help of provided email ID and the cart ID by calling this particular method over here,
    // which is fine cart by email ID and the cart ID.  we are checking if the cart is null. If it's null, then we are throwing a resource not found exception.
    // Okay then over here we are mapping this particular cart object to the cart DTO type okay. This is important because the output that you're supposed to give,
    // the response that you're supposed to give is in the form of a DTO over here. Okay. Then we are updating the quantity.
    // We are updating the quantity over here. So we are iterating through each cart item in the cart object. And we are setting the quantity of each corresponding product object.
    // And this is done to ensure that the product objects within the cart have their quantities properly set. Okay. So this line of code maps each product object within the carts cart
    // item to a product DTO object using the model mapper. Okay. And this creates a list of product data objects representing the products in the cart.
    // Okay. And then we are setting this list as with the cart DTO. And then we are returning the cart DTO.
    @Override
    public CartDTO getCart(String emailId, Long cartId) {

        Cart cart=cartRepository.findCartByEmailAndCartId(emailId,cartId);
        if(cart==null)throw new ResourceNotFoundException("Cart","cartId",cartId);

        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
        cart.getCartItems().forEach(c->c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO>products =cart.getCartItems().stream()
                .map(p->modelMapper.map(p.getProduct(),ProductDTO.class)).toList();
        cartDTO.setProducts(products);

        return cartDTO;
    }
    @Transactional //either all operation are done or none of them are done
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        //validation
        //cart exist or not
        //qty in stock or not
        String emailId=authUtil.loggedInEmail();
        Cart userCart=cartRepository.findCartByEmail(emailId);
        Long cartId=userCart.getCartId();

        Cart cart=cartRepository.findById(cartId).orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));

        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        if(product.getQuantity()==0)throw new APIException(product.getProductName() + "is not available");

        if(product.getQuantity()<quantity)throw new APIException("Please make an order of the "+ product.getProductName()
                + "less than or equal to the quantity " +product.getQuantity());

        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);

        if(cartItem==null)throw new APIException("Product "+product.getProductName() + "not available in the cart!!!");

        //cal new qty
        int newQuantity=cartItem.getQuantity()+quantity;

        //validate to prevent -ve qty
        if(newQuantity<0)throw  new APIException("The resulting qty cannot be negative!!!");

        if(newQuantity==0)deleteProductFromCart(cartId,productId);
        else {
            //update the cart item
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }
        CartItem updatedItem=cartItemRepository.save(cartItem);


        if(updatedItem.getQuantity()==0)cartItemRepository.deleteById(updatedItem.getCartItemId());

        //create dto to send response to the user
        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
        List<CartItem>cartItems=cart.getCartItems();

        Stream<ProductDTO>productStream=cartItems.stream().map(item->{
            ProductDTO prd=modelMapper.map(item.getProduct(),ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });
        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
    //validation for cart is valid or not
    Cart cart=cartRepository.findById(cartId)
            .orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));

    CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
    if(cartItem==null)throw new ResourceNotFoundException("Product","productId",productId);


    //update the price once the product is deleted from the cart
    cart.setTotalPrice(cart.getTotalPrice()-(cartItem.getProductPrice()*cartItem.getQuantity()));
    cartItemRepository.deleteCartItemByProductIdAndCardId(cartId,productId);

    return "Product "+cartItem.getProduct().getProductName()+ " removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        //validation

        Cart cart=cartRepository.findById(cartId).orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));

        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem==null)
        {
            throw new APIException("Product " +product.getProductName()+ " not available in the cart !!!" );
        }

        //update the price in the cart. so old price got reduce and new product price is updated
        //1000 - 100*2 = 800
         double cartPrice=cart.getTotalPrice()-(cartItem.getProductPrice()*cartItem.getQuantity());
         //200
         cartItem.setProductPrice(product.getSpecialPrice());
         //800 + (200*2) = 1200
         cart.setTotalPrice(cartPrice +(cartItem.getProductPrice()*cartItem.getQuantity()));

         cartItem=cartItemRepository.save(cartItem);
    }

    private Cart createCart(){
        Cart userCart=cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart!=null)return userCart;
        Cart cart=new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart=cartRepository.save(cart);
        return newCart;

    }
}
