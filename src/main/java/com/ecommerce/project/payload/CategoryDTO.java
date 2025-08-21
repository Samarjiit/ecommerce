package com.ecommerce.project.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;
}

//So category DTO is used to encapsulate and transfer data related to categories from client to server.
//So if you want to transfer any data related to category you will make use of category DTO.Okay.
//So earlier we were simply making use of category model to change anything
//And if there are any change in uh if there is any change in the model well this will also be impacted.And we wanted to decouple this.
//So we introduced category DTO which is like a model but it's actually not a model.
//It is representing category at the presentation layer.