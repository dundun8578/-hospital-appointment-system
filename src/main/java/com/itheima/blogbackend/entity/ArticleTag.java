package com.itheima.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("article_tag")
public class ArticleTag {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long tagId;
}
