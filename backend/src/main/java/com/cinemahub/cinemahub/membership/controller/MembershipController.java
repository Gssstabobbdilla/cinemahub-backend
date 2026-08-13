package com.cinemahub.cinemahub.membership.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.membership.dto.AdjustPointsRequest;
import com.cinemahub.cinemahub.membership.dto.ChangeLevelRequest;
import com.cinemahub.cinemahub.membership.dto.MembershipResponse;
import com.cinemahub.cinemahub.membership.dto.PointHistoryResponse;
import com.cinemahub.cinemahub.membership.entity.Membership;
import com.cinemahub.cinemahub.membership.service.MembershipService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping("/api/users/{userId}/membership")
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse createForUser(@PathVariable Long userId) {
        Membership membership = membershipService.createForUser(userId);
        return MembershipResponse.from(membership);
    }

    @GetMapping("/api/users/{userId}/membership")
    public MembershipResponse findByUser(@PathVariable Long userId) {
        return MembershipResponse.from(membershipService.findByUser(userId));
    }

    @GetMapping("/api/memberships/{id}")
    public MembershipResponse findById(@PathVariable Long id) {
        return MembershipResponse.from(membershipService.findById(id));
    }

    @PostMapping("/api/memberships/{id}/points")
    public MembershipResponse adjustPoints(@PathVariable Long id, @Valid @RequestBody AdjustPointsRequest request) {
        Membership membership = membershipService.adjustPoints(id, request.delta(), request.reason());
        return MembershipResponse.from(membership);
    }

    @PatchMapping("/api/memberships/{id}/level")
    public MembershipResponse changeLevel(@PathVariable Long id, @Valid @RequestBody ChangeLevelRequest request) {
        Membership membership = membershipService.changeLevel(id, request.level());
        return MembershipResponse.from(membership);
    }

    @GetMapping("/api/memberships/{id}/history")
    public List<PointHistoryResponse> findHistory(@PathVariable Long id) {
        return membershipService.findHistory(id).stream().map(PointHistoryResponse::from).toList();
    }
}