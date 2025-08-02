document.addEventListener('DOMContentLoaded', function() {
    const API_BASE_URL = 'http://localhost:8080/api';

    // Инициализация приложения
    initApp();

    function initApp() {
        // Навигация
        setupNavigation();

        // Обработчики для фильмов
        document.getElementById('get-films').addEventListener('click', fetchFilms);
        document.getElementById('get-popular').addEventListener('click', fetchPopularFilms);
        document.getElementById('film-form').addEventListener('submit', handleFilmSubmit);

        // Обработчики для пользователей
        document.getElementById('get-users').addEventListener('click', fetchUsers);
        document.getElementById('user-form').addEventListener('submit', handleUserSubmit);

        // Загружаем начальные данные
        showPage('films');
        fetchInitialData();
    }

    function setupNavigation() {
        document.querySelectorAll('nav a').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const pageId = this.getAttribute('data-page');
                showPage(pageId);
            });
        });
    }

    function fetchInitialData() {
        fetchFilms();
        fetchGenres();
        fetchMpaRatings();
    }

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
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const films = await response.json();
            renderFilms(films);
        } catch (error) {
            console.error('Ошибка при загрузке фильмов:', error);
            showError('Ошибка при загрузке фильмов');
        }
    }

    async function fetchPopularFilms() {
        try {
            const response = await fetch(`${API_BASE_URL}/films/popular?count=10`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const films = await response.json();
            renderFilms(films);
        } catch (error) {
            console.error('Ошибка при загрузке популярных фильмов:', error);
            showError('Ошибка при загрузке популярных фильмов');
        }
    }

    function renderFilms(films) {
        const filmsList = document.getElementById('films-list');
        filmsList.innerHTML = '';

        if (!films || films.length === 0) {
            filmsList.innerHTML = '<p>Нет доступных фильмов</p>';
            return;
        }

        films.forEach(film => {
            const filmCard = document.createElement('div');
            filmCard.className = 'item-card';
            filmCard.innerHTML = `
                <h3>${film.name}</h3>
                <p>${film.description || 'Нет описания'}</p>
                <p>Дата выхода: ${formatDate(film.releaseDate)}</p>
                <p>Длительность: ${film.duration} мин</p>
                <div class="actions">
                    <button data-id="${film.id}" class="edit-film">Редактировать</button>
                    <button data-id="${film.id}" class="delete-film">Удалить</button>
                </div>
            `;
            filmsList.appendChild(filmCard);
        });

        // Добавляем обработчики для новых кнопок
        document.querySelectorAll('.edit-film').forEach(btn => {
            btn.addEventListener('click', () => editFilm(btn.dataset.id));
        });

        document.querySelectorAll('.delete-film').forEach(btn => {
            btn.addEventListener('click', () => deleteFilm(btn.dataset.id));
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
            const url = filmId ? `${API_BASE_URL}/films/${filmId}` : `${API_BASE_URL}/films`;
            const method = filmId ? 'PUT' : 'POST';

            if (filmId) filmData.id = filmId;

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(filmData),
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            form.reset();
            fetchFilms();
            showSuccess(filmId ? 'Фильм обновлен' : 'Фильм создан');
        } catch (error) {
            console.error('Ошибка при сохранении фильма:', error);
            showError('Ошибка при сохранении фильма');
        }
    }

    // Функции для работы с пользователями
    async function fetchUsers() {
        try {
            const response = await fetch(`${API_BASE_URL}/users`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const users = await response.json();
            renderUsers(users);
        } catch (error) {
            console.error('Ошибка при загрузке пользователей:', error);
            showError('Ошибка при загрузке пользователей');
        }
    }

    function renderUsers(users) {
        const usersList = document.getElementById('users-list');
        usersList.innerHTML = '';

        if (!users || users.length === 0) {
            usersList.innerHTML = '<p>Нет зарегистрированных пользователей</p>';
            return;
        }

        users.forEach(user => {
            const userCard = document.createElement('div');
            userCard.className = 'item-card';
            userCard.innerHTML = `
                <h3>${user.name || user.login}</h3>
                <p>Email: ${user.email}</p>
                <p>Логин: ${user.login}</p>
                <p>День рождения: ${formatDate(user.birthday) || 'Не указан'}</p>
                <div class="actions">
                    <button data-id="${user.id}" class="edit-user">Редактировать</button>
                    <button data-id="${user.id}" class="delete-user">Удалить</button>
                </div>
            `;
            usersList.appendChild(userCard);
        });

        // Добавляем обработчики для новых кнопок
        document.querySelectorAll('.edit-user').forEach(btn => {
            btn.addEventListener('click', () => editUser(btn.dataset.id));
        });

        document.querySelectorAll('.delete-user').forEach(btn => {
            btn.addEventListener('click', () => deleteUser(btn.dataset.id));
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
            const url = userId ? `${API_BASE_URL}/users/${userId}` : `${API_BASE_URL}/users`;
            const method = userId ? 'PUT' : 'POST';

            if (userId) userData.id = userId;

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            form.reset();
            fetchUsers();
            showSuccess(userId ? 'Пользователь обновлен' : 'Пользователь создан');
        } catch (error) {
            console.error('Ошибка при сохранении пользователя:', error);
            showError('Ошибка при сохранении пользователя');
        }
    }

    // Функции для работы с жанрами и рейтингами
    async function fetchGenres() {
        try {
            const response = await fetch(`${API_BASE_URL}/genres`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const genres = await response.json();
            renderGenres(genres);
        } catch (error) {
            console.error('Ошибка при загрузке жанров:', error);
            showError('Ошибка при загрузке жанров');
        }
    }

    function renderGenres(genres) {
        const genresList = document.getElementById('genres-list');
        genresList.innerHTML = '';

        if (!genres || genres.length === 0) {
            genresList.innerHTML = '<p>Нет доступных жанров</p>';
            return;
        }

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
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const mpaRatings = await response.json();
            renderMpaRatings(mpaRatings);
        } catch (error) {
            console.error('Ошибка при загрузке рейтингов:', error);
            showError('Ошибка при загрузке рейтингов');
        }
    }

    function renderMpaRatings(mpaRatings) {
        const mpaList = document.getElementById('mpa-list');
        mpaList.innerHTML = '';

        if (!mpaRatings || mpaRatings.length === 0) {
            mpaList.innerHTML = '<p>Нет доступных рейтингов</p>';
            return;
        }

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
        fetch(`${API_BASE_URL}/films/${id}`)
            .then(response => {
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                return response.json();
            })
            .then(film => {
                const form = document.getElementById('film-form');
                form.querySelector('#film-id').value = film.id;
                form.querySelector('#film-name').value = film.name;
                form.querySelector('#film-description').value = film.description || '';
                form.querySelector('#film-release-date').value = film.releaseDate;
                form.querySelector('#film-duration').value = film.duration;

                // Прокручиваем к форме
                form.scrollIntoView({ behavior: 'smooth' });
            })
            .catch(error => {
                console.error('Ошибка при загрузке фильма:', error);
                showError('Ошибка при загрузке фильма для редактирования');
            });
    }

    function deleteFilm(id) {
        if (confirm('Вы уверены, что хотите удалить этот фильм?')) {
            fetch(`${API_BASE_URL}/films/${id}`, {
                method: 'DELETE',
            })
                .then(response => {
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    fetchFilms();
                    showSuccess('Фильм удален');
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    showError('Ошибка при удалении фильма');
                });
        }
    }

    function editUser(id) {
        fetch(`${API_BASE_URL}/users/${id}`)
            .then(response => {
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                return response.json();
            })
            .then(user => {
                const form = document.getElementById('user-form');
                form.querySelector('#user-id').value = user.id;
                form.querySelector('#user-email').value = user.email;
                form.querySelector('#user-login').value = user.login;
                form.querySelector('#user-name').value = user.name || '';
                form.querySelector('#user-birthday').value = user.birthday || '';

                // Прокручиваем к форме
                form.scrollIntoView({ behavior: 'smooth' });
            })
            .catch(error => {
                console.error('Ошибка при загрузке пользователя:', error);
                showError('Ошибка при загрузке пользователя для редактирования');
            });
    }

    function deleteUser(id) {
        if (confirm('Вы уверены, что хотите удалить этого пользователя?')) {
            fetch(`${API_BASE_URL}/users/${id}`, {
                method: 'DELETE',
            })
                .then(response => {
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    fetchUsers();
                    showSuccess('Пользователь удален');
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    showError('Ошибка при удалении пользователя');
                });
        }
    }

    function formatDate(dateString) {
        if (!dateString) return null;
        const date = new Date(dateString);
        return date.toLocaleDateString();
    }

    function showError(message) {
        alert(`Ошибка: ${message}`);
    }

    function showSuccess(message) {
        alert(`Успех: ${message}`);
    }
});