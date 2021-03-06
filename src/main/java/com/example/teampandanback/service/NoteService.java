package com.example.teampandanback.service;

import com.example.teampandanback.domain.Comment.CommentRepository;
import com.example.teampandanback.domain.bookmark.Bookmark;
import com.example.teampandanback.domain.bookmark.BookmarkRepository;
import com.example.teampandanback.domain.note.Note;
import com.example.teampandanback.domain.note.NoteRepository;
import com.example.teampandanback.domain.note.Step;
import com.example.teampandanback.domain.project.Project;
import com.example.teampandanback.domain.project.ProjectRepository;
import com.example.teampandanback.domain.user.User;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMapping;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMappingRepository;
import com.example.teampandanback.dto.auth.SessionUser;
import com.example.teampandanback.dto.note.request.NoteCreateRequestDto;
import com.example.teampandanback.dto.note.request.NoteUpdateRequestDto;
import com.example.teampandanback.dto.note.response.*;
import com.example.teampandanback.dto.note.response.noteEachSearchInTotalResponseDto;
import com.example.teampandanback.dto.note.response.NoteSearchInTotalResponseDto;
import com.example.teampandanback.exception.ApiRequestException;
import com.example.teampandanback.utils.PandanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserProjectMappingRepository userProjectMappingRepository;
    private final ProjectRepository projectRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;
    private final PandanUtils pandanUtils;

    // Note ์์ธ ์กฐํ
    @Transactional
    public NoteResponseDto readNoteDetail(Long noteId, SessionUser sessionUser) {
        NoteResponseDto noteResponseDto = noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new ApiRequestException("์์ฑ๋ ๋ธํธ๊ฐ ์์ต๋๋ค."));

        Optional<Bookmark> bookmark = bookmarkRepository.findByUserIdAndNoteId(sessionUser.getUserId(), noteId);
        noteResponseDto.setBookmark(bookmark.isPresent());
        return noteResponseDto;
    }

    // Note ์๋ฐ์ดํธ
    @Transactional
    public NoteUpdateResponseDto updateNoteDetail(Long noteId, NoteUpdateRequestDto noteUpdateRequestDto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ApiRequestException("์์? ํ? ๋ธํธ๊ฐ ์์ต๋๋ค."));

        note.update(noteUpdateRequestDto, pandanUtils.changeType(noteUpdateRequestDto.getDeadline()), Step.valueOf(noteUpdateRequestDto.getStep()));

        return NoteUpdateResponseDto.of(note);
    }

    // Note ์์ฑ
    @Transactional
    public NoteCreateResponseDto createNote(Long projectId, NoteCreateRequestDto noteCreateRequestDto, SessionUser sessionUser) {

        Optional<UserProjectMapping> userProjectMapping =
                userProjectMappingRepository
                        .findByUserIdAndProjectId(sessionUser.getUserId(), projectId);

        if(!userProjectMapping.isPresent()){
            throw new ApiRequestException("ํด๋น ์?์?๊ฐ ํด๋น ํ๋ก์?ํธ์ ์ฐธ์ฌํด์์ง ์์ต๋๋ค.");
        }

        // [๋ธํธ ์์ฑ] ์?๋ฌ๋ฐ์ String deadline์ LocalDate ์๋ฃํ์ผ๋ก ํ๋ณํ
        LocalDate deadline = pandanUtils.changeType(noteCreateRequestDto.getDeadline());

        // [๋ธํธ ์์ฑ] ์?๋ฌ๋ฐ์ String step์ Enum Step์ผ๋ก
        Step step = Step.valueOf(noteCreateRequestDto.getStep());

        // [๋ธํธ ์์ฑ] ์ฐพ์ userProjectMappingRepository๋ฅผ ํตํด user์ ํ๋ก์?ํธ ๊ฐ์?ธ์ค๊ธฐ
        User user = userProjectMapping.get().getUser();
        Project project = userProjectMapping.get().getProject();

        // [๋ธํธ ์์ฑ] ์?๋ฌ๋ฐ์ noteCreateRequestDto๋ฅผ Note.java์ ์?์ํ of ๋ฉ์๋์ ์?๋ฌํ์ฌ ๋น๋ ํจํด์ ๋ฃ๋๋ค.
        Note note = noteRepository.save(Note.of(noteCreateRequestDto, deadline, step, user, project));
        return NoteCreateResponseDto.of(note);
    }

    // ํด๋น Project ์์ ๋ด๊ฐ ์์ฑํ Note ์กฐํ
    public NoteMineInProjectResponseDto readNotesMineOnly(Long projectId, SessionUser sessionUser) {

        // Project ์กฐํ
        projectRepository.findById(projectId).orElseThrow(
                () -> new ApiRequestException("๋ด๊ฐ ์์ฑํ ๋ฌธ์๋ฅผ ์กฐํํ? ํ๋ก์?ํธ๊ฐ ์์ต๋๋ค.")
        );

        // ํด๋น Project ์์ ๋ด๊ฐ ์์ฑํ Note ์ฃํ
        List<NoteReadMineEachResponseDto> myNoteList = noteRepository.findAllNoteByProjectAndUserOrderByCreatedAtDesc(projectId, sessionUser.getUserId())
                .stream()
                .map(NoteReadMineEachResponseDto::fromEntity)
                .collect(Collectors.toList());

        return NoteMineInProjectResponseDto.of(myNoteList);
    }

    // ์?์ฒด Project ์์ ๋ด๊ฐ ๋ถ๋งํฌํ Note ์กฐํ
    public NoteBookmarkedResponseDto readBookmarkedMine(SessionUser sessionUser) {

        // ํด๋น ๋ถ๋งํฌํ Note ์กฐํ
        List<NoteEachBookmarkedResponseDto> noteEachBookmarkedResponseDto =
                bookmarkRepository.findNoteByUserIdInBookmark(sessionUser.getUserId());

        return NoteBookmarkedResponseDto.builder().noteList(noteEachBookmarkedResponseDto).build();
    }

    // Note ์ญ์?
    @Transactional
    public NoteDeleteResponseDto deleteNote(Long noteId) {
        // ์ญ์?ํ? Note ์กฐํ
        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("์ด๋ฏธ ์ญ์?๋ ๋ธํธ์๋๋ค.")
        );

        // Note์ ์ฐ๊ด๋  ์ฝ๋ฉํธ ์ญ์?
        commentRepository.deleteCommentByNoteId(noteId);

        // Note ์ ์ฐ๊ด๋ ๋ถ๋งํฌ ์ญ์?
        bookmarkRepository.deleteByNote(noteId);

        // Note ์ญ์?
        noteRepository.delete(note);

        return NoteDeleteResponseDto.builder()
                .noteId(noteId)
                .build();
    }

    // Note ์นธ๋ฐํ ์กฐํ (์นธ๋ฐ ํ์ด์ง)
    @Transactional
    public KanbanNoteSearchResponseDto readKanbanNote(Long projectId) {
        List<NoteOfProjectResponseDto> noteOfProjectResponseDtoList = new ArrayList<>();
        List<KanbanNoteEachResponseDto> kanbanNoteEachResponseDtoList1 = new ArrayList<>();
        List<KanbanNoteEachResponseDto> kanbanNoteEachResponseDtoList2 = new ArrayList<>();
        List<KanbanNoteEachResponseDto> kanbanNoteEachResponseDtoList3 = new ArrayList<>();
        List<KanbanNoteEachResponseDto> kanbanNoteEachResponseDtoList4 = new ArrayList<>();

        // Project ์กฐํ
        Project project = projectRepository.findById(projectId).orElseThrow(
                ()-> new ApiRequestException("์นธ๋ฐ์ ์กฐํํ? ํ๋ก์?ํธ๊ฐ ์์ต๋๋ค.")
        );

        for (Note note : noteRepository.findByProject(project)) {
            switch(note.getStep()){
                case STORAGE:
                    kanbanNoteEachResponseDtoList1.add((KanbanNoteEachResponseDto.of(note))); break;
                case TODO:
                    kanbanNoteEachResponseDtoList2.add((KanbanNoteEachResponseDto.of(note))); break;
                case PROCESSING:
                    kanbanNoteEachResponseDtoList3.add((KanbanNoteEachResponseDto.of(note))); break;
                case DONE:
                    kanbanNoteEachResponseDtoList4.add(KanbanNoteEachResponseDto.of(note)); break;
            }
        }
        // Note ๋ฅผ ๊ฐ ์ํ๋ณ๋ก List ๋ก ๋ฌถ์ด์ ์๋ต ๋ณด๋ด๊ธฐ
        noteOfProjectResponseDtoList.add(NoteOfProjectResponseDto.of(Step.STORAGE, kanbanNoteEachResponseDtoList1));
        noteOfProjectResponseDtoList.add(NoteOfProjectResponseDto.of(Step.TODO, kanbanNoteEachResponseDtoList2));
        noteOfProjectResponseDtoList.add(NoteOfProjectResponseDto.of(Step.PROCESSING, kanbanNoteEachResponseDtoList3));
        noteOfProjectResponseDtoList.add(NoteOfProjectResponseDto.of(Step.DONE, kanbanNoteEachResponseDtoList4));

        return KanbanNoteSearchResponseDto.builder()
                .noteOfProjectResponseDtoList(noteOfProjectResponseDtoList)
                .build();
    }

    // Note ์ผ๋ฐํ ์กฐํ (ํ์ผ ํ์ด์ง)
    @Transactional
    public NoteSearchResponseDto readOrdinaryNote(Long projectId) {
        List<OrdinaryNoteEachResponseDto> ordinaryNoteEachResponseDtoList = new ArrayList<>();

        // Project ์กฐํ
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ApiRequestException("ํ์ผ์ ์กฐํํ? ํ๋ก์?ํธ๊ฐ ์์ต๋๋ค.")
        );


        for (Note note : noteRepository.findAllByProjectOrderByCreatedAtDesc(project)) {
            ordinaryNoteEachResponseDtoList.add((OrdinaryNoteEachResponseDto.fromEntity(note)));
        }

        return NoteSearchResponseDto.of(ordinaryNoteEachResponseDtoList);
    }

    // ์?์ฒด ํ๋ก์?ํธ์์ ๋ด๊ฐ ์์ฑํ ๋ธํธ ์กฐํ
    public NoteMineInTotalResponseDto readMyNoteInTotalProject(SessionUser sessionUser) {
        List<NoteEachMineInTotalResponseDto> resultList = noteRepository.findUserNoteInTotalProject(sessionUser.getUserId());
        return NoteMineInTotalResponseDto.builder().myNoteList(resultList).build();
    }

    public NoteSearchInTotalResponseDto searchNoteInMyProjects(SessionUser sessionUser, String rawKeyword){
        List<String> keywordList = pandanUtils.parseKeywordToList(rawKeyword);
        List<noteEachSearchInTotalResponseDto> resultList = noteRepository.findNotesByUserIdAndKeywordInTotal(sessionUser.getUserId(), keywordList);
        return NoteSearchInTotalResponseDto.builder().noteList(resultList).build();
    }

    public NoteSearchInMineResponseDto searchNoteInMyNotes(SessionUser sessionUser, String rawKeyword){
        List<String> keywordList = pandanUtils.parseKeywordToList(rawKeyword);
        List<NoteEachSearchInMineResponseDto> resultList = noteRepository.findNotesByUserIdAndKeywordInMine(sessionUser.getUserId(), keywordList);
        return NoteSearchInMineResponseDto.builder().noteList(resultList).build();
    }


}
