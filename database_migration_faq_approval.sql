-- SQL Migration Script for FAQ Approval System
-- Run this script on your Microsoft SQL Server database

-- NOTE: Replace 'your_database_name' with your actual database name
-- Example: USE [customer_care_db];
-- USE [your_database_name];
-- GO

-- If you don't need to switch database, comment out the above lines

-- Add is_approved column (default FALSE/0)
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[faq]') 
               AND name = 'is_approved')
BEGIN
    ALTER TABLE [dbo].[faq]
    ADD [is_approved] BIT NOT NULL DEFAULT 0;
    
    PRINT 'Column is_approved added successfully';
END
ELSE
BEGIN
    PRINT 'Column is_approved already exists';
END
GO

-- Add created_by column (foreign key to users table)
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[faq]') 
               AND name = 'created_by')
BEGIN
    ALTER TABLE [dbo].[faq]
    ADD [created_by] BIGINT NULL;
    
    PRINT 'Column created_by added successfully';
END
ELSE
BEGIN
    PRINT 'Column created_by already exists';
END
GO

-- Add approved_by column (foreign key to users table)
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[faq]') 
               AND name = 'approved_by')
BEGIN
    ALTER TABLE [dbo].[faq]
    ADD [approved_by] BIGINT NULL;
    
    PRINT 'Column approved_by added successfully';
END
ELSE
BEGIN
    PRINT 'Column approved_by already exists';
END
GO

-- Add approved_at column
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[faq]') 
               AND name = 'approved_at')
BEGIN
    ALTER TABLE [dbo].[faq]
    ADD [approved_at] DATETIME NULL;
    
    PRINT 'Column approved_at added successfully';
END
ELSE
BEGIN
    PRINT 'Column approved_at already exists';
END
GO

-- Add foreign key constraint for created_by
IF NOT EXISTS (SELECT * FROM sys.foreign_keys 
               WHERE name = 'FK_faq_created_by_users')
BEGIN
    ALTER TABLE [dbo].[faq]
    ADD CONSTRAINT FK_faq_created_by_users
    FOREIGN KEY (created_by) REFERENCES [dbo].[users](id);
    
    PRINT 'Foreign key FK_faq_created_by_users added successfully';
END
ELSE
BEGIN
    PRINT 'Foreign key FK_faq_created_by_users already exists';
END
GO

-- Add foreign key constraint for approved_by
IF NOT EXISTS (SELECT * FROM sys.foreign_keys 
               WHERE name = 'FK_faq_approved_by_users')
BEGIN
    ALTER TABLE [dbo].[faq]
    ADD CONSTRAINT FK_faq_approved_by_users
    FOREIGN KEY (approved_by) REFERENCES [dbo].[users](id);
    
    PRINT 'Foreign key FK_faq_approved_by_users added successfully';
END
ELSE
BEGIN
    PRINT 'Foreign key FK_faq_approved_by_users already exists';
END
GO

-- Update existing FAQs to be approved (optional - uncomment if you want existing FAQs to be visible)
-- UPDATE [dbo].[faq]
-- SET is_approved = 1
-- WHERE is_approved = 0;
-- GO

-- Verify the changes
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'faq'
ORDER BY ORDINAL_POSITION;
GO

PRINT 'FAQ table migration completed successfully!';
GO
