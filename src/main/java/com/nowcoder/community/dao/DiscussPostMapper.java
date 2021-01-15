package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(
            @Param("userId") int userId, @Param("offset")int offset, @Param("limit")int limit);

    // @Param注解用于给参数取别名,
    // sql在<if>里使用,则所有参数必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

}
