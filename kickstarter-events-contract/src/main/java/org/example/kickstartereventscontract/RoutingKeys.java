package org.example.kickstartereventscontract;

public final class RoutingKeys {

    // Имя общего topic exchange для доменных событий
    public static final String EXCHANGE = "kickstarter.events";

    // Routing keys для событий проектов
    public static final String PROJECT_CREATED = "project.created";
    public static final String PROJECT_UPDATED = "project.updated";
    public static final String PROJECT_DELETED = "project.deleted";

    // События взносов
    public static final String PLEDGE_CREATED = "pledge.created";

    // События наград
    public static final String REWARD_CREATED = "reward.created";

    // События пользователей
    public static final String USER_CREATED = "user.created";

    // Паттерны для подписки (wildcard)
    public static final String ALL_PROJECT_EVENTS = "project.*";
    public static final String ALL_PLEDGE_EVENTS = "pledge.*";
    public static final String ALL_EVENTS = "#";
}