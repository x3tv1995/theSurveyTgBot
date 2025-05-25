package ru.lesson.thesurveytgbot.bot;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lesson.thesurveytgbot.entity.User;
import ru.lesson.thesurveytgbot.repository.UserRepository;

import java.io.IOException;
import java.util.*;

@Component
public class Bot extends TelegramLongPollingBot {
    private final Map<Long, UserState> userStates = new HashMap<>();
    private final Validator validator;
    private final UserRepository userRepository;
    @Value("${bot.username}")
    private String botUsername;

    @Autowired
    private Document document;

    public Bot(@Value("${bot.token}") String token, UserRepository userRepository) {
        super(token);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.userRepository = userRepository;
        this.validator = factory.getValidator();


    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getFrom().getUserName();
        String getMessage = update.getMessage().getText();

        Optional<User> userOptional = userRepository.findByChatId(chatId);


        if (getMessage.contains("/report")) {
            try {

                document.generateWordReport(chatId, this);

            } catch (IOException e) {
                message(chatId, "Ошибка при создании отчета: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (getMessage.contains("/start")) {
            message(chatId, "Привет, " + name);
        }
        User user = userOptional.orElseGet(User::new);
        user.setChatId(chatId);
        if (getMessage.contains("/form")) {
            message(chatId, "Введите ваше имя");
            userStates.put(chatId, UserState.AWAITING_NAME);
        } else if (userStates.get(chatId) != null && userStates.get(chatId).equals(UserState.AWAITING_NAME)) {
            processAwaitingName(chatId, getMessage, user);
        } else if (userStates.get(chatId) != null && userStates.get(chatId).equals(UserState.AWAITING_EMAIL)) {
            processAwaitingEmail(chatId, getMessage, user);
        } else if (userStates.get(chatId) != null && userStates.get(chatId).equals(UserState.AWAITING_RATING)) {
            processAwaitingRating(chatId, getMessage, user);

        }

    }


    public boolean validateUserProperty(User user, String propertyName) {
        Set<ConstraintViolation<User>> violations = validator.validateProperty(user, propertyName);  // Validate a single property
        return violations.isEmpty();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public void message(Long id, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void processAwaitingName(long chatId, String getMessage, User user) {
        user.setFirstName(getMessage);
        if (validateUserProperty(user, "firstName")) {
            message(chatId, "Принято");
            message(chatId, "Введите ваше почту");
            userStates.put(chatId, UserState.AWAITING_EMAIL);
            userRepository.save(user);
        } else {
            message(chatId, "Попробуйте ещё раз ввести  " + "имя");
        }

    }

    public void processAwaitingEmail(long chatId, String getMessage, User user) {
        user.setEmail(getMessage);
        if (validateUserProperty(user, "email")) {
            message(chatId, "Принято");
            message(chatId, "Введите ваше оценку от 1 до 10");
            userStates.put(chatId, UserState.AWAITING_RATING);
            userRepository.save(user);
        } else {
            message(chatId, "Попробуйте ещё раз ввести  " + "email");
        }
    }

    public void processAwaitingRating(long chatId, String getMessage, User user) {
        try {
            user.setRating(Integer.parseInt(getMessage));
            if (validateUserProperty(user, "rating")) {
                message(chatId, "Принято");
                userStates.remove(chatId);
                userRepository.save(user);
            } else {
                message(chatId, "Попробуйте ещё раз ввести  " + "оценку");
            }
        } catch (NumberFormatException e) {
            message(chatId, "Попробуйте ещё раз ввести  " + "оценку");
        }


    }


}
