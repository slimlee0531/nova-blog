package com.slim.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slim.blogbackend.entity.Category;
import com.slim.blogbackend.exception.BusinessException;
import com.slim.blogbackend.mapper.CategoryMapper;
import com.slim.blogbackend.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Transactional
    public Result<Category> createCategory(Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getSlug, category.getSlug());
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类标识已存在");
        }

        category.setArticleCount(0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);

        return Result.success(category);
    }

    @Transactional
    public Result<Category> updateCategory(Long id, Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        if (category.getName() != null) existing.setName(category.getName());
        if (category.getSlug() != null) existing.setSlug(category.getSlug());
        if (category.getDescription() != null) existing.setDescription(category.getDescription());
        if (category.getParentId() != null) existing.setParentId(category.getParentId());
        if (category.getSortOrder() != null) existing.setSortOrder(category.getSortOrder());

        existing.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(existing);

        return Result.success(existing);
    }

    @Transactional
    public Result<Void> deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        categoryMapper.deleteById(id);
        return Result.success();
    }

    public Result<List<Category>> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);
        return Result.success(categories);
    }

    public Result<Category> getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return Result.success(category);
    }
}
