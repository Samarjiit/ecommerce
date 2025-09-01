package com.ecommerce.project.payload;


//represent the payment details of particular order


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long paymentId;
    private String pgPaymentId;
    private String pgStatus;
    private String pgResponseMessage;
    private String pgName;




}
