# Database Reset Instructions

## Problem

If you encounter the following error during application startup:

```
ERROR: column "tenant_id" of relation "contacts" contains null values
```

This means the database is in an inconsistent state due to a failed migration.

## Solution

You need to reset the database to allow Liquibase to run all migrations from scratch.

### Option 1: Using the Reset Script (Recommended)

```bash
./scripts/reset-database.sh
```

This script will:
1. Drop the existing database
2. Create a new empty database
3. Let Liquibase recreate all tables

### Option 2: Manual Reset

If you prefer to do it manually:

```bash
# Connect to PostgreSQL
psql -U postgres

# Drop and recreate the database
DROP DATABASE mailist_dev;
CREATE DATABASE mailist_dev;

# Exit psql
\q
```

### Option 3: Using Docker (if using Docker PostgreSQL)

```bash
# Stop and remove the PostgreSQL container
docker-compose down -v

# Start PostgreSQL again
docker-compose up -d postgres

# Wait a few seconds for PostgreSQL to start
sleep 5
```

## After Reset

Once the database is reset, simply run the application:

```bash
./mvnw spring-boot:run
```

or if using your IDE, just run the `MailistApplication` main class.

Liquibase will automatically:
1. Create all tables
2. Add indexes
3. Insert sample data (in dev environment)
4. Apply all migrations in the correct order

## What Was Fixed

The migrations have been updated to handle existing data properly:

1. **015-add-tenant-id-columns.xml**: Now adds `tenant_id` as nullable first, fills existing rows, then makes it NOT NULL
2. **016-update-users-auth-fields.xml**: Added all new user fields including:
   - Profile fields: avatar, phone, company, timezone, language
   - 2FA fields: two_factor_enabled, two_factor_secret
   - Created `user_preferences` table

## Verification

After the application starts successfully, you can verify the database structure:

```bash
psql -U postgres -d mailist_dev -c "\dt"
```

You should see all tables including:
- users
- user_preferences
- user_roles
- refresh_tokens
- contacts
- contact_lists
- campaigns
- automation_rules
- reports
- organizations

## Need Help?

If you still encounter issues, check:
1. PostgreSQL is running: `pg_isready`
2. Database exists: `psql -U postgres -l | grep mailist_dev`
3. Application.properties has correct database credentials
