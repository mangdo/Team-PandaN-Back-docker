package com.example.teampandanback.dto.note.response;

import com.example.teampandanback.domain.note.Step;
import lombok.Getter;

@Getter
public class NoteEachSearchInBookmarkResponseDto {
    private Long noteId;
    private String title;
    private Step step;
    private Long projectId;
    private String projectTitle;
    private String writer;

    public NoteEachSearchInBookmarkResponseDto(Long noteId, String title, Step step, Long projectId, String projectTitle, String writer) {
        this.noteId = noteId;
        this.title = title;
        this.step = step;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.writer = writer;
    }
}