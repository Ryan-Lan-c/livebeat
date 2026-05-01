-- ============================================================
-- auth.user_profiles
-- ============================================================
CREATE TABLE auth.user_profiles (
    user_id    UUID         PRIMARY KEY,
    avatar_url VARCHAR(500),
    bio        TEXT,
    phone      VARCHAR(20),
    birth_date DATE,
    address    TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id)
        REFERENCES auth.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  auth.user_profiles            IS 'Extended profile for all users | 所有使用者的擴充個人資料';
COMMENT ON COLUMN auth.user_profiles.user_id    IS 'PK / FK to auth.users | 主鍵，關聯至使用者';
COMMENT ON COLUMN auth.user_profiles.avatar_url IS 'Avatar image URL | 頭像圖片 URL';
COMMENT ON COLUMN auth.user_profiles.bio        IS 'Short self-introduction | 簡介';
COMMENT ON COLUMN auth.user_profiles.phone      IS 'Contact phone number | 聯絡電話';
COMMENT ON COLUMN auth.user_profiles.birth_date IS 'Date of birth for age verification | 生日';
COMMENT ON COLUMN auth.user_profiles.address    IS 'Mailing address for ticket delivery | 居住地址';
COMMENT ON COLUMN auth.user_profiles.created_at IS 'Creation timestamp (UTC) | 建立時間';
COMMENT ON COLUMN auth.user_profiles.updated_at IS 'Last update timestamp (UTC) | 最後更新時間';

-- ============================================================
-- auth.organizer_profiles
-- ============================================================
CREATE TABLE auth.organizer_profiles (
    user_id          UUID         PRIMARY KEY,
    company_name     VARCHAR(255),
    company_tax_id   VARCHAR(20),
    contact_person   VARCHAR(100),
    contact_phone    VARCHAR(20),
    description      TEXT,
    website          VARCHAR(500),
    contact_email    VARCHAR(255),
    is_blacklisted   BOOLEAN      NOT NULL DEFAULT FALSE,
    blacklist_reason TEXT,
    blacklisted_at   TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_organizer_profiles_user FOREIGN KEY (user_id)
        REFERENCES auth.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  auth.organizer_profiles                  IS 'Business profile for ORGANIZER users | 主辦方業務資料';
COMMENT ON COLUMN auth.organizer_profiles.user_id          IS 'PK / FK to auth.users (role=ORGANIZER) | 主鍵，關聯至主辦方使用者';
COMMENT ON COLUMN auth.organizer_profiles.company_name     IS 'Legal or display company name | 公司名稱';
COMMENT ON COLUMN auth.organizer_profiles.company_tax_id   IS 'Company tax ID (統一編號) for invoice | 統一編號';
COMMENT ON COLUMN auth.organizer_profiles.contact_person   IS 'Primary contact person name | 主要聯絡人姓名';
COMMENT ON COLUMN auth.organizer_profiles.contact_phone    IS 'Contact phone number | 聯絡電話';
COMMENT ON COLUMN auth.organizer_profiles.description      IS 'Organizer introduction shown on public pages | 主辦方公開介紹';
COMMENT ON COLUMN auth.organizer_profiles.website          IS 'Official website URL | 官方網站';
COMMENT ON COLUMN auth.organizer_profiles.contact_email    IS 'Public contact email | 公開聯絡信箱';
COMMENT ON COLUMN auth.organizer_profiles.is_blacklisted   IS 'Whether this organizer is blacklisted | 是否列入黑名單';
COMMENT ON COLUMN auth.organizer_profiles.blacklist_reason IS 'Reason for blacklisting | 黑名單原因';
COMMENT ON COLUMN auth.organizer_profiles.blacklisted_at   IS 'Timestamp when blacklisted | 列入黑名單時間';
COMMENT ON COLUMN auth.organizer_profiles.created_at       IS 'Creation timestamp (UTC) | 建立時間';
COMMENT ON COLUMN auth.organizer_profiles.updated_at       IS 'Last update timestamp (UTC) | 最後更新時間';
