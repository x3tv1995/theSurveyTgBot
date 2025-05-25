package ru.lesson.thesurveytgbot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Size(min = 3, max = 50, message = "От 3 до 20 символов")
    private String firstName;


    @Email(message = "неверно введена почта")
    @Size(min = 3, max = 50, message = "От 3 до 20 символов")
    private String email;


    @Min(1)
    @Max(10)
    private Integer rating;

    @Column(name = "chat_id")
    private Long chatId;

}
