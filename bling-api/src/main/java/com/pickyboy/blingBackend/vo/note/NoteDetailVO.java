package com.pickyboy.blingBackend.vo.note;

import java.util.List;

import com.pickyboy.blingBackend.vo.tag.TagSimpleVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小记详情VO (新增)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDetailVO {
    private String id;
    private String title;     // 从content前200字符生成
    private String content;   // 完整内容
    private String createdAt;
    private String updatedAt;
    private List<TagSimpleVO> tags; // 使用TagSimpleVO
}