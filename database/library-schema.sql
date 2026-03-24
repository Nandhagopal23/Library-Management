CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    available_copies INT NOT NULL CHECK (available_copies >= 0)
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE borrows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE NULL,
    late_penalty DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_borrows_book FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_borrows_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_borrows_user_active ON borrows(user_id, return_date);
CREATE INDEX idx_borrows_book_active ON borrows(book_id, return_date);
