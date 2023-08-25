# Restful API Социальной медиа платформы
Целью проекта ставится разработка api, позволяющего пользователям реге
стрироваться, входить в систему, создавать посты, переписываться,
подписываться на других пользователей и получать свою ленту активности.

# Запуск приложения
Для запуска Вам необходимо выполнить несколько этапов:
1. Скачать zip архив с проектом. После откройте проект в Intellij Idea Ultimate.
2. Открыть файл docker-compose в меню проекта. Запустить оба контейнера. Перейти в браузере по адресу http://localhost:9000, логин и пароль
minioadmin после создать бакет images
4. Запустите микросервис Authentication-service
5. Запустите микросервис Posts-service
6. Запустите микросервис Subscriptions-service
7. Для взаимодействия с приложением требуется открыть три ссылки на swagger UI,каждая на свой микросервис:

http://localhost:8080/swagger-ui/index.html#

http://localhost:8085/swagger-ui/index.html#

http://localhost:8090/swagger-ui/index.html#
В первом приложении Вы получаете токен, который потом используете при каждом запросе вне authentication-service(в нем токен вставлять не нужно).
Токен вставляется в кнопке Authorize. Таким образом, взаимодействие с приложением производится через три окна, в каждом из которых своя логика.
