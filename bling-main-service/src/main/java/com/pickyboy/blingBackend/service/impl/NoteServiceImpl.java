package com.pickyboy.blingBackend.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.dto.note.CreateNoteRequest;
import com.pickyboy.blingBackend.dto.note.DeleteNotesRequest;
import com.pickyboy.blingBackend.dto.note.SetNoteTagsRequest;
import com.pickyboy.blingBackend.dto.note.UpdateNoteRequest;
import com.pickyboy.blingBackend.entity.NoteTagMap;
import com.pickyboy.blingBackend.entity.Tags;
import com.pickyboy.blingBackend.entity.Notes;
import com.pickyboy.blingBackend.vo.note.NoteDetailVO;
import com.pickyboy.blingBackend.vo.note.NoteListVO;
import com.pickyboy.blingBackend.vo.tag.TagSimpleVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.mapper.NotesMapper;
import com.pickyboy.blingBackend.service.INoteService;
import com.pickyboy.blingBackend.service.ITagService;
import com.pickyboy.blingBackend.service.INoteTagMapService;
import com.pickyboy.blingBackend.mapper.TagsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小记服务实现类
 *
 * @author shiqi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NotesMapper, Notes> implements INoteService {

    @Autowired
    private ITagService tagService;

    @Autowired
    private INoteTagMapService noteTagMapService;

    @Autowired
    private TagsMapper tagsMapper;

    @Override
    public List<NoteListVO> getNoteList(Long tagId,String keyword, Integer page, Integer limit, String sortBy, String order) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 如果提供了搜索关键词，优先使用搜索功能（忽略分页参数）
        if (keyword != null && !keyword.trim().isEmpty()) {
            return searchNotes(keyword);
        }

        // 构建分页对象
        Page<Notes> pageObj = new Page<>(page, limit);

        // 构建查询条件
        LambdaQueryWrapper<Notes> wrapper = new LambdaQueryWrapper<Notes>()
                .eq(Notes::getUserId, userId);

        // 如果有标签筛选
        if (tagId != null) {
            // 先查询出该标签关联的所有小记ID
            List<Long> noteIds = noteTagMapService.list(
                            new LambdaQueryWrapper<NoteTagMap>()
                                    .eq(NoteTagMap::getTagId, tagId)
                    ).stream()
                    .map(NoteTagMap::getNoteId)
                    .collect(Collectors.toList());

            if (noteIds.isEmpty()) {
                return new ArrayList<>();
            }

            wrapper.in(Notes::getId, noteIds);
        }

        // 设置排序
        if ("createdAt".equals(sortBy)) {
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Notes::getCreatedAt);
            } else {
                wrapper.orderByDesc(Notes::getCreatedAt);
            }
        } else {
            // 默认按 updatedAt 排序
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Notes::getUpdatedAt);
            } else {
                wrapper.orderByDesc(Notes::getUpdatedAt);
            }
        }

        Page<Notes> notePage = page(pageObj, wrapper);

        // 转换为VO
        List<NoteListVO> voList = notePage.getRecords().stream()
                .map(this::convertToNoteListVO)
                .collect(Collectors.toList());

        return voList;
    }

    @Override
    @Transactional
    public NoteDetailVO createNote(CreateNoteRequest createNoteRequest) {
        log.info("创建新的小记: title={}");

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 创建小记
        Notes note = new Notes();
        note.setUserId(userId);
        note.setContent(createNoteRequest.getContent());

        String oriContent = createNoteRequest.getContent().trim();
        String headContent = oriContent.substring(0, Math.min(oriContent.length(), 50));
        String purifiedContent = getFirstLineTextContent(headContent);
        // 从内容生成title,会判断内容是否为空
        note.setTitle(generateTitleFromContent(purifiedContent));

        boolean saved = save(note);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记创建失败");
        }

        // 处理标签关联
        if (createNoteRequest.getTagIds() != null && !createNoteRequest.getTagIds().isEmpty()) {
            setNoteTagsInternal(note.getId(), createNoteRequest.getTagIds());
        }

        // 重新查询保存后的完整数据（包含自动生成的时间戳,及标签列表）
        Notes savedNote = getById(note.getId());
        return convertToNoteDetailVO(savedNote);
    }


    @Override
    public NoteDetailVO getNoteDetail(Long noteId) {
        log.info("获取小记详情: noteId={}", noteId);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        return convertToNoteDetailVO(note);
    }

    @Transactional
    @Override
    public void updateNote(Long noteId, UpdateNoteRequest updateNoteRequest) {
        log.info("更新小记: noteId={}", noteId);

        Long userId = CurrentHolder.getCurrentUserId();

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        // 更新小记内容和标题
        note.setContent(updateNoteRequest.getContent());
        // 从新内容生成新的title
        note.setTitle(generateTitleFromContent(updateNoteRequest.getContent()));

        boolean updated = updateById(note);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记更新失败");
        }
    }

    @Override
    @Transactional
    public void deleteNotes(DeleteNotesRequest deleteNotesRequest) {
        List<String> noteIds = deleteNotesRequest.getNoteIds();

        log.info("批量删除小记: noteIds={}", noteIds);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 转换为Long类型
        List<Long> longNoteIds = noteIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 验证小记是否属于当前用户
        long count = count(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getUserId, userId)
                        .in(Notes::getId, longNoteIds)
        );

        if (count != longNoteIds.size()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的小记ID");
        }

        // 获取要删除的小记关联的标签，用于更新标签引用计数
        List<NoteTagMap> relationsToDelete = noteTagMapService.list(
                new LambdaQueryWrapper<NoteTagMap>()
                        .in(NoteTagMap::getNoteId, longNoteIds)
        );

        // 统计每个标签的引用次数减少量
        Map<Long, Long> tagDecrementMap = relationsToDelete.stream()
                .collect(Collectors.groupingBy(
                        NoteTagMap::getTagId,
                        Collectors.counting()
                ));

        // 删除小记
        boolean notesDeleted = removeByIds(longNoteIds);

        // 删除小记-标签关联关系
        if (!relationsToDelete.isEmpty()) {
            List<Long> relationIds = relationsToDelete.stream()
                    .map(NoteTagMap::getId)
                    .collect(Collectors.toList());

            boolean relationsDeleted = noteTagMapService.removeByIds(relationIds);

            if (!relationsDeleted) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除小记标签关联失败");
            }

            // 更新标签引用计数
            if(!tagDecrementMap.isEmpty()) {
                tagsMapper.batchDecrementReferedCount(tagDecrementMap);
            }
        }

        if (!notesDeleted) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记删除失败");
        }

        log.info("批量删除小记成功，数量: {}", longNoteIds.size());
    }

    @Override
    @Transactional
    public void setNoteTags(Long noteId, SetNoteTagsRequest setNoteTagsRequest) {
        List<String> tagIds = setNoteTagsRequest.getTagIds();

        log.info("设置小记标签: noteId={}, tagIds={}", noteId, tagIds);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        setNoteTagsInternal(noteId, tagIds);
    }

    @Override
    public List<TagSimpleVO> getNoteTags(Long noteId){
        log.info("获取小记标签: noteId={}", noteId);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        // 获取小记关联的标签ID
        List<Long> tagIds = noteTagMapService.list(
                        new LambdaQueryWrapper<NoteTagMap>()
                                .eq(NoteTagMap::getNoteId, noteId)
                ).stream()
                .map(NoteTagMap::getTagId)
                .collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取标签详情
        List<Tags> tags = tagService.list(
                new LambdaQueryWrapper<Tags>()
                        .in(Tags::getId, tagIds)
                        .orderByAsc(Tags::getName)
        );

        return tags.stream()
                .map(this::convertToTagSimpleVO)
                .collect(Collectors.toList());
    }

    /**
     * 搜索小记 (模糊匹配content)
     */
    @Override
    public List<NoteListVO> searchNotes(String keyword) {
        log.info("搜索小记: keyword={}", keyword);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 构建查询条件 - 模糊匹配content
        LambdaQueryWrapper<Notes> wrapper = new LambdaQueryWrapper<Notes>()
                .eq(Notes::getUserId, userId)
                .like(Notes::getContent, keyword.trim())
                .orderByDesc(Notes::getUpdatedAt); // 按更新时间倒序

        List<Notes> notes = list(wrapper);

        // 转换为VO
        return notes.stream()
                .map(this::convertToNoteListVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为NoteDetailVO
     */
    private NoteDetailVO convertToNoteDetailVO(Notes note) {
        NoteDetailVO vo = new NoteDetailVO();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());

        // 设置时间字段
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (note.getCreatedAt() != null) {
            vo.setCreatedAt(note.getCreatedAt().format(formatter));
        }
        if (note.getUpdatedAt() != null) {
            vo.setUpdatedAt(note.getUpdatedAt().format(formatter));
        }

        // 获取关联的标签
        List<TagSimpleVO> tags = getNoteTags(note.getId());
        vo.setTags(tags);

        return vo;
    }


    /**
     * 转换为NoteListVO
     */
    private NoteListVO convertToNoteListVO(Notes note) {
        NoteListVO vo = new NoteListVO();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(note.getTitle());

        // 设置时间字段 - 注意：列表页面通常只需要updatedAt
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (note.getUpdatedAt() != null) {
            vo.setUpdatedAt(note.getUpdatedAt().format(formatter));
        }

        return vo;
    }

    public static final Map<String, Integer> BLOCK_LEVEL_TAG_MAP = new HashMap<String, Integer>() {{
        // Headers
        put("h1", 1); put("/h1", -1);
        put("h2", 1); put("/h2", -1);
        put("h3", 1); put("/h3", -1);
        put("h4", 1); put("/h4", -1);
        put("h5", 1); put("/h5", -1);
        put("h6", 1); put("/h6", -1);

        // Paragraph and text containers
        put("p", 1); put("/p", -1);
        put("blockquote", 1); put("/blockquote", -1);
        put("pre", 1); put("/pre", -1);
        put("hr", -1);

        // Lists
        put("ul", 1); put("/ul", -1);
        put("ol", 1); put("/ol", -1);
        put("li", 1); put("/li", -1);
        put("dl", 1); put("/dl", -1);
        put("dt", 1); put("/dt", -1);
        put("dd", 1); put("/dd", -1);

        // Main layout and semantic elements
        put("div", 1); put("/div", -1);
        put("main", 1); put("/main", -1);
        put("section", 1); put("/section", -1);
        put("article", 1); put("/article", -1);
        put("aside", 1); put("/aside", -1);
        put("header", 1); put("/header", -1);
        put("footer", 1); put("/footer", -1);
        put("nav", 1); put("/nav", -1);
        put("figure", 1); put("/figure", -1);
        put("figcaption", 1); put("/figcaption", -1);

        // Forms
        put("form", 1); put("/form", -1);
        put("fieldset", 1); put("/fieldset", -1);

        // Tables
        put("table", 1); put("/table", -1);

        // Others
        put("address", 1); put("/address", -1);

        // Forced line break
        put("br", -1);put("br/",-1);
    }};
/*
*
* 去除文本中的HTML标签
* */
    private String getFirstLineTextContent(String headContent) {
        StringBuilder res = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        Deque<Character> stack = new ArrayDeque<>();
        for(char c : headContent.toCharArray()){
            if(c=='<'){
                // 把之前缓存的字符加入结果
                res.append( temp);
                temp.setLength(0);
                stack.push(c);
            }else if(c=='>'){
                // 匹配到换行标签,结束并返回结果
                if(BLOCK_LEVEL_TAG_MAP.getOrDefault(temp.toString(),999).equals(-1 )){
                    return res.toString();
                }
                // 清空标签内容,继续匹配
                temp.setLength(0);
            }else {
                // 不在标签内的文本,缓存
               temp.append(c);
            }
        }
        if(!stack.isEmpty()){
            return res.toString();
        }
        return res.append( temp).toString();
    }



    /**
     * 从content生成title（前200字符）
     */
    private String generateTitleFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "无内容";
        }

        String trimmedContent = content.trim();

        // 按换行符分割，取第一行
        String[] lines = trimmedContent.split("\\r?\\n");
        String firstLine = lines[0].trim();
        if(firstLine.length()>15){
            firstLine = firstLine.substring(0,15);
            firstLine+="...";
        }

        // 如果第一行为空，返回"无内容"
        if (firstLine.isEmpty()) {
            return "无内容";
        }

        return firstLine;
    }

    /**
     * 转换为TagSimpleVO
     */
    private TagSimpleVO convertToTagSimpleVO(Tags tag) {
        TagSimpleVO vo = new TagSimpleVO();
        vo.setId(String.valueOf(tag.getId()));
        vo.setName(tag.getName());

        // 不设置时间字段，因为：
        // 1. TagSimpleVO是简化版本，主要用于显示标签基本信息
        // 2. 小记的时间信息已经在NoteDetailVO的根级别提供
        // 3. 如果需要在标签级别显示小记时间，考虑使用专门的VO类

        return vo;
    }

    /**
     * 内部方法：设置小记标签关联
     */
    private void setNoteTagsInternal(Long noteId, List<String> tagIds) {
        // 如果提供了标签ID，验证标签是否属于当前用户
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> longTagIds = tagIds.stream()
                    .filter(Objects::nonNull)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
                // 验证标签是否属于当前用户
            if (!longTagIds.isEmpty()) {
                Long userId = CurrentHolder.getCurrentUserId();
                long tagCount = tagService.count(
                        new LambdaQueryWrapper<Tags>()
                                .eq(Tags::getUserId, userId)
                                .in(Tags::getId, longTagIds)
                );

                if (tagCount != longTagIds.size()) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的标签ID");
                }
            }
        }

        // 获取当前小记关联的标签
        List<NoteTagMap> currentRelations = noteTagMapService.list(
                new LambdaQueryWrapper<NoteTagMap>()
                        .eq(NoteTagMap::getNoteId, noteId)
        );
        // 当前关联了的标签ID
        Set<Long> currentTagIds = currentRelations.stream()
                .map(NoteTagMap::getTagId)
                .collect(Collectors.toSet());

        // 新的标签ID
        Set<Long> newTagIdSet = tagIds != null ?
                tagIds.stream()
                        .filter(Objects::nonNull)
                        .map(Long::valueOf)
                        .collect(Collectors.toSet()) :
                new HashSet<>();

        // 计算需要添加和删除的标签
        Set<Long> toAdd = new HashSet<>(newTagIdSet);
        // 需要添加的关联记录
        toAdd.removeAll(currentTagIds);

        Set<Long> toRemove = new HashSet<>(currentTagIds);
        // 需要删除的关联记录
        toRemove.removeAll(newTagIdSet);

        // 删除不需要的关联
        if (!toRemove.isEmpty()) {
            noteTagMapService.remove(
                    new LambdaQueryWrapper<NoteTagMap>()
                            .eq(NoteTagMap::getNoteId, noteId)
                            .in(NoteTagMap::getTagId, toRemove)
            );

            // 更新标签引用计数

                tagService.update(
                        new LambdaUpdateWrapper<Tags>()
                                .in(Tags::getId, toRemove)
                                .setSql("refered_count = GREATEST(refered_count - 1, 0)"));


        }

        // 添加新的关联
        if (!toAdd.isEmpty()) {
            List<NoteTagMap> newRelations = toAdd.stream()
                    .map(tagId -> {
                        NoteTagMap relation = new NoteTagMap();
                        relation.setNoteId(noteId);
                        relation.setTagId(tagId);
                        return relation;
                    })
                    .collect(Collectors.toList());

            boolean relationsAdded = noteTagMapService.saveBatch(newRelations);
            if (!relationsAdded) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加标签关联失败");
            }

            // 更新标签引用计数
                tagService.update(
                        new LambdaUpdateWrapper<Tags>()
                                .in(Tags::getId, toAdd)
                                .setSql("refered_count = refered_count + 1"));

        }

        log.info("设置小记标签成功，noteId: {}, 添加: {}, 删除: {}",
                noteId, toAdd, toRemove);
    }
}