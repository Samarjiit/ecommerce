package com.ecommerce.project.controller;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api") //define at class level
public class CategoryController {

    //EITHER U CAN USE AUTOWIRED OR CONSTRUCTOR FOR MAPPING WITH SERVICE LAYER
    @Autowired
    private CategoryService categoryService;

//    public CategoryController(CategoryService categoryService) {
//        this.categoryService = categoryService;
//    }

    @GetMapping("/public/categories")
    public ResponseEntity<List<Category>>getAllCategories(){
        List<Category>categories=categoryService.getAllCategories();
        return new ResponseEntity<>(categories,HttpStatus.OK);
    }

    //@PostMapping("/api/public/categories")
    @RequestMapping(value = "/public/categories",method = RequestMethod.POST)
    public ResponseEntity<String> createCategory(@Valid @RequestBody Category category){
        categoryService.createCategory(category);
        return  new ResponseEntity<>("category addedd successfully",HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId){

        String status = categoryService.deleteCategory(categoryId);
        return  new ResponseEntity<>(status,HttpStatus.OK);
        //validatio will be handled on service
//        try {
//            String status = categoryService.deleteCategory(categoryId);
//        return new ResponseEntity<>(status, HttpStatus.OK); or
//            return ResponseEntity.status(HttpStatus.OK).body(status);
//
//        }catch(ResponseStatusException e){
//            return new ResponseEntity<>(e.getReason(),e.getStatusCode());
//        }
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<String>updateCategory(@Valid @RequestBody Category category,@PathVariable Long categoryId){
        Category savedCategory=categoryService.updateCategory(category,categoryId);
        return new ResponseEntity<>("Updated Category with category id: "+categoryId ,HttpStatus.OK);
//        try{
//            Category savedCategory=categoryService.updateCategory(category,categoryId);
//            return new ResponseEntity<>("Updated Category with category id: "+categoryId ,HttpStatus.OK);
//        }catch(ResponseStatusException e){
//                return new ResponseEntity<>(e.getReason(),e.getStatusCode());
//        }
    }

}
