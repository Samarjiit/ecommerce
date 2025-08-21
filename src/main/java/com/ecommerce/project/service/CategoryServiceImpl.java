package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{


    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
//    we are first fetching the categories like we do from the database. And then. What we are doing is for every category in the list,
//    we are mapping every category to that of category DTO and we are using the concept of stream over here because the
//    category object is in a list. So first we are converting categories into stream and then for every category we are making use
//    of model mapper to convert it into a object of type category And then we are collecting it as a list. You can see over here okay.
//    And then we are making use of this to create a category response object.
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize,String sortBy,String sortOrder) {
        //sort order
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();

        Pageable  pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Category>categoryPage=categoryRepository.findAll(pageDetails);
        List<Category>categories=categoryPage.getContent();

      //  List<Category>categories=categoryRepository.findAll();
        if(categories.isEmpty())throw new APIException("No category created till now.");

        List<CategoryDTO>categoryDTOS=categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse=new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse;
        //return categoryRepository.findAll();
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category=modelMapper.map(categoryDTO,Category.class);
        Category categoryFromDb=categoryRepository.findByCategoryName(category.getCategoryName());
        if(categoryFromDb!=null)throw new APIException("Category with the name " + category.getCategoryName() +  " already exists!!!");
        Category savedCategory=categoryRepository.save(category);
        return modelMapper.map(savedCategory,CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {

        Category category=categoryRepository.findById(categoryId)
                //.orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"resource not found!!"));
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        categoryRepository.delete(category);
        return  modelMapper.map(category,CategoryDTO.class);
//        return "category with categoryid: "+ categoryId + " deleted successfully";

    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category savedCategory=categoryRepository.findById(categoryId)
                //.orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resources not found!!"));
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        Category category=modelMapper.map(categoryDTO,Category.class);
        category.setCategoryId(categoryId);
        savedCategory=categoryRepository.save(category);
        return modelMapper.map(savedCategory,CategoryDTO.class);

    }
}
