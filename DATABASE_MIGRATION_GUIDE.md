# Database Migration Guide - FAQ Approval System

## Problem
The error `Invalid column name 'is_approved'` occurs because the database schema wasn't updated when we added new fields to the FAQ entity.

## Solution
Run the SQL migration script to add the missing columns.

## Migration Steps

### Option 1: Using SQL Server Management Studio (SSMS)

1. **Open SQL Server Management Studio**
   - Connect to your SQL Server instance

2. **Open the migration script**
   - Go to: File → Open → File
   - Select: `database_migration_faq_approval.sql`

3. **Update database name**
   - Find line: `USE [your_database_name];`
   - Replace `your_database_name` with your actual database name
   - Example: `USE [customer_care_db];`

4. **Execute the script**
   - Click "Execute" button (or press F5)
   - Check Messages tab for success confirmation

5. **Verify the changes**
   - Refresh the database tables
   - Right-click on `faq` table → Design
   - Verify these columns exist:
     - `is_approved` (bit, not null, default: 0)
     - `created_by` (bigint, null)
     - `approved_by` (bigint, null)
     - `approved_at` (datetime, null)

### Option 2: Using Azure Data Studio

1. **Open Azure Data Studio**
   - Connect to your SQL Server

2. **Create new query**
   - Click "New Query" button

3. **Paste the migration script**
   - Copy content from `database_migration_faq_approval.sql`
   - Paste into query window

4. **Update database name**
   - Change `[your_database_name]` to your actual database

5. **Run the script**
   - Click "Run" button
   - Check output for success messages

### Option 3: Command Line (sqlcmd)

```bash
# Replace with your connection details
sqlcmd -S localhost -d your_database_name -U your_username -P your_password -i database_migration_faq_approval.sql
```

## What the Migration Does

### Adds 4 New Columns:

1. **is_approved** (BIT, NOT NULL, DEFAULT 0)
   - Tracks whether FAQ is approved
   - Default: FALSE (0) - requires admin approval

2. **created_by** (BIGINT, NULL)
   - Foreign key to users table
   - Tracks which agent created the FAQ

3. **approved_by** (BIGINT, NULL)
   - Foreign key to users table
   - Tracks which admin approved the FAQ

4. **approved_at** (DATETIME, NULL)
   - Timestamp of when FAQ was approved

### Adds 2 Foreign Key Constraints:

1. **FK_faq_created_by_users**
   - Links `created_by` to `users.id`

2. **FK_faq_approved_by_users**
   - Links `approved_by` to `users.id`

## Post-Migration Steps

### If You Want Existing FAQs to Be Visible:

Uncomment and run this query:
```sql
UPDATE [dbo].[faq]
SET is_approved = 1
WHERE is_approved = 0;
```

This will approve all existing FAQ articles so they're visible to customers.

### If You Want Existing FAQs to Require Approval:

Do nothing - they'll default to `is_approved = 0` and will need admin approval.

## Verification

After running the migration, verify with this query:

```sql
-- Check table structure
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'faq'
ORDER BY ORDINAL_POSITION;

-- Check existing data
SELECT 
    id,
    category,
    question,
    is_approved,
    created_by,
    approved_by,
    approved_at
FROM faq;
```

Expected output should show all 4 new columns.

## Restart Application

After successful migration:

1. **Stop the Spring Boot application** (if running)
2. **Restart the application**
   ```bash
   mvn spring-boot:run
   ```
3. **Test the functionality**
   - Agent creates FAQ → should be pending
   - Admin views `/admin/faq/pending` → should see pending FAQs
   - Admin approves → should be visible to customers

## Troubleshooting

### Error: "Invalid object name 'users'"
**Solution:** The users table doesn't exist. Check your table name:
```sql
SELECT name FROM sys.tables WHERE name LIKE '%user%';
```
Update foreign key constraints with correct table name.

### Error: "Cannot add foreign key constraint"
**Solution:** Check that referenced user IDs exist:
```sql
-- Remove existing created_by values that don't match users
UPDATE faq SET created_by = NULL WHERE created_by NOT IN (SELECT id FROM users);

-- Then add the foreign key
ALTER TABLE faq
ADD CONSTRAINT FK_faq_created_by_users
FOREIGN KEY (created_by) REFERENCES users(id);
```

### Error: "Column already exists"
**Solution:** Script is safe to re-run. It checks for existing columns before adding.

### Application Still Shows Error After Migration
**Solutions:**
1. Restart the application completely
2. Clear Hibernate cache:
   - Delete `target` folder
   - Run `mvn clean`
   - Rebuild: `mvn clean install`
3. Check database connection in `application.properties`

## Rollback (if needed)

If you need to revert the changes:

```sql
-- Drop foreign keys first
ALTER TABLE faq DROP CONSTRAINT FK_faq_created_by_users;
ALTER TABLE faq DROP CONSTRAINT FK_faq_approved_by_users;

-- Drop columns
ALTER TABLE faq DROP COLUMN is_approved;
ALTER TABLE faq DROP COLUMN created_by;
ALTER TABLE faq DROP COLUMN approved_by;
ALTER TABLE faq DROP COLUMN approved_at;
```

**Warning:** This will permanently delete approval data!

## Alternative: Enable Hibernate Auto-Update (Not Recommended for Production)

If you want Hibernate to auto-create columns (only for development):

**File:** `src/main/resources/application.properties`

```properties
# Add this line (ONLY FOR DEVELOPMENT!)
spring.jpa.hibernate.ddl-auto=update
```

**Risks:**
- ⚠️ Can cause data loss
- ⚠️ Not suitable for production
- ⚠️ May create incorrect schema
- ✅ Manual migration is safer

## Summary

1. ✅ Run `database_migration_faq_approval.sql`
2. ✅ Verify columns were added
3. ✅ Optionally approve existing FAQs
4. ✅ Restart application
5. ✅ Test FAQ approval workflow

---
**Date:** 2025-10-23  
**Status:** Migration Required  
**Priority:** High - Application won't work without this migration
