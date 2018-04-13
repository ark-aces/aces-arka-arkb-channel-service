CREATE TABLE contracts (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255),
  status VARCHAR(20),
  recipient_arkb_address VARCHAR(255),
  deposit_arka_address VARCHAR(255),
  deposit_arka_address_passphrase VARCHAR(255),
  subscription_id VARCHAR(255),
  created_at TIMESTAMP
);

CREATE TABLE transfers (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  contract_pid BIGINT NOT NULL,
  status VARCHAR(255),
  arka_transaction_id VARCHAR(255),
  arka_amount DECIMAL(8,5),
  arka_to_arkb_rate DECIMAL(8,5),
  arka_flat_fee DECIMAL(8,5),
  arka_percent_fee DECIMAL(8,5),
  arka_total_fee DECIMAL(8,5),
  arkb_send_amount DECIMAL(8,5),
  arkb_transaction_id VARCHAR(255),
  needs_arkb_confirmation BOOLEAN,
  arkb_confirmation_subscription_id VARCHAR(255),
  needs_arka_return BOOLEAN,
  return_arka_transaction_id VARHCHAR(255)
);
ALTER TABLE transfers ADD CONSTRAINT FOREIGN KEY (contract_pid) REFERENCES contracts (pid);
