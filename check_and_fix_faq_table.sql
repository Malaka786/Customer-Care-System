-- Check and Fix FAQ Table Script
-- This script checks what columns exist and only adds missing ones

PRINT '=== Checking FAQ Table Structure ===';
GO

-- Check current columns
PRINT 'Current FAQ table columns:';
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'faq'
ORDER BY ORDINAL_POSITION;
GO

PRINT '';
PRINT '=== Adding Missing Columns ===';
GO

-- Check and add is_approved
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'is_approved'
)
BEGIN
    ALTER TABLE faq ADD is_approved BIT NOT NULL DEFAULT 0;
    PRINT '✓ Added column: is_approved';
END
ELSE
BEGIN
    PRINT '→ Column is_approved already exists - skipping';
END
GO

-- Check and add created_by
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'created_by'
)
BEGIN
    ALTER TABLE faq ADD created_by BIGINT NULL;
    PRINT '✓ Added column: created_by';
END
ELSE
BEGIN
    PRINT '→ Column created_by already exists - skipping';
END
GO

-- Check and add approved_by
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'approved_by'
)
BEGIN
    ALTER TABLE faq ADD approved_by BIGINT NULL;
    PRINT '✓ Added column: approved_by';
END
ELSE
BEGIN
    PRINT '→ Column approved_by already exists - skipping';
END
GO

-- Check and add approved_at
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'approved_at'
)
BEGIN
    ALTER TABLE faq ADD approved_at DATETIME NULL;
    PRINT '✓ Added column: approved_at';
END
ELSE
BEGIN
    PRINT '→ Column approved_at already exists - skipping';
END
GO

PRINT '';
PRINT '=== Adding Foreign Key Constraints ===';
GO

-- Check and add foreign key for created_by
IF NOT EXISTS (
    SELECT * FROM sys.foreign_keys 
    WHERE name = 'FK_faq_created_by_users'
)
BEGIN
    ALTER TABLE faq
    ADD CONSTRAINT FK_faq_created_by_users
    FOREIGN KEY (created_by) REFERENCES users(id);
    PRINT '✓ Added foreign key: FK_faq_created_by_users';
END
ELSE
BEGIN
    PRINT '→ Foreign key FK_faq_created_by_users already exists - skipping';
END
GO

-- Check and add foreign key for approved_by
IF NOT EXISTS (
    SELECT * FROM sys.foreign_keys 
    WHERE name = 'FK_faq_approved_by_users'
)
BEGIN
    ALTER TABLE faq
    ADD CONSTRAINT FK_faq_approved_by_users
    FOREIGN KEY (approved_by) REFERENCES users(id);
    PRINT '✓ Added foreign key: FK_faq_approved_by_users';
END
ELSE
BEGIN
    PRINT '→ Foreign key FK_faq_approved_by_users already exists - skipping';
END
GO

PRINT '';
PRINT '=== Optional: Approve Existing FAQs ===';
PRINT 'To make existing FAQs visible to customers, uncomment and run the following:';
PRINT '-- UPDATE faq SET is_approved = 1 WHERE is_approved = 0;';
GO

PRINT '';
PRINT '=== Verification ===';
GO

-- Show updated table structure
PRINT 'Updated FAQ table structure:';
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'faq'
ORDER BY ORDINAL_POSITION;
GO

-- Show sample data
PRINT '';
PRINT 'Sample data from FAQ table:';
SELECT TOP 5
    id,
    category,
    LEFT(question, 50) as question_preview,
    is_approved,
    created_by,
    approved_by,
    approved_at,
    created_at
FROM faq
ORDER BY created_at DESC;
GO

PRINT '';
PRINT '=== Migration Complete! ===';
PRINT 'Next steps:';
PRINT '1. Review the results above';
PRINT '2. Optionally approve existing FAQs';
PRINT '3. Restart your Spring Boot application';
GO
