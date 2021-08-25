package com.example.teampandanback.service;

import com.example.teampandanback.domain.project.Project;
import com.example.teampandanback.domain.project.ProjectRepository;
import com.example.teampandanback.dto.project.request.ProjectRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Test
    @DisplayName("프로젝트 만들기")
    public void createProject(){
        // given
        ProjectRequestDto requestDto = ProjectRequestDto.builder()
                                            .title("프로젝트 제목")
                                            .detail("프로젝트 설명")
                                            .build();
        Project project = Project.toEntity(requestDto);

        ProjectRequestDto requestDto2 = ProjectRequestDto.builder()
                .title("프로젝트 제목2")
                .detail("프로젝트 설명2")
                .build();
        Project project2 = Project.toEntity(requestDto2);

        // when
        assertThat(project.getTitle()).isEqualTo(requestDto.getTitle());
        //assertThat(project2.getTitle()).isEqualTo(requestDto.getTitle());

    }
}