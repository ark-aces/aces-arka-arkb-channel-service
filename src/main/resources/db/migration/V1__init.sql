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
CREATE INDEX ON contracts (id);
CREATE INDEX ON contracts (correlation_id);
CREATE INDEX ON contracts (status);
CREATE INDEX ON contracts (recipient_arkb_address);
CREATE INDEX ON contracts (deposit_arka_address);
CREATE INDEX ON contracts (subscription_id);
CREATE INDEX ON contracts (created_at);

CREATE TABLE transfers (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  return_arka_address VARCHAR(255),
  contract_pid BIGINT NOT NULL,
  status VARCHAR(255),
  arka_transaction_id VARCHAR(255),
  arka_amount DECIMAL(40,5),
  arka_to_arkb_rate DECIMAL(40,5),
  arka_flat_fee DECIMAL(40,5),
  arka_percent_fee DECIMAL(40,5),
  arka_total_fee DECIMAL(40,5),
  arkb_per_arka DECIMAL(40, 5),
  arkb_send_amount DECIMAL(40,5),
  arkb_transaction_id VARCHAR(255),
  needs_arkb_confirmation BOOLEAN,
  arkb_confirmation_subscription_id VARCHAR(255),
  needs_arka_return BOOLEAN,
  return_arka_transaction_id VARCHAR(255)
);
ALTER TABLE transfers ADD FOREIGN KEY (contract_pid) REFERENCES contracts (pid);

CREATE INDEX ON transfers (id);
CREATE INDEX ON transfers (created_at);
CREATE INDEX ON transfers (contract_pid);
CREATE INDEX ON transfers (status);
CREATE INDEX ON transfers (arka_transaction_id);
CREATE INDEX ON transfers (arka_amount);
CREATE INDEX ON transfers (arkb_transaction_id);
CREATE INDEX ON transfers (return_arka_transaction_id);


CREATE TABLE service_capacities (
  pid BIGSERIAL PRIMARY KEY,
  available_amount DECIMAL(40, 8),
  unsettled_amount DECIMAL(40, 8),
  total_amount DECIMAL(40, 8),
  unit VARCHAR(20),
  updated_at TIMESTAMP,
  created_at TIMESTAMP
);
CREATE INDEX ON service_capacities (available_amount);
CREATE INDEX ON service_capacities (unsettled_amount);
CREATE INDEX ON service_capacities (total_amount);
CREATE INDEX ON service_capacities (unit);
CREATE INDEX ON service_capacities (updated_at);
CREATE INDEX ON service_capacities (created_at);

