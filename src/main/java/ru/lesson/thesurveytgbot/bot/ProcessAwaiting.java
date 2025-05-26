package ru.lesson.thesurveytgbot.bot;

import org.springframework.stereotype.Component;
import ru.lesson.thesurveytgbot.entity.User;
import ru.lesson.thesurveytgbot.repository.UserRepository;
import java.util.Map;

@Component
public class ProcessAwaiting {
    private final UserRepository userRepository;


    public ProcessAwaiting(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void processAwaitingName(long chatId, String getMessage, User user, Map<Long, UserState> userStates,Bot bot) {
        user.setFirstName(getMessage);
        if (bot.validateUserProperty(user, "firstName")) {
            bot.message(chatId, "Принято");
            bot.message(chatId, "Введите ваше почту");
            userStates.put(chatId, UserState.AWAITING_EMAIL);
            userRepository.save(user);
        } else {
            bot.message(chatId, "Попробуйте ещё раз ввести  " + "имя");
        }

    }

    public void processAwaitingEmail(long chatId, String getMessage, User user,Map<Long, UserState> userStates,Bot bot) {
        user.setEmail(getMessage);
        if (bot.validateUserProperty(user, "email")) {
            bot.message(chatId, "Принято");
            bot.message(chatId, "Введите ваше оценку от 1 до 10");
            userStates.put(chatId, UserState.AWAITING_RATING);
            userRepository.save(user);
        } else {
            bot.message(chatId, "Попробуйте ещё раз ввести  " + "email");
        }
    }

    public void processAwaitingRating(long chatId, String getMessage, User user,Map<Long, UserState> userStates,Bot bot) {
        try {
            user.setRating(Integer.parseInt(getMessage));
            if (bot.validateUserProperty(user, "rating")) {
                bot.message(chatId, "Принято");
                userStates.remove(chatId);
                userRepository.save(user);
            } else {
                bot.message(chatId, "Попробуйте ещё раз ввести  " + "оценку");
            }
        } catch (NumberFormatException e) {
            bot.message(chatId, "Попробуйте ещё раз ввести  " + "оценку");
        }


    }
}
