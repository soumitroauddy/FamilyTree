-- Users table (created first; family_id FK added after families table)
CREATE TABLE users (
    id               TEXT PRIMARY KEY,
    username         TEXT UNIQUE,
    password_hash    TEXT,
    email            TEXT NOT NULL,
    display_name     TEXT NOT NULL,
    auth_provider    TEXT NOT NULL DEFAULT 'LOCAL',
    provider_user_id TEXT,
    family_id        TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Families table (references users.id for owner)
CREATE TABLE families (
    id            TEXT PRIMARY KEY,
    name          TEXT NOT NULL,
    join_code     TEXT UNIQUE NOT NULL,
    owner_user_id TEXT NOT NULL REFERENCES users(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Add FK from users.family_id -> families.id now that families exists
ALTER TABLE users
    ADD CONSTRAINT fk_users_family FOREIGN KEY (family_id) REFERENCES families(id);

-- Family member nodes (people in the tree)
CREATE TABLE family_members (
    id               TEXT PRIMARY KEY,
    family_id        TEXT NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    added_by_user_id TEXT NOT NULL REFERENCES users(id),
    full_name        TEXT NOT NULL,
    photo_url        TEXT,
    birth_year       INT,
    death_year       INT,
    bio              TEXT,
    location         TEXT,
    parent_member_id TEXT REFERENCES family_members(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Invitations for new family members
CREATE TABLE invitations (
    id              TEXT PRIMARY KEY,
    family_id       TEXT NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    inviter_user_id TEXT NOT NULL REFERENCES users(id),
    invite_code     TEXT UNIQUE NOT NULL,
    invitee_email   TEXT,
    status          TEXT NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_users_family_id ON users(family_id);
CREATE INDEX idx_family_members_family_id ON family_members(family_id);
CREATE INDEX idx_invitations_invite_code ON invitations(invite_code);
