# Fix: Column 'is_approved' Already Exists Error

## Error Message
```
Msg 2705, Level 16, State 4, Line 3
Column names in each table must be unique. 
Column name 'is_approved' in table 'faq' is specified more than once.
```

## What This Means
The `is_approved` column already exists in your FAQ table, so trying to add it again causes an error.

## Solution: Run Diagnostic First

### Step 1: Check What Exists

Open SQL Server Management Studio and run this script:

**File:** `diagnose_faq_table.sql`

This will show you:
- ✓ Which columns already exist
- ✗ Which columns are missing
- Current table structure

### Step 2: Add Only Missing Columns

Run this safe script that checks before adding:

**File:** `check_and_fix_faq_table.sql`

This script:
- Checks if each column exists
- Only adds missing columns
- Skips columns that already exist
- Safe to run multiple times

### Step 3: Approve Existing FAQs (Optional)

If you want existing FAQ articles to be visible to customers:

```sql
UPDATE faq SET is_approved = 1;
```

If you want them to require approval, skip this step.

### Step 4: Restart Application

```bash
# Stop the application (Ctrl+C in terminal)
# Then restart
mvn spring-boot:run
```

## Quick Copy-Paste Solution

If you just want to run one script, copy and paste this into SSMS:

```sql
-- Only add columns that don't exist
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'is_approved')
    ALTER TABLE faq ADD is_approved BIT NOT NULL DEFAULT 0;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'created_by')
    ALTER TABLE faq ADD created_by BIGINT NULL;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'approved_by')
    ALTER TABLE faq ADD approved_by BIGINT NULL;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'approved_at')
    ALTER TABLE faq ADD approved_at DATETIME NULL;

-- Add foreign keys if they don't exist
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_faq_created_by_users')
    ALTER TABLE faq ADD CONSTRAINT FK_faq_created_by_users FOREIGN KEY (created_by) REFERENCES users(id);

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_faq_approved_by_users')
    ALTER TABLE faq ADD CONSTRAINT FK_faq_approved_by_users FOREIGN KEY (approved_by) REFERENCES users(id);

-- Optional: Approve all existing FAQs
-- UPDATE faq SET is_approved = 1;

PRINT 'Migration complete!';
```

## Files Created

I've created 3 SQL scripts for you:

1. **diagnose_faq_table.sql** - Check current table status
2. **check_and_fix_faq_table.sql** - Safe migration that checks first
3. **database_migration_faq_approval.sql** - Original full script

## Recommended Order

1. Run `diagnose_faq_table.sql` first
2. Look at the output to see what's missing
3. Run `check_and_fix_faq_table.sql` to add missing columns
4. Restart your application

## Troubleshooting

### If you get "Column already exists" errors
- The column was partially added before
- Use the diagnostic script to see what exists
- Use the check_and_fix script which is safe to re-run

### If foreign key fails
```sql
-- Check if users table exists
SELECT * FROM sys.tables WHERE name = 'users';

-- If it doesn't exist or has a different name, skip foreign keys for now
-- The application will still work, just without referential integrity
```

### If application still shows errors after migration
1. Verify columns exist: Run `diagnose_faq_table.sql`
2. Restart application completely
3. Clear compiled files:
   ```bash
   mvn clean
   mvn spring-boot:run
   ```

---

**Next Step:** Run `diagnose_faq_table.sql` to see your current table structure!
