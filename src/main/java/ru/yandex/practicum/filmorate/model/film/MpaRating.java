package ru.yandex.practicum.filmorate.model.film;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Data
public class MpaRating {
    private int id;
    private String code;
    private String description;

    public MpaRating(String code) {
        Rating rating = Rating.valueOf(code.toUpperCase());
        this.id = rating.getId();
        this.code = code;
        this.description = rating.getDescription();
    }

    public MpaRating() {}
}

//Предполагается, что этот енум будет лежать в бд и потом из бд подгружаться  в будущем
@Getter
enum Rating {
    G(1,"G", "Нет возрастных ограничений"),
    PG(2,"PG", "Детям рекомендуется смотреть с родителями"),
    PG_13(3,"PG-13", "Детям до 13 лет просмотр не желателен"),
    R(4, "R", "Лицам до 17 лет только с взрослым"),
    NC_17(5,"NC-17", "Лицам до 18 лет просмотр запрещён");

    private final int id;
    private final String code;
    private final String description;

    Rating(int id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public static Rating fromCode(String code) {
        for (Rating rating : values()) {
            if (rating.code.equalsIgnoreCase(code)) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Rating code undefined: " + code);
    }

}
