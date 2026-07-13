package com.slim.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slim.blogbackend.entity.Bookmark;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookmarkMapper extends BaseMapper<Bookmark> {
}
