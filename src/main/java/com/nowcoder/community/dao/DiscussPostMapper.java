package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(
            @Param("userId") int userId, @Param("offset")int offset, @Param("limit")int limit);

    // @Param注解用于给参数取别名，只有一个变量可以不加，要是有两个一定要加，所以一般都加上比较保险
    // sql在<if>里使用,则所有参数必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

}
