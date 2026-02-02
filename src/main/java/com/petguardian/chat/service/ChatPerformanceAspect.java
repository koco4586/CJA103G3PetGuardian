package com.petguardian.chat.service;

import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataService;
import com.petguardian.orders.model.StoreMemberVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Performance Interceptor for Chat-related requests.
 * 
 * Responsibility:
 * This Aspect intercepts redundant database lookups triggered by external
 * modules
 * (like GlobalModelAdvice) when the request originates from the Chat module.
 * 
 * Instead of letting external components hit the database for member info,
 * it seamlessly redirects the lookup to our high-performance Redis cache.
 * 
 * Non-intrusive design: Zero modifications required to teammate's code.
 */
@Aspect
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatPerformanceAspect {

    private final ChatRoomMetadataService metadataService;
    private final HttpServletRequest request;

    /**
     * Intercepts finds by ID on the StoreMemberRepository.
     * This is the bridge that "hijacks" the DB call and provides a cached result.
     * 
     * @param pjp Join point for the repository call
     * @param id  Member ID being looked up
     * @return Optional<StoreMemberVO> from cache or DB
     * @throws Throwable if proceeding fails
     */
    @Around("execution(* com.petguardian.orders.model.StoreMemberRepository.findById(..)) && args(id)")
    public Object interceptMemberLookup(ProceedingJoinPoint pjp, Integer id) throws Throwable {
        // 1. Context check: Is this request actually coming from the Chat module?
        if (isChatRequest()) {
            if (log.isDebugEnabled()) {
                log.debug("[Performance] Redirecting external member lookup (ID: {}) to Redis cache.", id);
            }

            // 2. Try to fetch from our optimized Redis cache
            MemberProfileDTO profile = metadataService.getMemberProfile(id);
            if (profile != null) {
                // 3. Cache HIT: Wrap as VO and return to caller (Zero SQL!)
                return Optional.of(convertToStoreMember(profile));
            }
        }

        // 4. Cache MISS or Non-Chat request: Fallback to real database lookup
        return pjp.proceed();
    }

    /**
     * Identifies if the current web request is within the Chat scope.
     */
    private boolean isChatRequest() {
        if (request == null)
            return false;
        String uri = request.getRequestURI();
        return uri != null && (uri.startsWith("/chat") || uri.startsWith("/api/chatrooms"));
    }

    /**
     * Translates our internal DTO to the external VO format expected by the caller.
     */
    private StoreMemberVO convertToStoreMember(MemberProfileDTO profile) {
        StoreMemberVO vo = new StoreMemberVO();
        vo.setMemId(profile.getMemberId());
        vo.setMemName(profile.getMemberName());
        vo.setMemImage(profile.getMemberImage());
        // Note: memTel/memAdd omitted as they aren't used in GlobalModelAdvice
        return vo;
    }
}
