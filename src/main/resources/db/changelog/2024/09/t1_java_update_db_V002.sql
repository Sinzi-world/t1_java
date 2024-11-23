ALTER TABLE transaction
    ADD COLUMN transaction_id UUID NOT NULL,
    ADD COLUMN status varchar (64);

ALTER TABLE account
    ADD COLUMN account_id UUID NOT NULL,
    ADD COLUMN account_status VARCHAR(10) NOT NULL,
    ADD COLUMN frozen_amount DECIMAL(15, 2) NOT NULL;

ALTER TABLE client
    ADD COLUMN client_id UUID NOT NULL;

