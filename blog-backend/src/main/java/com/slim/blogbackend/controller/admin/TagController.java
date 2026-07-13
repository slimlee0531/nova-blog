package com.slim.blogbackend.controller.admin;

import com.slim.blogbackend.entity.Tag;
import com.slim.blogbackend.service.TagService;
import com.slim.blogbackend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public Result<Tag> createTag(@RequestBody Tag tag) {
        return tagService.createTag(tag);
    }

    @PutMapping("/{id}")
    public Result<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        return tagService.updateTag(id, tag);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        return tagService.deleteTag(id);
    }

    @GetMapping("/list")
    public Result<List<Tag>> getTagList() {
        return tagService.getTagList();
    }

    @GetMapping("/{id}")
    public Result<Tag> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id);
    }
}
