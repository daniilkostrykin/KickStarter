-- Пользователи
INSERT INTO users (username, email, role) VALUES
                                              ('admin_creator', 'admin@kickstarter.local', 'ROLE_ADMIN'),
                                              ('investor_pro', 'investor@mail.ru', 'ROLE_USER'),
                                              ('tech_geek', 'geek@gmail.com', 'ROLE_USER');

-- Проекты
INSERT INTO projects (title, description, goal, pledged, status, deadline, author_id) VALUES
                                                                                          ('Умный рюкзак с солнечной батареей', 'Рюкзак со встроенным powerbank и защитой от краж для студентов.', 500000.00, 15000.00, 'ACTIVE', CURRENT_TIMESTAMP + INTERVAL '30 days', 1),
                                                                                          ('Инди-игра Cyber Dungeon', 'Пиксельная RPG с открытым миром и сложными боссами.', 100000.00, 125000.00, 'SUCCESSFUL', CURRENT_TIMESTAMP - INTERVAL '2 days', 3),
                                                                                          ('Эко-кроссовки из пластика', 'Стильная обувь, спасающая экологию.', 300000.00, 0.00, 'DRAFT', CURRENT_TIMESTAMP + INTERVAL '60 days', 2);

-- Награды
INSERT INTO rewards (project_id, title, description, min_price) VALUES
                                                                    (1, 'Ранняя пташка', 'Один рюкзак со скидкой 50%', 5000.00),
                                                                    (1, 'Комбо набор', 'Рюкзак + термокружка', 7500.00),
                                                                    (2, 'Цифровая копия', 'Ключ в Steam', 500.00);

-- Взносы
INSERT INTO pledges (project_id, reward_id, user_id, amount, status, transaction_date) VALUES
                                                                                           (1, 1, 2, 5000.00, 'CAPTURED', CURRENT_TIMESTAMP - INTERVAL '5 days'),
                                                                                           (1, 2, 3, 10000.00, 'CAPTURED', CURRENT_TIMESTAMP - INTERVAL '2 days');