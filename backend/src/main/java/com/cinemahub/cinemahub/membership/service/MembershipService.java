package com.cinemahub.cinemahub.membership.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.membership.entity.Membership;
import com.cinemahub.cinemahub.membership.entity.MembershipLevel;
import com.cinemahub.cinemahub.membership.entity.PointHistory;
import com.cinemahub.cinemahub.membership.repository.MembershipRepository;
import com.cinemahub.cinemahub.membership.repository.PointHistoryRepository;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    public MembershipService(MembershipRepository membershipRepository,
                              PointHistoryRepository pointHistoryRepository,
                              UserRepository userRepository) {
        this.membershipRepository = membershipRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.userRepository = userRepository;
    }

    public Membership findById(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Membership", id));
    }

    public Membership findByUser(Long userId) {
        return membershipRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario %d no tiene membresía".formatted(userId)));
    }

    @Transactional
    public Membership createForUser(Long userId) {
        membershipRepository.findByUserId(userId).ifPresent(existing -> {
            throw DuplicateResourceException.of("una membresía", "userId", String.valueOf(userId));
        });
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return membershipRepository.save(new Membership(user));
    }

    /**
     * Suma (o resta, si delta es negativo) puntos a la membresía y deja constancia en
     * point_history. No permite que el saldo quede negativo (CHECK points >= 0 en BD).
     */
    @Transactional
    public Membership adjustPoints(Long membershipId, int delta, String reason) {
        if (delta == 0) {
            throw new IllegalArgumentException("delta no puede ser cero");
        }
        Membership membership = findById(membershipId);
        int newBalance = membership.getPoints() + delta;
        if (newBalance < 0) {
            throw new IllegalStateException("La membresía no tiene puntos suficientes");
        }
        membership.setPoints(newBalance);

        PointHistory history = new PointHistory(membership, delta);
        history.setReason(reason);
        pointHistoryRepository.save(history);

        return membership;
    }

    @Transactional
    public Membership changeLevel(Long membershipId, MembershipLevel level) {
        Membership membership = findById(membershipId);
        membership.setLevel(level);
        return membership;
    }

    public List<PointHistory> findHistory(Long membershipId) {
        findById(membershipId); // valida que exista
        return pointHistoryRepository.findByMembershipId(membershipId);
    }
}