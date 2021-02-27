package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //orderMode排序模式，默认为0。1是按帖子热度排序
    List<DiscussPost> selectDiscussPosts(
            @Param("userId") int userId, @Param("offset")int offset, @Param("limit")int limit, @Param("orderMode")int orderMode);

    // @Param注解用于给参数取别名，只有一个变量可以不加，要是有两个一定要加，所以一般都加上比较保险
    // sql在<if>里使用,则所有参数必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(@Param("id")int id);

    int updateCommentCount(@Param("id")int id, @Param("commentCount")int commentCount);

    int updateType(@Param("id")int id,@Param("type") int type);

    int updateStatus(@Param("id")int id, @Param("status")int status);

    int updateScore(@Param("id")int id, @Param("score")double score);
}
