CREATE TABLE account (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    client_id BIGINT NOT NULL,
    account_type VARCHAR(10) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL,
    CONSTRAINT fk_account_client FOREIGN KEY (client_id) REFERENCES client(id)
);

CREATE TABLE transaction (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id BIGINT NOT NULL,
    transaction_amount DECIMAL(15, 2) NOT NULL,
    transaction_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE data_source_error_log (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    stack_trace TEXT,
    message VARCHAR(255) NOT NULL,
    method_signature VARCHAR(255) NOT NULL
);
