document.addEventListener('DOMContentLoaded', function() {
    const API_BASE_URL = 'http://localhost:8080/api';
    let currentUserId = null;
    let currentFilmId = null;
    let currentReviewId = null;
    let allMpaRatings = [];
    let allGenres = [];
    let selectedMpa = null;
    let selectedGenres = [];
    let allUsers = [];
    let allFilms = [];
    let allReviews = [];

    // Инициализация приложения
    initApp();

    const MPA_DESCRIPTIONS = {
        "G": "Нет возрастных ограничений",
        "PG": "Рекомендуется присутствие родителей",
        "PG-13": "Детям до 13 лет просмотр не желателен",
        "R": "Лицам до 17 лет обязательно присутствие взрослого",
        "NC-17": "Лицам до 18 лет просмотр запрещён"
    };

    function initApp() {
        setupNavigation();
        setupEventListeners();
        fetchInitialData();
    }

    function setupNavigation() {
        document.querySelectorAll('nav a').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const pageId = this.getAttribute('data-page');
                showPage(pageId);

                if (pageId === 'users') {
                    fetchUsers();
                }

                if (pageId === 'reviews') {
                    fetchReviews();
                }

                if (pageId === 'genres') {
                    fetchGenres();
                }

                if (pageId === 'mpa') {
                    fetchMpaRatings();
                }

                if (pageId === 'feed') {
                    document.getElementById('feed-list').innerHTML = '';
                    document.getElementById('feed-user-id').value = '';
                }
            });
        });
    }

    function setupEventListeners() {
        // Фильмы
        document.getElementById('get-films').addEventListener('click', fetchFilms);
        document.getElementById('get-popular').addEventListener('click', fetchPopularFilms);
        document.getElementById('film-form').addEventListener('submit', handleFilmSubmit);

        // Пользователи
        document.getElementById('get-users').addEventListener('click', fetchUsers);
        document.getElementById('get-friends').addEventListener('click', showFriendsModal);
        document.getElementById('get-recommendations').addEventListener('click', showRecommendations);
        document.getElementById('user-form').addEventListener('submit', handleUserSubmit);
        document.getElementById('close-recommendations').addEventListener('click', hideRecommendations);

        // Отзывы
        document.getElementById('get-reviews').addEventListener('click', fetchReviews);
        document.getElementById('get-film-reviews').addEventListener('click', showFilmReviewsModal);
        document.getElementById('review-form').addEventListener('submit', handleReviewSubmit);

        // Выпадающие списки
        document.getElementById('mpa-dropdown').addEventListener('click', toggleMpaDropdown);
        document.getElementById('genre-dropdown').addEventListener('click', toggleGenreDropdown);

        document.getElementById('feed-form').addEventListener('submit', function(e) {
            e.preventDefault();
            const userId = document.getElementById('feed-user-id').value.trim();

            if (!userId) {
                showError('Введите ID пользователя');
                return;
            }

            if (!/^\d+$/.test(userId)) {
                showError('ID пользователя должен быть числом');
                return;
            }

            fetchFeed(userId);
        });

        // Модальные окна
        document.querySelectorAll('.close-modal').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.modal').forEach(modal => {
                    modal.style.display = 'none';
                });
            });
        });

        // Друзья
        document.getElementById('add-friend-btn').addEventListener('click', addFriend);

        // Действия с отзывами
        document.getElementById('like-review-btn').addEventListener('click', likeReview);
        document.getElementById('dislike-review-btn').addEventListener('click', dislikeReview);
        document.getElementById('remove-like-review-btn').addEventListener('click', removeLikeReview);
        document.getElementById('remove-dislike-review-btn').addEventListener('click', removeDislikeReview);

        // Закрытие при клике вне списка
        document.addEventListener('click', closeAllDropdowns);
    }

    function fetchInitialData() {
        fetchFilms();
        loadMpaRatings();
        loadGenres();
    }

    function showPage(pageId) {
        document.querySelectorAll('.page').forEach(page => {
            page.classList.remove('active');
        });
        document.getElementById(pageId).classList.add('active');

        // Скрываем рекомендации при переходе на другую страницу
        if (pageId !== 'users') {
            hideRecommendations();
        }
    }

    // ========== УВЕДОМЛЕНИЯ ==========
    function showNotification(message, isSuccess = true) {
        const notification = document.createElement('div');
        notification.className = `notification ${isSuccess ? 'success' : 'error'}`;
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    function showError(message) {
        alert('Ошибка: ' + message); // Можно заменить на более красивый вывод
        console.error(message);
    }

// Функция для показа успешных сообщений
    function showSuccess(message) {
        alert('Успех: ' + message); // Можно заменить на более красивый вывод
        console.log(message);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========
    function formatDate(dateString) {
        if (!dateString) return 'Не указана';
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU');
    }

    function closeAllDropdowns(e) {
        if (!e.target.closest('.dropdown')) {
            document.querySelectorAll('.dropdown-menu').forEach(menu => {
                menu.classList.remove('show');
            });
            document.querySelectorAll('.dropdown-toggle').forEach(toggle => {
                toggle.classList.remove('active');
            });
        }
    }

    function toggleMpaDropdown(e) {
        e.stopPropagation();
        const menu = document.getElementById('mpa-menu');
        const toggle = document.getElementById('mpa-dropdown');

        menu.classList.toggle('show');
        toggle.classList.toggle('active');
    }

    function toggleGenreDropdown(e) {
        e.stopPropagation();
        const menu = document.getElementById('genre-menu');
        const toggle = document.getElementById('genre-dropdown');

        menu.classList.toggle('show');
        toggle.classList.toggle('active');
    }

    // ========== РАБОТА С ФИЛЬМАМИ ==========
    async function fetchFilms() {
        try {
            const response = await fetch(`${API_BASE_URL}/films`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            allFilms = await response.json();
            renderFilms(allFilms);
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
                <p>Рейтинг: ${film.mpa?.name || 'Не указан'}</p>
                <p>Жанры: ${film.genres?.map(g => g.name).join(', ') || 'Не указаны'}</p>
                <div class="actions">
                    <button data-id="${film.id}" class="edit-btn edit-film">Редактировать</button>
                    <button data-id="${film.id}" class="delete-btn delete-film">Удалить</button>
                    <button data-id="${film.id}" class="like-btn like-film">Лайк</button>
                </div>
            `;
            filmsList.appendChild(filmCard);
        });

        // Назначение обработчиков для кнопок
        document.querySelectorAll('.edit-film').forEach(btn => {
            btn.addEventListener('click', () => editFilm(btn.dataset.id));
        });

        document.querySelectorAll('.delete-film').forEach(btn => {
            btn.addEventListener('click', () => deleteFilm(btn.dataset.id));
        });

        document.querySelectorAll('.like-film').forEach(btn => {
            btn.addEventListener('click', () => likeFilm(btn.dataset.id));
        });
    }

    async function editFilm(filmId) {
        try {
            const response = await fetch(`${API_BASE_URL}/films/${filmId}`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const film = await response.json();

            // Заполняем форму данными фильма
            const form = document.getElementById('film-form');
            form.querySelector('#film-id').value = film.id;
            form.querySelector('#film-name').value = film.name;
            form.querySelector('#film-description').value = film.description || '';
            form.querySelector('#film-release-date').value = film.releaseDate;
            form.querySelector('#film-duration').value = film.duration;

            // Устанавливаем рейтинг
            if (film.mpa) {
                selectedMpa = film.mpa;
                document.getElementById('mpa-selected-text').innerHTML = `
                    <strong>${film.mpa.name}</strong>
                    <div class="mpa-description">${MPA_DESCRIPTIONS[film.mpa.name] || ''}</div>
                `;
                document.getElementById('selected-mpa').innerHTML = `
                    <span>
                        <strong>${film.mpa.name}</strong>
                        <div class="mpa-description">${MPA_DESCRIPTIONS[film.mpa.name] || ''}</div>
                    </span>
                    <button type="button" class="remove-btn">×</button>
                `;
                document.getElementById('selected-mpa').querySelector('.remove-btn').addEventListener('click', (e) => {
                    e.stopPropagation();
                    clearMpaSelection();
                });
            }

            // Устанавливаем жанры
            if (film.genres && film.genres.length > 0) {
                selectedGenres = [...film.genres];
                renderSelectedGenres();
            }

            // Прокручиваем к форме
            form.scrollIntoView({ behavior: 'smooth' });
        } catch (error) {
            console.error('Ошибка при загрузке фильма:', error);
            showError('Ошибка при загрузке фильма');
        }
    }

    async function deleteFilm(filmId) {
        if (!confirm('Вы уверены, что хотите удалить этот фильм?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/films/${filmId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Фильм удален');
            fetchFilms();
        } catch (error) {
            console.error('Ошибка при удалении фильма:', error);
            showError('Ошибка при удалении фильма');
        }
    }

    async function likeFilm(filmId) {
        if (!currentUserId) {
            showError('Пожалуйста, выберите пользователя для оценки фильма');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/films/${filmId}/like/${currentUserId}`, {
                method: 'PUT'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Лайк добавлен');
            fetchFilms();
        } catch (error) {
            console.error('Ошибка при добавлении лайка:', error);
            showError('Ошибка при добавлении лайка');
        }
    }

    async function handleFilmSubmit(e) {
        e.preventDefault();
        const form = e.target;
        const filmId = form.querySelector('#film-id').value;
        const releaseDate = form.querySelector('#film-release-date').value;

        // Валидация года (4 цифры)
        const year = new Date(releaseDate).getFullYear();
        if (year.toString().length !== 4) {
            showError('Год должен содержать 4 цифры (например: 1999)');
            return;
        }

        // Проверка минимальной даты (1895-12-28)
        const minDate = new Date('1895-12-28');
        const inputDate = new Date(releaseDate);
        if (inputDate < minDate) {
            showError('Дата релиза не может быть раньше 28 декабря 1895 года');
            return;
        }

        const filmData = {
            name: form.querySelector('#film-name').value,
            description: form.querySelector('#film-description').value,
            releaseDate: releaseDate,
            duration: parseInt(form.querySelector('#film-duration').value),
            mpa: selectedMpa ? { id: selectedMpa.id } : null,
            genres: selectedGenres.map(genre => ({ id: genre.id }))
        };

        try {
            const url = `${API_BASE_URL}/films`;
            const method = filmId ? 'PUT' : 'POST';

            if (filmId) filmData.id = parseInt(filmId);

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(filmData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Неизвестная ошибка');
            }

            // Сброс формы
            form.reset();
            document.getElementById('film-id').value = '';
            clearSelections();
            fetchFilms();
            showSuccess(filmId ? 'Фильм обновлен' : 'Фильм создан');
        } catch (error) {
            console.error('Ошибка при сохранении фильма:', error);
            showError(error.message || 'Ошибка при сохранении фильма');
        }
    }

    function clearSelections() {
        selectedMpa = null;
        selectedGenres = [];
        document.getElementById('mpa-selected-text').textContent = 'Выберите рейтинг';
        document.getElementById('selected-mpa').innerHTML = '';
        document.getElementById('selected-genres').innerHTML = '';
        populateGenreDropdown();
    }

    // ========== РАБОТА С ПОЛЬЗОВАТЕЛЯМИ ==========
    async function fetchUsers() {
        try {
            const response = await fetch(`${API_BASE_URL}/users`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            allUsers = await response.json();
            renderUsers(allUsers);
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
                <p>Дата рождения: ${formatDate(user.birthday)}</p>
                <div class="actions">
                    <button data-id="${user.id}" class="edit-btn edit-user">Редактировать</button>
                    <button data-id="${user.id}" class="delete-btn delete-user">Удалить</button>
                    <button data-id="${user.id}" class="like-btn select-user">Выбрать</button>
                    <button data-id="${user.id}" class="feed-btn view-feed">Лента</button>
                </div>
            `;
            usersList.appendChild(userCard);
        });

        // Назначение обработчиков для кнопок
        document.querySelectorAll('.edit-user').forEach(btn => {
            btn.addEventListener('click', () => editUser(btn.dataset.id));
        });

        document.querySelectorAll('.delete-user').forEach(btn => {
            btn.addEventListener('click', () => deleteUser(btn.dataset.id));
        });

        document.querySelectorAll('.select-user').forEach(btn => {
            btn.addEventListener('click', () => selectUser(btn.dataset.id));
        });

        document.querySelectorAll('.view-feed').forEach(btn => {
            btn.addEventListener('click', () => {
                currentUserId = btn.dataset.id;
                showPage('feed');
                fetchFeed();
            });
        });
    }

    async function editUser(userId) {
        try {
            const response = await fetch(`${API_BASE_URL}/users/${userId}`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const user = await response.json();

            // Заполняем форму данными пользователя
            const form = document.getElementById('user-form');
            form.querySelector('#user-id').value = user.id;
            form.querySelector('#user-email').value = user.email;
            form.querySelector('#user-login').value = user.login;
            form.querySelector('#user-name').value = user.name || '';
            form.querySelector('#user-birthday').value = user.birthday || '';

            // Прокручиваем к форме
            form.scrollIntoView({ behavior: 'smooth' });
        } catch (error) {
            console.error('Ошибка при загрузке пользователя:', error);
            showError('Ошибка при загрузке пользователя');
        }
    }

    async function deleteUser(userId) {
        if (!confirm('Вы уверены, что хотите удалить этого пользователя?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Пользователь удален');
            fetchUsers();
        } catch (error) {
            console.error('Ошибка при удалении пользователя:', error);
            showError('Ошибка при удалении пользователя');
        }
    }

    function selectUser(userId) {
        currentUserId = userId;
        const user = allUsers.find(u => u.id == userId);
        showSuccess(`Выбран пользователь: ${user.name || user.login}`);
    }

    async function handleUserSubmit(e) {
        e.preventDefault();
        const form = e.target;
        const userId = form.querySelector('#user-id').value;

        const userData = {
            email: form.querySelector('#user-email').value,
            login: form.querySelector('#user-login').value,
            name: form.querySelector('#user-name').value || form.querySelector('#user-login').value,
            birthday: form.querySelector('#user-birthday').value || null
        };

        try {
            const url = `${API_BASE_URL}/users`;
            const method = userId ? 'PUT' : 'POST';

            if (userId) {
                userData.id = parseInt(userId);
            }

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Неизвестная ошибка');
            }

            // Сброс формы
            form.reset();
            document.getElementById('user-id').value = '';
            fetchUsers();
            showSuccess(userId ? 'Пользователь обновлен' : 'Пользователь создан');
        } catch (error) {
            console.error('Ошибка при сохранении пользователя:', error);
            showError(error.message || 'Ошибка при сохранении пользователя');
        }
    }

    // ========== РАБОТА С ДРУЗЬЯМИ ==========
    async function showFriendsModal() {
        if (!currentUserId) {
            showError('Пожалуйста, выберите пользователя');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/users/${currentUserId}/friends`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const friends = await response.json();

            const friendsList = document.getElementById('friends-list');
            friendsList.innerHTML = '';

            if (friends.length === 0) {
                friendsList.innerHTML = '<p>У пользователя нет друзей</p>';
            } else {
                friends.forEach(friend => {
                    const friendCard = document.createElement('div');
                    friendCard.className = 'item-card';
                    friendCard.innerHTML = `
                        <h3>${friend.name || friend.login}</h3>
                        <p>Email: ${friend.email}</p>
                        <div class="friendship-actions">
                            <button data-id="${friend.id}" class="delete-btn remove-friend">Удалить из друзей</button>
                        </div>
                    `;
                    friendsList.appendChild(friendCard);
                });

                document.querySelectorAll('.remove-friend').forEach(btn => {
                    btn.addEventListener('click', () => removeFriend(btn.dataset.id));
                });
            }

            document.getElementById('friends-modal').style.display = 'block';
        } catch (error) {
            console.error('Ошибка при загрузке друзей:', error);
            showError('Ошибка при загрузке друзей');
        }
    }

    async function addFriend() {
        const friendId = document.getElementById('friend-id').value;
        if (!friendId) {
            showError('Пожалуйста, введите ID друга');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/users/${currentUserId}/friends/${friendId}`, {
                method: 'PUT'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Запрос на добавление в друзья отправлен');
            document.getElementById('friend-id').value = '';
            showFriendsModal();
        } catch (error) {
            console.error('Ошибка при добавлении друга:', error);
            showError('Ошибка при добавлении друга');
        }
    }

    async function removeFriend(friendId) {
        if (!confirm('Вы уверены, что хотите удалить этого пользователя из друзей?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/users/${currentUserId}/friends/${friendId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Пользователь удален из друзей');
            showFriendsModal();
        } catch (error) {
            console.error('Ошибка при удалении друга:', error);
            showError('Ошибка при удалении друга');
        }
    }

    // ========== РЕКОМЕНДАЦИИ ==========
    async function showRecommendations() {
        if (!currentUserId) {
            showError('Пожалуйста, выберите пользователя');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/users/${currentUserId}/recommendations`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const recommendations = await response.json();

            const container = document.querySelector('.recommendations-container');
            container.innerHTML = '';

            if (recommendations.length === 0) {
                container.innerHTML = '<p>Нет рекомендаций для этого пользователя</p>';
            } else {
                recommendations.forEach(film => {
                    const card = document.createElement('div');
                    card.className = 'recommendation-card';
                    card.innerHTML = `
                        <h4>${film.name}</h4>
                        <p>${film.description || 'Нет описания'}</p>
                        <p>Рейтинг: ${film.mpa?.name || 'Не указан'}</p>
                    `;
                    container.appendChild(card);
                });
            }

            document.getElementById('users-list').style.display = 'none';
            document.getElementById('recommendations-list').style.display = 'block';
        } catch (error) {
            console.error('Ошибка при загрузке рекомендаций:', error);
            showError('Ошибка при загрузке рекомендаций');
        }
    }

    function hideRecommendations() {
        document.getElementById('recommendations-list').style.display = 'none';
        document.getElementById('users-list').style.display = 'block';
    }

    // ========== РАБОТА С ОТЗЫВАМИ ==========
    async function fetchReviews() {
        try {
            const response = await fetch(`${API_BASE_URL}/reviews`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            allReviews = await response.json();
            renderReviews(allReviews);
        } catch (error) {
            console.error('Ошибка при загрузке отзывов:', error);
            showError('Ошибка при загрузке отзывов');
        }
    }

    function renderReviews(reviews) {
        const reviewsList = document.getElementById('reviews-list');
        reviewsList.innerHTML = '';

        if (!reviews || reviews.length === 0) {
            reviewsList.innerHTML = '<p>Нет доступных отзывов</p>';
            return;
        }

        reviews.forEach(review => {
            const reviewCard = document.createElement('div');
            reviewCard.className = 'item-card';
            reviewCard.innerHTML = `
                <h3>Отзыв #${review.reviewId}</h3>
                <p>Фильм ID: ${review.filmId}</p>
                <p>Пользователь ID: ${review.userId}</p>
                <p>${review.content}</p>
                <p>Оценка: ${review.isPositive ? 'Положительный' : 'Отрицательный'}</p>
                <p>Полезность: ${review.useful}</p>
                <div class="actions">
                    <button data-id="${review.reviewId}" class="edit-btn edit-review">Редактировать</button>
                    <button data-id="${review.reviewId}" class="delete-btn delete-review">Удалить</button>
                    <button data-id="${review.reviewId}" class="like-btn review-actions">Действия</button>
                </div>
            `;
            reviewsList.appendChild(reviewCard);
        });

        // Назначение обработчиков для кнопок
        document.querySelectorAll('.edit-review').forEach(btn => {
            btn.addEventListener('click', () => editReview(btn.dataset.id));
        });

        document.querySelectorAll('.delete-review').forEach(btn => {
            btn.addEventListener('click', () => deleteReview(btn.dataset.id));
        });

        document.querySelectorAll('.review-actions').forEach(btn => {
            btn.addEventListener('click', () => showReviewActionsModal(btn.dataset.id));
        });
    }

    async function showFilmReviewsModal() {
        const filmId = prompt('Введите ID фильма:');
        if (!filmId) return;

        try {
            const response = await fetch(`${API_BASE_URL}/reviews?filmId=${filmId}&count=10`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const reviews = await response.json();

            if (reviews.length === 0) {
                showError('Нет отзывов для этого фильма');
                return;
            }

            renderReviews(reviews);
        } catch (error) {
            console.error('Ошибка при загрузке отзывов:', error);
            showError('Ошибка при загрузке отзывов');
        }
    }

    async function editReview(reviewId) {
        try {
            const response = await fetch(`${API_BASE_URL}/reviews/${reviewId}`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const review = await response.json();

            // Заполняем форму данными отзыва
            const form = document.getElementById('review-form');
            form.querySelector('#review-id').value = review.reviewId;
            form.querySelector('#review-film-id').value = review.filmId;
            form.querySelector('#review-user-id').value = review.userId;
            form.querySelector('#review-content').value = review.content;

            if (review.isPositive) {
                document.getElementById('review-positive').checked = true;
            } else {
                document.getElementById('review-negative').checked = true;
            }

            // Прокручиваем к форме
            form.scrollIntoView({ behavior: 'smooth' });
        } catch (error) {
            console.error('Ошибка при загрузке отзыва:', error);
            showError('Ошибка при загрузке отзыва');
        }
    }

    async function deleteReview(reviewId) {
        if (!confirm('Вы уверены, что хотите удалить этот отзыв?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/reviews/${reviewId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Отзыв удален');
            fetchReviews();
        } catch (error) {
            console.error('Ошибка при удалении отзыва:', error);
            showError('Ошибка при удалении отзыва');
        }
    }

    function showReviewActionsModal(reviewId) {
        if (!currentUserId) {
            showError('Пожалуйста, выберите пользователя');
            return;
        }

        currentReviewId = reviewId;
        document.getElementById('current-review-id').value = reviewId;
        document.getElementById('current-review-user-id').value = currentUserId;
        document.getElementById('review-actions-modal').style.display = 'block';
    }

    async function likeReview() {
        try {
            const response = await fetch(`${API_BASE_URL}/reviews/${currentReviewId}/like/${currentUserId}`, {
                method: 'PUT'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Лайк добавлен');
            document.getElementById('review-actions-modal').style.display = 'none';
            fetchReviews();
        } catch (error) {
            console.error('Ошибка при добавлении лайка:', error);
            showError('Ошибка при добавлении лайка');
        }
    }

    async function dislikeReview() {
        try {
            const response = await fetch(`${API_BASE_URL}/reviews/${currentReviewId}/dislike/${currentUserId}`, {
                method: 'PUT'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Дизлайк добавлен');
            document.getElementById('review-actions-modal').style.display = 'none';
            fetchReviews();
        } catch (error) {
            console.error('Ошибка при добавлении дизлайка:', error);
            showError('Ошибка при добавлении дизлайка');
        }
    }

    async function removeLikeReview() {
        try {
            const response = await fetch(`${API_BASE_URL}/reviews/${currentReviewId}/like/${currentUserId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Лайк удален');
            document.getElementById('review-actions-modal').style.display = 'none';
            fetchReviews();
        } catch (error) {
            console.error('Ошибка при удалении лайка:', error);
            showError('Ошибка при удалении лайка');
        }
    }

    async function removeDislikeReview() {
        try {
            const response = await fetch(`${API_BASE_URL}/reviews/${currentReviewId}/dislike/${currentUserId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            showSuccess('Дизлайк удален');
            document.getElementById('review-actions-modal').style.display = 'none';
            fetchReviews();
        } catch (error) {
            console.error('Ошибка при удалении дизлайка:', error);
            showError('Ошибка при удалении дизлайка');
        }
    }

    async function handleReviewSubmit(e) {
        e.preventDefault();
        const form = e.target;
        const reviewId = form.querySelector('#review-id').value;

        const reviewData = {
            content: form.querySelector('#review-content').value,
            isPositive: document.querySelector('input[name="review-rating"]:checked').value === 'true',
            userId: parseInt(form.querySelector('#review-user-id').value),
            filmId: parseInt(form.querySelector('#review-film-id').value)
        };

        try {
            let url, method;
            if (reviewId) {
                url = `${API_BASE_URL}/reviews`;
                method = 'PUT';
                reviewData.reviewId = parseInt(reviewId);
            } else {
                url = `${API_BASE_URL}/reviews`;
                method = 'POST';
            }

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(reviewData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Неизвестная ошибка');
            }

            // Сброс формы
            form.reset();
            document.getElementById('review-id').value = '';
            fetchReviews();
            showSuccess(reviewId ? 'Отзыв обновлен' : 'Отзыв создан');
        } catch (error) {
            console.error('Ошибка при сохранении отзыва:', error);
            showError(error.message || 'Ошибка при сохранении отзыва');
        }
    }

    // ========== РАБОТА С ЖАНРАМИ ==========
    async function fetchGenres() {
        try {
            const response = await fetch(`${API_BASE_URL}/genres`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            allGenres = await response.json();
            renderGenres(allGenres);
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

    // ========== РАБОТА С РЕЙТИНГАМИ MPA ==========
    async function fetchMpaRatings() {
        try {
            const response = await fetch(`${API_BASE_URL}/mpa`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            allMpaRatings = await response.json();
            renderMpaRatings(allMpaRatings);
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
                <p>${mpa.description || 'Нет описания'}</p>
            `;
            mpaList.appendChild(mpaCard);
        });
    }

    // ========== ЗАГРУЗКА ДАННЫХ ДЛЯ ФОРМ ==========
    async function loadMpaRatings() {
        try {
            const response = await fetch(`${API_BASE_URL}/mpa`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            allMpaRatings = await response.json();
            populateMpaDropdown();
        } catch (error) {
            console.error('Ошибка при загрузке рейтингов:', error);
            showError('Ошибка при загрузке рейтингов');
        }
    }

    function populateMpaDropdown() {
        const mpaMenu = document.getElementById('mpa-menu');
        mpaMenu.innerHTML = '';

        allMpaRatings.forEach(mpa => {
            const item = document.createElement('div');
            item.className = 'dropdown-item';
            item.innerHTML = `
                <strong>${mpa.name}</strong>
                <div class="mpa-description">${mpa.description || 'Нет описания'}</div>
            `;
            item.addEventListener('click', () => selectMpa(mpa));
            mpaMenu.appendChild(item);
        });
    }

    function selectMpa(mpa) {
        selectedMpa = mpa;
        document.getElementById('mpa-selected-text').innerHTML = `
            <strong>${mpa.name}</strong>
            <div class="mpa-description">${mpa.description || ''}</div>
        `;
        document.getElementById('selected-mpa').innerHTML = `
            <span>
                <strong>${mpa.name}</strong>
                <div class="mpa-description">${mpa.description || ''}</div>
            </span>
            <button type="button" class="remove-btn">×</button>
        `;
        document.getElementById('selected-mpa').querySelector('.remove-btn').addEventListener('click', (e) => {
            e.stopPropagation();
            clearMpaSelection();
        });
        document.getElementById('mpa-menu').classList.remove('show');
        document.getElementById('mpa-dropdown').classList.remove('active');
    }

    function clearMpaSelection() {
        selectedMpa = null;
        document.getElementById('mpa-selected-text').textContent = 'Выберите рейтинг';
        document.getElementById('selected-mpa').innerHTML = '';
    }

    async function loadGenres() {
        try {
            const response = await fetch(`${API_BASE_URL}/genres`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            allGenres = await response.json();
            populateGenreDropdown();
        } catch (error) {
            console.error('Ошибка при загрузке жанров:', error);
            showError('Ошибка при загрузке жанров');
        }
    }

    function populateGenreDropdown() {
        const genreMenu = document.getElementById('genre-menu');
        genreMenu.innerHTML = '';

        allGenres.forEach(genre => {
            const item = document.createElement('div');
            item.className = 'dropdown-item';
            item.innerHTML = `
                <strong>${genre.name}</strong>
            `;
            item.addEventListener('click', () => toggleGenreSelection(genre));
            genreMenu.appendChild(item);
        });
    }

    function toggleGenreSelection(genre) {
        const index = selectedGenres.findIndex(g => g.id === genre.id);
        if (index === -1) {
            selectedGenres.push(genre);
        } else {
            selectedGenres.splice(index, 1);
        }
        renderSelectedGenres();
    }

    function renderSelectedGenres() {
        const selectedGenresContainer = document.getElementById('selected-genres');
        selectedGenresContainer.innerHTML = '';

        if (selectedGenres.length === 0) {
            document.getElementById('genre-selected-text').textContent = 'Выберите жанры';
            return;
        }

        document.getElementById('genre-selected-text').textContent = `Выбрано: ${selectedGenres.length}`;

        selectedGenres.forEach(genre => {
            const tag = document.createElement('div');
            tag.className = 'selected-tag';
            tag.innerHTML = `
                <span>${genre.name}</span>
                <button type="button" class="remove-btn">×</button>
            `;
            tag.querySelector('.remove-btn').addEventListener('click', (e) => {
                e.stopPropagation();
                removeGenreSelection(genre);
            });
            selectedGenresContainer.appendChild(tag);
        });
    }

    function removeGenreSelection(genre) {
        selectedGenres = selectedGenres.filter(g => g.id !== genre.id);
        renderSelectedGenres();
    }
});

const style = document.createElement('style');
style.textContent = `
    .notification {
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        font-weight: bold;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    }
    
    .success {
        background-color: #28a745;
    }
    
    .error {
        background-color: #dc3545;
    }
    
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes fadeOut {
        from {
            opacity: 1;
        }
        to {
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

async function fetchFeed(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}/feed`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const feedEvents = await response.json();
        renderFeedEvents(feedEvents);
    } catch (error) {
        console.error('Ошибка при загрузке ленты событий:', error);
        showError('Ошибка при загрузке ленты событий');
    }
}

function renderFeedEvents(events) {
    const feedList = document.getElementById('feed-list');
    feedList.innerHTML = '';

    if (!events || events.length === 0) {
        feedList.innerHTML = '<p>Нет событий для отображения</p>';
        return;
    }

    events.forEach(event => {
        const eventCard = document.createElement('div');
        eventCard.className = 'event-card';

        // Определяем иконку в зависимости от типа события
        let icon, action;
        switch(event.eventType) {
            case 'LIKE':
                icon = '👍';
                action = event.operation === 'ADD' ? 'поставил лайк' : 'убрал лайк';
                break;
            case 'REVIEW':
                icon = '📝';
                action = event.operation === 'ADD' ? 'добавил отзыв' :
                    event.operation === 'REMOVE' ? 'удалил отзыв' : 'обновил отзыв';
                break;
            case 'FRIEND':
                icon = '👥';
                action = event.operation === 'ADD' ? 'добавил в друзья' : 'удалил из друзей';
                break;
            default:
                icon = '🔔';
                action = 'совершил действие';
        }

        const eventDate = new Date(event.timestamp).toLocaleString();

        eventCard.innerHTML = `
                <div class="event-header">
                    <span class="event-icon">${icon}</span>
                    <span class="event-type">${event.eventType}</span>
                    <span class="event-date">${eventDate}</span>
                </div>
                <div class="event-body">
                    <p>Пользователь ${action} (ID сущности: ${event.entityId})</p>
                    <p class="event-id">ID события: ${event.eventId}</p>
                </div>
            `;

        feedList.appendChild(eventCard);
    });
}

function renderFeed(events, userId) {
    const feedList = document.getElementById('feed-list');
    feedList.innerHTML = '';

    // Заголовок с ID пользователя
    const header = document.createElement('h3');
    header.textContent = `Лента событий пользователя #${userId}`;
    feedList.appendChild(header);

    // Сортируем события по времени (новые сверху)
    events.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    // Создаем контейнер для событий
    const eventsContainer = document.createElement('div');
    eventsContainer.className = 'events-container';

    events.forEach(event => {
        const eventCard = document.createElement('div');
        eventCard.className = 'event-card';

        const eventDate = new Date(event.timestamp);
        const formattedDate = eventDate.toLocaleString('ru-RU');

        let eventDescription = '';
        let icon = '🔹';
        let eventType = '';

        switch(event.eventType) {
            case 'LIKE':
                icon = '👍';
                eventType = 'Лайк';
                eventDescription = event.operation === 'ADD'
                    ? `Лайк фильму #${event.entityId}`
                    : `Удален лайк фильма #${event.entityId}`;
                break;
            case 'REVIEW':
                icon = '✍️';
                eventType = 'Отзыв';
                if (event.operation === 'ADD') eventDescription = `Добавлен отзыв #${event.entityId}`;
                else if (event.operation === 'REMOVE') eventDescription = `Удален отзыв #${event.entityId}`;
                else eventDescription = `Обновлен отзыв #${event.entityId}`;
                break;
            case 'FRIEND':
                icon = '👥';
                eventType = 'Друг';
                eventDescription = event.operation === 'ADD'
                    ? `Добавлен друг #${event.entityId}`
                    : `Удален друг #${event.entityId}`;
                break;
        }

        eventCard.innerHTML = `
                <div class="event-header">
                    <span class="event-icon">${icon}</span>
                    <span class="event-type">${eventType}</span>
                    <span class="event-date">${formattedDate}</span>
                </div>
                <div class="event-body">
                    <p>${eventDescription}</p>
                    <p class="event-id">ID события: ${event.eventId}</p>
                </div>
            `;
        eventsContainer.appendChild(eventCard);
    });

    feedList.appendChild(eventsContainer);
}