package org.example.kickstarterapicontract.dto;

public enum PledgeStatus {
    AUTHORIZED, // Деньги заморожены (Взнос только что создан)
    CAPTURED,   // Деньги успешно списаны (Проект собрал сумму)
    VOIDED,     // Заморозка снята (Проект провалился)
    CANCELED    // Пользователь сам отменил взнос
}