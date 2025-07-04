package com.pickyboy.blingBackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 小记与标签的关联表
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("note_tag_map")
public class NoteTagMap implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 雪花算法生成
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 小记id
     */
    private Long noteId;

    /**
     * 标签id
     */
    private Long tagId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记: 0-未删除, 1-已删除
     */
    @TableLogic
    private Boolean isDeleted;


}
