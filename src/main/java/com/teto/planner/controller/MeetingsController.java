package com.teto.planner.controller;

import com.teto.planner.dto.BusySlotsResponse;
import com.teto.planner.dto.MeetingCreateRequest;
import com.teto.planner.dto.MeetingDto;
import com.teto.planner.dto.MeetingUpdateRequest;
import com.teto.planner.dto.MeetingsPage;
import com.teto.planner.entity.UserEntity;
import com.teto.planner.service.CurrentUserService;
import com.teto.planner.service.MeetingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meetings")
public class MeetingsController {
    private final MeetingService meetingService;
    private final CurrentUserService currentUserService;

    public MeetingsController(MeetingService meetingService, CurrentUserService currentUserService) {
        this.meetingService = meetingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public MeetingsPage listMeetings(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Login", required = false) String login,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "includePending", defaultValue = "false") boolean includePending,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        UserEntity currentUser = currentUserService.resolve(userId, login);
        return meetingService.listMeetings(currentUser, startDate, endDate, includePending, page, size);
    }

    @PostMapping
    public ResponseEntity<MeetingDto> createMeeting(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Login", required = false) String login,
            @Valid @RequestBody MeetingCreateRequest request
    ) {
        UserEntity currentUser = currentUserService.resolve(userId, login);
        MeetingDto created = meetingService.createMeeting(currentUser, request);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{meetingId}")
    public MeetingDto getMeeting(@PathVariable UUID meetingId) {
        return meetingService.getMeeting(meetingId);
    }

    @PatchMapping("/{meetingId}")
    public MeetingDto patchMeeting(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Login", required = false) String login,
            @PathVariable UUID meetingId,
            @Valid @RequestBody MeetingUpdateRequest request
    ) {
        UserEntity currentUser = currentUserService.resolve(userId, login);
        return meetingService.updateMeeting(currentUser, meetingId, request);
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> cancelMeeting(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Login", required = false) String login,
            @PathVariable UUID meetingId
    ) {
        UserEntity currentUser = currentUserService.resolve(userId, login);
        meetingService.cancelMeeting(currentUser, meetingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slots")
    public BusySlotsResponse getSlots(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Login", required = false) String login,
            @RequestParam("meetingDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate meetingDate
    ) {
        UserEntity currentUser = currentUserService.resolve(userId, login);
        return meetingService.getBusySlots(currentUser, meetingDate);
    }

}
