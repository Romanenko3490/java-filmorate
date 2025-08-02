document.addEventListener('DOMContentLoaded', function() {
    const API_BASE_URL = '';

    // Навигация
    document.querySelectorAll('nav a').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const pageId = this.getAttribute('data-page');
            showPage(pageId);
        });
    });

    // Обработчики для фильмов
    document.getElementById('get-films').addEventListener('click', fetchFilms);
    document.getElementById('get-popular').addEventListener('click', fetchPopularFilms);
    document.getElementById('film-form').addEventListener('submit', handleFilmSubmit);

    // Обработчики для пользователей
    document.getElementById('get-users').addEventListener('click', fetchUsers);
    document.getElementById('user-form').addEventListener('submit', handleUserSubmit);

    // Загружаем начальные данные
    showPage('films');
    fetchFilms();
    fetchGenres();
    fetchMpaRatings();
});

function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    document.getElementById(pageId).classList.add('active');
}

// Функции для работы с фильмами
async function fetchFilms() {
    try {
        const response = await fetch(`${API_BASE_URL}/films`);
        const films = await response.json();
        renderFilms(films);
    } catch (error) {
        console.error('Ошибка при загрузке фильмов:', error);
    }
}

async function fetchPopularFilms() {
    try {
        const response = await fetch(`${API_BASE_URL}/films/popular?count=10`);
        const films = await response.json();
        renderFilms(films);
    } catch (error) {
        console.error('Ошибка при загрузке популярных фильмов:', error);
    }
}

function renderFilms(films) {
    const filmsList = document.getElementById('films-list');
    filmsList.innerHTML = '';

    films.forEach(film => {
        const filmCard = document.createElement('div');
        filmCard.className = 'item-card';
        filmCard.innerHTML = `
            <h3>${film.name}</h3>
            <p>${film.description || 'Нет описания'}</p>
            <p>Дата выхода: ${film.releaseDate}</p>
            <p>Длительность: ${film.duration} мин</p>
            <div class="actions">
                <button onclick="editFilm(${film.id})">Редактировать</button>
                <button onclick="deleteFilm(${film.id})">Удалить</button>
            </div>
        `;
        filmsList.appendChild(filmCard);
    });
}

async function handleFilmSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const filmId = form.querySelector('#film-id').value;
    const filmData = {
        name: form.querySelector('#film-name').value,
        description: form.querySelector('#film-description').value,
        releaseDate: form.querySelector('#film-release-date').value,
        duration: parseInt(form.querySelector('#film-duration').value),
    };

    try {
        let response;
        if (filmId) {
            // Обновление существующего фильма
            filmData.id = filmId;
            response = await fetch(`${API_BASE_URL}/films`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(filmData),
            });
        } else {
            // Создание нового фильма
            response = await fetch(`${API_BASE_URL}/films`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(filmData),
            });
        }

        if (response.ok) {
            form.reset();
            fetchFilms();
        } else {
            console.error('Ошибка при сохранении фильма');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Функции для работы с пользователями
async function fetchUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/users`);
        const users = await response.json();
        renderUsers(users);
    } catch (error) {
        console.error('Ошибка при загрузке пользователей:', error);
    }
}

function renderUsers(users) {
    const usersList = document.getElementById('users-list');
    usersList.innerHTML = '';

    users.forEach(user => {
        const userCard = document.createElement('div');
        userCard.className = 'item-card';
        userCard.innerHTML = `
            <h3>${user.name || user.login}</h3>
            <p>Email: ${user.email}</p>
            <p>Логин: ${user.login}</p>
            <p>День рождения: ${user.birthday || 'Не указан'}</p>
            <div class="actions">
                <button onclick="editUser(${user.id})">Редактировать</button>
                <button onclick="deleteUser(${user.id})">Удалить</button>
            </div>
        `;
        usersList.appendChild(userCard);
    });
}

async function handleUserSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const userId = form.querySelector('#user-id').value;
    const userData = {
        email: form.querySelector('#user-email').value,
        login: form.querySelector('#user-login').value,
        name: form.querySelector('#user-name').value || form.querySelector('#user-login').value,
        birthday: form.querySelector('#user-birthday').value,
    };

    try {
        let response;
        if (userId) {
            // Обновление существующего пользователя
            userData.id = userId;
            response = await fetch(`${API_BASE_URL}/users`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });
        } else {
            // Создание нового пользователя
            response = await fetch(`${API_BASE_URL}/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });
        }

        if (response.ok) {
            form.reset();
            fetchUsers();
        } else {
            console.error('Ошибка при сохранении пользователя');
        }
    } catch (error) {
        console.error('Ошибка:', error);
    }
}

// Функции для работы с жанрами и рейтингами
async function fetchGenres() {
    try {
        const response = await fetch(`${API_BASE_URL}/genres`);
        const genres = await response.json();
        renderGenres(genres);
    } catch (error) {
        console.error('Ошибка при загрузке жанров:', error);
    }
}

function renderGenres(genres) {
    const genresList = document.getElementById('genres-list');
    genresList.innerHTML = '';

    genres.forEach(genre => {
        const genreCard = document.createElement('div');
        genreCard.className = 'item-card';
        genreCard.innerHTML = `
            <h3>${genre.name}</h3>
            <p>ID: ${genre.id}</p>
        `;
        genresList.appendChild(genreCard);
    });
}

async function fetchMpaRatings() {
    try {
        const response = await fetch(`${API_BASE_URL}/mpa`);
        const mpaRatings = await response.json();
        renderMpaRatings(mpaRatings);
    } catch (error) {
        console.error('Ошибка при загрузке рейтингов:', error);
    }
}

function renderMpaRatings(mpaRatings) {
    const mpaList = document.getElementById('mpa-list');
    mpaList.innerHTML = '';

    mpaRatings.forEach(mpa => {
        const mpaCard = document.createElement('div');
        mpaCard.className = 'item-card';
        mpaCard.innerHTML = `
            <h3>${mpa.name}</h3>
            <p>ID: ${mpa.id}</p>
        `;
        mpaList.appendChild(mpaCard);
    });
}

// Вспомогательные функции
function editFilm(id) {
    // Реализация заполнения формы для редактирования фильма
    console.log('Редактирование фильма с ID:', id);
}

function deleteFilm(id) {
    if (confirm('Вы уверены, что хотите удалить этот фильм?')) {
        fetch(`${API_BASE_URL}/films/${id}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    fetchFilms();
                }
            })
            .catch(error => console.error('Ошибка:', error));
    }
}

function editUser(id) {
    // Реализация заполнения формы для редактирования пользователя
    console.log('Редактирование пользователя с ID:', id);
}

function deleteUser(id) {
    if (confirm('Вы уверены, что хотите удалить этого пользователя?')) {
        fetch(`${API_BASE_URL}/users/${id}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    fetchUsers();
                }
            })
            .catch(error => console.error('Ошибка:', error));
    }
}