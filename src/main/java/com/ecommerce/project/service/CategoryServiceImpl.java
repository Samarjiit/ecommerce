package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{


    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        List<Category>categories=categoryRepository.findAll();
        if(categories.isEmpty())throw new APIException("No category created till now.");

        return categories;
        //return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
//        category.setCategoryId(nextId++);
        Category savedCategory=categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory!=null)throw new APIException("Category with the name " + category.getCategoryName() +  " already exists!!!");

        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
//        List<Category> categories = categoryRepository.findAll();
//
//        Category category = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst()
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
//
//        categoryRepository.delete(category);
//        return "Category with categoryId: " + categoryId + " deleted successfully !!";

        Category category=categoryRepository.findById(categoryId)
                //.orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"resource not found!!"));
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        categoryRepository.delete(category);
        return "category with categoryid: "+ categoryId + " deleted successfully";

    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
//        List<Category> categories = categoryRepository.findAll();
//
//        Optional<Category> optionalCategory = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst();
//
//        if(optionalCategory.isPresent()){
//            Category existingCategory = optionalCategory.get();
//            existingCategory.setCategoryName(category.getCategoryName());
//            Category savedCategory = categoryRepository.save(existingCategory);
//            return savedCategory;
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
//        }
        Category savedCategory=categoryRepository.findById(categoryId)
                //.orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resources not found!!"));
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));


        category.setCategoryId(categoryId);
        savedCategory=categoryRepository.save(category);
        return savedCategory;

    }
}
