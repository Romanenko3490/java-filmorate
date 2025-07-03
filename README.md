# ER Diagram для Filmorate Database

## Описание структуры базы данных

### Основные сущности

#### 1. Пользователи (`users`)

| Поле       | Тип       | Ограничения               | Описание                |
|------------|-----------|---------------------------|-------------------------|
| user_id    | INTEGER   | PRIMARY KEY               | Уникальный идентификатор|
| email      | VARCHAR   | NOT NULL, UNIQUE          | Электронная почта       |
| login      | VARCHAR   | NOT NULL                  | Логин пользователя      |
| name       | VARCHAR   |                           | Отображаемое имя        |
| birthday   | DATE      |                           | Дата рождения           |

#### 2. Фильмы (`film`)

| Поле          | Тип       | Ограничения               | Описание                |
|---------------|-----------|---------------------------|-------------------------|
| film_id       | INTEGER   | PRIMARY KEY               | Уникальный идентификатор|
| name          | VARCHAR   | NOT NULL                  | Название фильма         |
| description   | VARCHAR   | MAX(200)                  | Описание фильма         |
| release_date  | DATE      | NOT NULL                  | Дата выхода             |
| duration      | INTEGER   | NOT NULL, >0              | Продолжительность (мин) |
| mpa_rating_id | INTEGER   | FOREIGN KEY               | Ссылка на возрастной рейтинг |

### Справочные таблицы

#### 3. Жанры (`genre`)

| Поле       | Тип      | Ограничения               | Описание         |
|------------|----------|---------------------------|------------------|
| genre_id   | INTEGER  | PRIMARY KEY               | ID жанра         |
| name       | VARCHAR  | NOT NULL, UNIQUE          | Название жанра   |

#### 4. Возрастные рейтинги (`mpa_rating`)

| Поле         | Тип      | Ограничения               | Описание               |
|--------------|----------|---------------------------|------------------------|
| mpa_id       | INTEGER  | PRIMARY KEY               | ID рейтинга            |
| code         | VARCHAR  | NOT NULL, UNIQUE          | Код рейтинга (G, PG-13)|
| description  | VARCHAR  |                           | Описание рейтинга      |

### Таблицы связей

#### 5. Дружба (`friendship`)

| Поле         | Тип       | Ограничения               | Описание                |
|--------------|-----------|---------------------------|-------------------------|
| user_id      | INTEGER   | PRIMARY KEY, FOREIGN KEY  | ID пользователя         |
| friend_id    | INTEGER   | PRIMARY KEY, FOREIGN KEY  | ID друга                |
| status       | ENUM      | 'PENDING','CONFIRMED'     | Статус дружбы           |
| created_date | DATE      | DEFAULT CURRENT_DATE      | Дата создания           |

#### 6. Связь фильмов и жанров (`film_genre`)

| Поле     | Тип      | Ограничения              | Описание         |
|----------|----------|--------------------------|------------------|
| film_id  | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID фильма        |
| genre_id | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID жанра         |

#### 7. Лайки фильмов (`film_likes`)
e
| Поле     | Тип      | Ограничения              | Описание         |
|----------|----------|--------------------------|------------------|
| film_id  | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID фильма        |
| user_id  | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID пользователя  |


## ER-диаграмма базы данных Filmorate

```mermaid
erDiagram
    USER ||--o{ FRIENDSHIP : "имеет"
    USER ||--o{ FILM_LIKE : "ставит"
    FILM ||--o{ FILM_GENRE : "имеет"
    FILM ||--o{ FILM_LIKE : "получает"
    FILM }|--|| MPA_RATING : "рейтинг"
    FILM_GENRE }|--|| GENRE : "жанр"

    USER {
        int id PK
        string email
        string login
        string name
        date birthday
    }
    
    FILM {
        int id PK
        string name
        string description
        date release_date
        int duration
        int mpa_rating_id FK
    }
    
    GENRE {
        int id PK
        string name
    }
    
    MPA_RATING {
        int id PK
        string code
        string description
    }
    
    FRIENDSHIP {
        int user_id PK,FK
        int friend_id PK,FK
        string status
        date created_date
    }
    
    FILM_GENRE {
        int film_id PK,FK
        int genre_id PK,FK
    }
    
    FILM_LIKE {
        int film_id PK,FK
        int user_id PK,FK
    }


