# LocTrackCar

[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)

Android приложение доступно на <a href="https://apps.rustore.ru/app/tk.ifroz.LocTrackCar">RuStore</a> и на <a href="https://4pda.to/forum/index.php?showtopic=713501&st=100">4pda</a>

<table>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/iFr0z/LocTrackCar/master/google%2Bplay%2B1.png" width="240" height="480" /></td>
    <td align="center"><img src="https://raw.githubusercontent.com/iFr0z/LocTrackCar/master/google%2Bplay%2B2.png" width="240" height="480" /></td>
    <td align="center"><img src="https://raw.githubusercontent.com/iFr0z/LocTrackCar/master/google%2Bplay%2B3.png" width="240" height="480" /></td>
    <td align="center"><img src="https://raw.githubusercontent.com/iFr0z/LocTrackCar/master/google%2Bplay%2B4.png" width="240" height="480" /></td>
  </tr>
</table>

Описание приложения
---

Главный и актуальный вопрос сейчас - Где Моя Машина? Теперь есть ответ! И мы поможем в этом!
<b>LocTrackCar</b> - это сервис, который всегда и в любое время покажет местоположение вашего автомобиля.
Если забыли, где оставили свою машину, приложение: определит местоположение, проложит путь.
А также если нужно забрать машину в определенное время, то приложение, несомненно, напомнит об этом.
Всё это и многое другое ждёт Вас в приложении <b>LocTrackCar</b>.

• Приложение <b>LocTrackCar</b> не использует надоедливую рекламу или платный функционал.

<b>Версия: 9.4.0</b>

---

Настройка
---
Данный проект построен на базе <a href="https://github.com/yandex/mapkit-android-demo">Яндекс Карт</a>. Каждый может сделать что-то свое на основе моего решения, но было бы здорово совместно улучшать данный проект. У меня полно идей, но не знаю как их реализовать пока что. Мои соцсети указаны в профиле для общения, присоединяйтесь. Если есть вопросы, задавайте тут https://t.me/ifr0z

Прежде чем начать, изучите это руководство от <a href="https://yandex.ru/dev/mapkit/doc/ru/android/quickstart">Яндекса</a>. Вы можете использовать мой ключ для <a href="https://github.com/iFr0z/LocTrackCar/blob/f82e619ff43d99940daa4a41ac4dcc025a3724c6/app/src/main/java/tk/ifroz/loctrackcar/ui/view/fragment/MapFragment.kt#L654">подключения</a>, но лучше завести свой на этом <a href="https://developer.tech.yandex.ru/services">сайте</a>.

---


Стек технологий
---

    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    ksp 'androidx.room:room-compiler:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.7'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7'
    implementation 'androidx.work:work-runtime:2.10.0'
    implementation 'androidx.work:work-runtime-ktx:2.10.0'
    implementation 'androidx.fragment:fragment-ktx:1.8.5'
    implementation 'androidx.navigation:navigation-fragment-ktx:2.8.4'
    implementation 'androidx.navigation:navigation-ui-ktx:2.8.4'
    implementation 'androidx.constraintlayout:constraintlayout:2.2.0'

    implementation 'com.google.android.gms:play-services-location:21.3.0'
    implementation 'com.yandex.android:maps.mobile:4.9.0-full'


