# Quick Fix: Invalid column name 'is_approved'

## Error Message
```
SQL Error: 207, SQLState: S0001
ERROR: Invalid column name 'is_approved'.
```

## Cause
The database doesn't have the new columns we added to the FAQ entity.

## Quick Fix (3 Steps)

### Step 1: Open SQL Server Management Studio
Connect to your SQL Server database.

### Step 2: Run This SQL Script

```sql
-- Add missing columns
ALTER TABLE faq ADD is_approved BIT NOT NULL DEFAULT 0;
ALTER TABLE faq ADD created_by BIGINT NULL;
ALTER TABLE faq ADD approved_by BIGINT NULL;
ALTER TABLE faq ADD approved_at DATETIME NULL;

-- Add foreign keys
ALTER TABLE faq
ADD CONSTRAINT FK_faq_created_by_users
FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE faq
ADD CONSTRAINT FK_faq_approved_by_users
FOREIGN KEY (approved_by) REFERENCES users(id);

-- Optional: Approve all existing FAQs so they're visible
-- UPDATE faq SET is_approved = 1;
```

### Step 3: Restart Application

```bash
# Stop the application (Ctrl+C)
# Then restart
mvn spring-boot:run
```

## Done!

The application should now work correctly.

---

## Full Migration Script

For a complete, safe migration script, see:
- `database_migration_faq_approval.sql`
- `DATABASE_MIGRATION_GUIDE.md`
