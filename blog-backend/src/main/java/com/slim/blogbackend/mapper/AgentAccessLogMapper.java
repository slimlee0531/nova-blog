package com.slim.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slim.blogbackend.entity.AgentAccessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentAccessLogMapper extends BaseMapper<AgentAccessLog> {
}
