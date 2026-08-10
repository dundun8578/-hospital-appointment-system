package com.itheima.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private String nickname;
    private String email;
    private String content;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}
