package com.slim.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slim.blogbackend.entity.KnowledgeBaseDocument;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeBaseDocumentMapper extends BaseMapper<KnowledgeBaseDocument> {
}
