package com.menu.demo.Controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;
import com.menu.demo.Services.ClassroomService;

import Dto.ClassRoomRequestDto;
import Dto.ClassRoomResponsDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassRoomController {

    private final ClassroomService classroomService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    @PostMapping
    public ResponseEntity<ClassRoomResponsDto> create(
            @RequestBody ClassRoomRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        return classroomService.createClassRoom(request, resolveAdmin(currentUser));
    }

    @GetMapping
    public ResponseEntity<List<ClassRoomResponsDto>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(classroomService.getAllClassRooms(resolveAdmin(currentUser)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassRoomResponsDto> update(
            @PathVariable Long id,
            @RequestBody ClassRoomRequestDto dto,
            @AuthenticationPrincipal User currentUser) {
        return classroomService.updateClassroom(id, resolveAdmin(currentUser), dto);
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
}
