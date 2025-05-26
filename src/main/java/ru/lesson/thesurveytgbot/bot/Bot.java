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

    @Autowired
    private  ProcessAwaiting processAwaiting;

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
        if(getMessage.contains("/start")||getMessage.contains("/report")) {
            userStates.remove(chatId);
        }

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
            processAwaiting.processAwaitingName(chatId, getMessage, user,userStates,this);
        } else if (userStates.get(chatId) != null && userStates.get(chatId).equals(UserState.AWAITING_EMAIL)) {
            processAwaiting.processAwaitingEmail(chatId, getMessage, user,userStates,this);
        } else if (userStates.get(chatId) != null && userStates.get(chatId).equals(UserState.AWAITING_RATING)) {
            processAwaiting.processAwaitingRating(chatId, getMessage, user,userStates,this);

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




}
