package com.slim.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slim.blogbackend.entity.Tag;
import com.slim.blogbackend.exception.BusinessException;
import com.slim.blogbackend.mapper.TagMapper;
import com.slim.blogbackend.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    @Transactional
    public Result<Tag> createTag(Tag tag) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, tag.getName());
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("标签名已存在");
        }

        LambdaQueryWrapper<Tag> slugWrapper = new LambdaQueryWrapper<>();
        slugWrapper.eq(Tag::getSlug, tag.getSlug());
        if (tagMapper.selectCount(slugWrapper) > 0) {
            throw new BusinessException("标签标识已存在");
        }

        tag.setArticleCount(0);
        tag.setCreatedAt(LocalDateTime.now());
        tagMapper.insert(tag);

        return Result.success(tag);
    }

    @Transactional
    public Result<Tag> updateTag(Long id, Tag tag) {
        Tag existing = tagMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("标签不存在");
        }

        if (tag.getName() != null) existing.setName(tag.getName());
        if (tag.getSlug() != null) existing.setSlug(tag.getSlug());
        if (tag.getDescription() != null) existing.setDescription(tag.getDescription());
        if (tag.getColor() != null) existing.setColor(tag.getColor());

        tagMapper.updateById(existing);

        return Result.success(existing);
    }

    @Transactional
    public Result<Void> deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        tagMapper.deleteById(id);
        return Result.success();
    }

    public Result<List<Tag>> getTagList() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Tag::getArticleCount);
        List<Tag> tags = tagMapper.selectList(wrapper);
        return Result.success(tags);
    }

    public Result<Tag> getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return Result.success(tag);
    }

    public Result<Tag> getTagBySlug(String slug) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getSlug, slug);
        Tag tag = tagMapper.selectOne(wrapper);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return Result.success(tag);
    }
}
