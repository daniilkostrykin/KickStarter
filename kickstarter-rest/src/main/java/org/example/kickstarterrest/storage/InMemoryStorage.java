package org.example.kickstarterrest.storage;

import jakarta.annotation.PostConstruct;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstarterapicontract.dto.PledgeStatus;
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
    public final Map<Long, UserResponse> users = new ConcurrentHashMap<>();

    public final AtomicLong userSequence = new AtomicLong(0);
    public final AtomicLong projectSequence = new AtomicLong(0);
    public final AtomicLong rewardSequence = new AtomicLong(0);
    public final AtomicLong pledgeSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {
        // Пользователи
        long uId1 = userSequence.incrementAndGet();
        users.put(uId1, UserResponse.builder()
                .id(uId1).username("admin_creator").email("admin@kickstarter.local").build());

        long uId2 = userSequence.incrementAndGet();
        users.put(uId2, UserResponse.builder()
                .id(uId2).username("investor_pro").email("investor@mail.ru").build());

        long uId3 = userSequence.incrementAndGet();
        users.put(uId3, UserResponse.builder()
                .id(uId3).username("tech_geek").email("geek@gmail.com").build());


        //  Проект 1: Активный 
        long pId1 = projectSequence.incrementAndGet();
        projects.put(pId1, ProjectResponse.builder()
                .id(pId1).title("Умный рюкзак с солнечной батареей")
                .description("Рюкзак со встроенным powerbank и защитой от краж для студентов.")
                .goal(new BigDecimal("500000")).pledged(new BigDecimal("15000"))
                .authorId(uId1)
                .status("ACTIVE").deadline(OffsetDateTime.now().plusDays(30)).build());

        long rId1 = rewardSequence.incrementAndGet();
        rewards.put(rId1, RewardResponse.builder()
                .id(rId1).title("Ранняя пташка").description("Один рюкзак со скидкой 50%")
                .minPrice(new BigDecimal("5000")).projectId(pId1).build());

        long rId2 = rewardSequence.incrementAndGet();
        rewards.put(rId2, RewardResponse.builder()
                .id(rId2).title("Комбо набор").description("Рюкзак + термокружка")
                .minPrice(new BigDecimal("7500")).projectId(pId1).build());


        //  Проект 2: Успешный
        long pId2 = projectSequence.incrementAndGet();
        projects.put(pId2, ProjectResponse.builder()
                .id(pId2).title("Инди-игра 'Cyber Dungeon'")
                .description("Пиксельная RPG с открытым миром и сложными боссами.")
                .goal(new BigDecimal("100000")).pledged(new BigDecimal("125000"))
                .authorId(uId3)
                .status("SUCCESSFUL").deadline(OffsetDateTime.now().minusDays(2)).build());

        long rId3 = rewardSequence.incrementAndGet();
        rewards.put(rId3, RewardResponse.builder()
                .id(rId3).title("Цифровая копия").description("Ключ в Steam")
                .minPrice(new BigDecimal("500")).projectId(pId2).build());


        //  Проект 3: Черновик
        long pId3 = projectSequence.incrementAndGet();
        projects.put(pId3, ProjectResponse.builder()
                .id(pId3).title("Эко-кроссовки из пластика")
                .description("Стильная обувь, спасающая экологию.")
                .goal(new BigDecimal("300000")).pledged(new BigDecimal("0"))
                .authorId(uId2)
                .status("DRAFT").deadline(OffsetDateTime.now().plusDays(60)).build());


        //  Взносы

        // Спонсор 2 донатит в проект 1 (Рюкзак)
        long plId1 = pledgeSequence.incrementAndGet();
        pledges.put(plId1, PledgeResponse.builder()
                .pledgeId(plId1).projectId(pId1).rewardId(rId1).userId(uId2)
                .amount(new BigDecimal("5000"))
                .status(PledgeStatus.CAPTURED)
                .transactionDate(OffsetDateTime.now().minusDays(5)).build());

        // Спонсор 3 донатит в проект 1 (Рюкзак)
        long plId2 = pledgeSequence.incrementAndGet();
        pledges.put(plId2, PledgeResponse.builder()
                .pledgeId(plId2).projectId(pId1).rewardId(rId2).userId(uId3)
                .amount(new BigDecimal("10000"))
                .status(PledgeStatus.CAPTURED)
                .transactionDate(OffsetDateTime.now().minusDays(2)).build());
    }
}