package ru.lesson.thesurveytgbot.bot;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lesson.thesurveytgbot.bot.enums.FunctionEnum;
import ru.lesson.thesurveytgbot.bot.enums.UserState;
import ru.lesson.thesurveytgbot.bot.service.Document;
import ru.lesson.thesurveytgbot.bot.service.ProcessAwaiting;
import ru.lesson.thesurveytgbot.entity.User;
import ru.lesson.thesurveytgbot.repository.UserRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log4j2
public class Bot extends TelegramLongPollingBot {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Validator validator;
    private final UserRepository userRepository;
    private final List<FunctionEnum> REMOVE_USER_ENUM = Arrays.asList(FunctionEnum.REPORT, FunctionEnum.START);
    @Value("${bot.username}")
    private String botUsername;

    @Autowired
    private Document document;

    @Autowired
    private ProcessAwaiting processAwaiting;

    public Bot(@Value("${bot.token}") String token, UserRepository userRepository) {
        super(token);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.userRepository = userRepository;
        this.validator = factory.getValidator();


    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Update received: {}", update);
        Long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getFrom().getUserName();
        String message = update.getMessage().getText();

        Optional<User> userOptional = userRepository.findByChatId(chatId);
        FunctionEnum function = FunctionEnum.fromValue(message);
        if (REMOVE_USER_ENUM.contains(function)) {
            userStates.remove(chatId);
        } else if (FunctionEnum.REPORT == function) {
            try {
                document.generateWordReport(chatId, this);
            } catch (IOException e) {
                message(chatId, "Ошибка при создании отчета: " + e.getMessage());
                log.error("ChatId = {}Ошибка при создании отчета: ", chatId, e);
            }
        } else if (FunctionEnum.START == function) {
            message(chatId, "Привет, " + name);
        }
        User user = userOptional.orElseGet(User::new);
        user.setChatId(chatId);
        UserState state = userStates.get(chatId);
        if (message.contains("/form")) {
            message(chatId, "Введите ваше имя");
            userStates.put(chatId, UserState.AWAITING_NAME);
        } else if(state != null){
            switch (state){
                case AWAITING_NAME -> processAwaiting.processAwaitingName(chatId, message, user,userStates,this);
                case AWAITING_EMAIL -> processAwaiting.processAwaitingEmail(chatId, message, user,userStates,this);
                case AWAITING_RATING -> processAwaiting.processAwaitingRating(chatId, message, user,userStates,this);
            }
        }
        log.info("Обновление успешно: {}", update);
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
        try {
            execute(new SendMessage(id.toString(), text));
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения пользователю", e);
            throw new RuntimeException(e);
        }
    }




}
