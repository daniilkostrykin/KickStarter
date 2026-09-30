CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL DEFAULT '$2a$10$w8.3f/eX1mUfQfWp5oGg7ODd1xWqC5qK.9N1xPzOa/b9UaH2Xg7Gy',
                       role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
                       version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE projects (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                          title VARCHAR(100) NOT NULL UNIQUE,
                          description TEXT NOT NULL,
                          goal NUMERIC(15, 2) NOT NULL,
                          pledged NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
                          status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
                          deadline TIMESTAMP WITH TIME ZONE NOT NULL,
                          author_id BIGINT NOT NULL REFERENCES users(id),
                          version BIGINT NOT NULL DEFAULT 0,
                          CONSTRAINT chk_projects_goal CHECK (goal > 0),
                          CONSTRAINT chk_projects_pledged CHECK (pledged >= 0)
);

CREATE INDEX idx_projects_author_id ON projects(author_id);

CREATE TABLE rewards (
                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                         project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                         title VARCHAR(100) NOT NULL,
                         description VARCHAR(255) NOT NULL,
                         min_price NUMERIC(15, 2) NOT NULL,
                         version BIGINT NOT NULL DEFAULT 0,
                         CONSTRAINT chk_rewards_min_price CHECK (min_price > 0)
);

CREATE INDEX idx_rewards_project_id ON rewards(project_id);

CREATE TABLE pledges (
                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                         project_id BIGINT NOT NULL REFERENCES projects(id),
                         reward_id BIGINT NOT NULL REFERENCES rewards(id),
                         user_id BIGINT NOT NULL REFERENCES users(id),
                         amount NUMERIC(15, 2) NOT NULL,
                         status VARCHAR(32) NOT NULL,
                         transaction_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         version BIGINT NOT NULL DEFAULT 0,
                         CONSTRAINT chk_pledges_amount CHECK (amount > 0)
);

CREATE INDEX idx_pledges_project_id ON pledges(project_id);
CREATE INDEX idx_pledges_user_id ON pledges(user_id);