-- Diagnostic Script: Check FAQ Table Status
-- Run this FIRST to see what columns already exist

PRINT '=========================================';
PRINT '  FAQ Table Diagnostic Report';
PRINT '=========================================';
PRINT '';

-- Check if FAQ table exists
IF OBJECT_ID('faq', 'U') IS NOT NULL
BEGIN
    PRINT '✓ FAQ table exists';
    PRINT '';
    
    -- Show all columns in FAQ table
    PRINT 'Current columns in FAQ table:';
    PRINT '--------------------------------------------';
    SELECT 
        ORDINAL_POSITION as [#],
        COLUMN_NAME as [Column],
        DATA_TYPE as [Type],
        CHARACTER_MAXIMUM_LENGTH as [Length],
        IS_NULLABLE as [Nullable],
        COLUMN_DEFAULT as [Default]
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'faq'
    ORDER BY ORDINAL_POSITION;
    PRINT '';
    
    -- Check specifically for new columns
    PRINT 'Checking for approval system columns:';
    PRINT '--------------------------------------------';
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'is_approved')
        PRINT '✓ is_approved column EXISTS'
    ELSE
        PRINT '✗ is_approved column MISSING (needs to be added)';
        
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'created_by')
        PRINT '✓ created_by column EXISTS'
    ELSE
        PRINT '✗ created_by column MISSING (needs to be added)';
        
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'approved_by')
        PRINT '✓ approved_by column EXISTS'
    ELSE
        PRINT '✗ approved_by column MISSING (needs to be added)';
        
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'faq' AND COLUMN_NAME = 'approved_at')
        PRINT '✓ approved_at column EXISTS'
    ELSE
        PRINT '✗ approved_at column MISSING (needs to be added)';
    
    PRINT '';
    
    -- Check foreign keys
    PRINT 'Checking for foreign key constraints:';
    PRINT '--------------------------------------------';
    
    IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_faq_created_by_users')
        PRINT '✓ FK_faq_created_by_users EXISTS'
    ELSE
        PRINT '✗ FK_faq_created_by_users MISSING (needs to be added)';
        
    IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_faq_approved_by_users')
        PRINT '✓ FK_faq_approved_by_users EXISTS'
    ELSE
        PRINT '✗ FK_faq_approved_by_users MISSING (needs to be added)';
    
    PRINT '';
    
    -- Show sample data
    PRINT 'Sample FAQ data (first 3 rows):';
    PRINT '--------------------------------------------';
    SELECT TOP 3 * FROM faq;
    
    PRINT '';
    PRINT 'FAQ record count: ';
    SELECT COUNT(*) as [Total FAQs] FROM faq;
    
END
ELSE
BEGIN
    PRINT '✗ FAQ table does NOT exist!';
    PRINT 'ERROR: The FAQ table needs to be created first.';
END

PRINT '';
PRINT '=========================================';
PRINT '  End of Diagnostic Report';
PRINT '=========================================';
PRINT '';
PRINT 'Based on the results above:';
PRINT '1. If columns are MISSING, run: check_and_fix_faq_table.sql';
PRINT '2. If columns already EXISTS, your database is ready!';
PRINT '3. Restart your Spring Boot application';
