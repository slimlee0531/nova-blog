package com.slim.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slim.blogbackend.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT a.* FROM articles a LEFT JOIN article_tags at ON a.id = at.article_id LEFT JOIN tags t ON at.tag_id = t.id WHERE t.slug = #{tagSlug} AND a.status = 'PUBLISHED' ORDER BY a.published_at DESC")
    List<Article> selectByTagSlug(@Param("tagSlug") String tagSlug);

    @Select("SELECT a.* FROM articles a LEFT JOIN categories c ON a.category_id = c.id WHERE c.slug = #{categorySlug} AND a.status = 'PUBLISHED' ORDER BY a.published_at DESC")
    List<Article> selectByCategorySlug(@Param("categorySlug") String categorySlug);
}
