package org.example.kickstarterrest.storage;

import jakarta.annotation.PostConstruct;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {
    public final Map<Long, ProjectResponse> projects = new ConcurrentHashMap<>();
    public final Map<Long, RewardResponse> rewards = new ConcurrentHashMap<>();
    public final Map<Long, PledgeResponse> pledges = new ConcurrentHashMap<>();
    public final Map<Long, UserResponse> users = new java.util.concurrent.ConcurrentHashMap<>();
    public final java.util.concurrent.atomic.AtomicLong userSequence = new java.util.concurrent.atomic.AtomicLong(0);

    public final AtomicLong projectSequence = new AtomicLong(0);
    public final AtomicLong rewardSequence = new AtomicLong(0);
    public final AtomicLong pledgeSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {
        long uId = userSequence.incrementAndGet();
        users.put(uId, UserResponse.builder()
                .id(uId).username("admin_creator").email("admin@kickstarter.local").build());

        long pId = projectSequence.incrementAndGet();
        projects.put(pId, ProjectResponse.builder()
                .id(pId).title("Умный рюкзак").description("Рюкзак со встроенным powerbank")
                .goal(new BigDecimal("500000")).pledged(new BigDecimal("0"))
                .authorId(uId)
                .status("ACTIVE").deadline(OffsetDateTime.now().plusDays(30)).build());

        System.out.println("ПРИ СТАРТЕ СЕРВЕРА: Рюкзак ID=" + pId + " получил Автора ID=" + projects.get(pId).getAuthorId());

        long rId = rewardSequence.incrementAndGet();
        rewards.put(rId, RewardResponse.builder()
                .id(rId).title("Ранняя пташка").description("Один рюкзак со скидкой")
                .minPrice(new BigDecimal("5000")).projectId(pId).build());
    }
}