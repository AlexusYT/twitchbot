# TwitchBot
Чат-бот для Twitch. Не обновляется. Часть функционала скорее всего уже не работает из-за обновлений API на стороне Twitch. 

# Лицензия

Исходный код, а также все, что с ним связано, запрещено распространять, компилировать, использовать или проводить с ним какие-либо другие манипуляции, кроме просмотра с целью изучения работы алгоритмов. 

## Функционал

* Собственная чат-валюта.
* Информация о ней и о статистике канала хранится в бд.
* Статистика трансляций (Дата и время начала, дата и время конца)
* Если после падения трансляция в течение 5 минут возобновилась, то бот продолжает сессию трансляции, иначе начинает новую.
* Активация бота (запуск сессии) при старте стрима (скорее всего уже не работает) или командой.
* Разграничение прав доступа между участниками чата.
* Различные команды.
* Возможность создавать обработчики наград (reward redeem handler). Скорее всего уже не работает.
* Исправление неправильной раскладки сообщения, написанного другим участником.
* Наказание в виде штрафа баллами при флуде.
* Возможность управлять ботом из личных сообщений (только стример и создатель).
* ...

Больше функционала можно найти в исходном коде

Стоит заметить, что бот работал на heroku. Для запуска Java-приложения на этом хостинге, приложение должно быть зависимо от Spring, но непосредственно на функционирование это не влияет. 

Также прошу обратить внимание, что бот возможно не будет работать в текущем состоянии, так как он был заморожен в процессе переписывания на новый Twitch API. 
