package com.tanhua.dubbo.server.enums;

/**
 * @author: tang
 * @date: Create in 21:17 2021/8/9
 * @description:评论类型  点赞：1  评论：2  喜欢：3
 */
public enum CommentType {
    LIKE(1),COMMENT(2),LOVE(3);
    int type;
    CommentType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
