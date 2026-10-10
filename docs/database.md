# База даних

Міграції — `src/main/resources/db/migration`, формат Flyway `V{номер}__{опис}.sql`.
`ddl-auto: validate`, тож схему визначають виключно міграції, а розбіжність між entity й
міграцією валить старт (і тести).

## Міграції

| Версія | Що робить |
| --- | --- |
| V1 | `users` |
| V2 | `languages` + сід довідника (~160 мов) |
| V3 | `dictionaries`, `words` |
| V4 | федеративний вхід: `provider`, `provider_id`, `password_hash` стає nullable |
| V5 | `password_reset_tokens` |
| V6 | UNIQUE `(user_id, language_code)` на `dictionaries` |
| V7 | індекси під гарячі читання |
| V8 | `users.role` |

## Чому саме ці констрейнти

### V4 — `uq_users_provider_identity UNIQUE (provider, provider_id)`

Один акаунт на ідентичність провайдера. `LOCAL`-рядки тримають `provider_id` як `NULL`, а
Postgres вважає `NULL` відмінними між собою — тому констрейнт не обмежує парольні акаунти.

### V4 — `ck_users_credentials`

Рядок мусить мати чим автентифікуватись: або пароль, або ідентичність провайдера.

### V6 — один словник на мову на користувача

Код уже це припускав: словник шукався за user + language code з розрахунком максимум на
один рядок. Але нічого цього не тримало, тож паралельні створення могли лишити дублікати.

### V8 — `ck_users_role CHECK (role IN ('USER', 'ADMIN'))`

Адміна навмисно не засіяно. Акаунт, який може писати спільні дані, має створюватись
свідомо, а не постачатися зі схемою під паролем, що лежить у репозиторії. Видається вручну:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

## Індекси (V7)

Postgres **не** індексує колонку foreign key сам, тож це не дублювання автоматики:

- `idx_words_dictionary_id` — `words.dictionary_id` несе два найгарячіших читання в API:
  `WordRepository.findByDictionaryId` і підзапит належності, який виконує кожен
  owner-scoped запит до слів. Без індексу це був seq scan по всій таблиці.
- `idx_words_lower_word` — `findOwnedByWord` зіставляє по `lower(word)`. Звичайний індекс на
  `word` такий предикат не обслужить, вираз треба індексувати як написано.
- `idx_password_reset_tokens_expires_at` — sweeper видаляє за терміном; без індексу він
  сканував би таблицю щоразу.

`dictionaries.user_id` власного індексу не потребує: UNIQUE-констрейнт із V6 підпертий
індексом, чия провідна колонка — `user_id`, і він уже обслуговує `findAllByUserId` та
підзапит належності.

## Типи

### `Instant`, не `LocalDateTime`

Колонки — `TIMESTAMPTZ`. `LocalDateTime` ходить через них із відкинутим offset, і далі
серіалізувався в JSON без зони — на відміну від кожного іншого timestamp в API.

### `TEXT[]` для `definitions`, `examples`, `synonyms_id`, `antonyms_id`

Масиви Postgres, мапляться на `String[]`. Через це валідація використовує `@NotEmpty`, а не
`@NotBlank` (див. [decisions.md](decisions.md)).
