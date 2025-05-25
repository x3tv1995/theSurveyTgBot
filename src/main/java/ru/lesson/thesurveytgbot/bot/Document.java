package ru.lesson.thesurveytgbot.bot;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lesson.thesurveytgbot.entity.User;
import ru.lesson.thesurveytgbot.repository.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class Document {
    private final UserRepository userRepository;


    public Document(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Async
    public void generateWordReport(Long chatId, Bot bot) throws IOException {
        try {
            List<User> users = userRepository.findAll();

            XWPFDocument document = new XWPFDocument();

            XWPFTable table = document.createTable();
            XWPFTableRow headerRow = table.getRow(0);

            headerRow.getCell(0).setText("Имя");
            headerRow.addNewTableCell().setText("Email");
            headerRow.addNewTableCell().setText("Оценка");

            for (User user : users) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(user.getFirstName());
                row.getCell(1).setText(user.getEmail());
                row.getCell(2).setText(String.valueOf(user.getRating()));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            byte[] reportBytes = outputStream.toByteArray();
            document.close();
            sendDocument(chatId, reportBytes, bot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDocument(Long chatId, byte[] documentContent, Bot bot) {
        try {
            File tempFile = File.createTempFile("report", ".docx");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(documentContent);
            }
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(String.valueOf(chatId));
            sendDocument.setDocument(new InputFile(tempFile));
            try {
                bot.execute(sendDocument);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            } finally {
                tempFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
