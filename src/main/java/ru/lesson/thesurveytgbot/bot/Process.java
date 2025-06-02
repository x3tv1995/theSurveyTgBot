package ru.lesson.thesurveytgbot.bot;

import ru.lesson.thesurveytgbot.bot.enums.UserState;
import ru.lesson.thesurveytgbot.entity.User;

import java.util.Map;

public interface Process {
    void processAwaitingName(long chatId, String getMessage, User user, Map<Long, UserState> userStates, Bot bot);
    void processAwaitingEmail(long chatId, String getMessage, User user,Map<Long, UserState> userStates,Bot bot);
    void processAwaitingRating(long chatId, String getMessage, User user,Map<Long, UserState> userStates,Bot bot);
}
