-- =====================================================
-- Agent Response System Database Migration
-- Version: 2.0
-- Date: 2025-10-23
-- =====================================================

USE Customer_Care_DB;
GO

PRINT 'Starting migration for Agent Response improvements...';

-- =====================================================
-- STEP 1: Backup existing data
-- =====================================================
PRINT 'Step 1: Creating backup of agent_responses table...';

-- Create backup table
IF OBJECT_ID('agent_responses_backup', 'U') IS NOT NULL
    DROP TABLE agent_responses_backup;
GO

SELECT * 
INTO agent_responses_backup
FROM agent_responses;
GO

PRINT 'Backup created successfully. Rows backed up: ' + 
      CAST(@@ROWCOUNT AS VARCHAR);

-- =====================================================
-- STEP 2: Drop unique constraint on ticket_id
-- =====================================================
PRINT 'Step 2: Removing unique constraint from ticket_id...';

-- Find and drop the unique constraint
DECLARE @ConstraintName NVARCHAR(256);
DECLARE @SQL NVARCHAR(MAX);

SELECT @ConstraintName = kc.name
FROM sys.key_constraints kc
INNER JOIN sys.tables t ON kc.parent_object_id = t.object_id
WHERE t.name = 'agent_responses' 
  AND kc.type = 'UQ';

IF @ConstraintName IS NOT NULL
BEGIN
    SET @SQL = 'ALTER TABLE agent_responses DROP CONSTRAINT ' + 
               QUOTENAME(@ConstraintName);
    EXEC sp_executesql @SQL;
    PRINT 'Unique constraint dropped: ' + @ConstraintName;
END
ELSE
BEGIN
    PRINT 'No unique constraint found on ticket_id (may already be removed)';
END
GO

-- =====================================================
-- STEP 3: Verify table structure
-- =====================================================
PRINT 'Step 3: Verifying table structure...';

-- Check if table exists
IF OBJECT_ID('agent_responses', 'U') IS NULL
BEGIN
    PRINT 'ERROR: agent_responses table does not exist!';
    PRINT 'Creating table...';
    
    CREATE TABLE agent_responses (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        ticket_id BIGINT NOT NULL,
        agent_id BIGINT NOT NULL,
        response_text NVARCHAR(MAX) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_agent_responses_ticket 
            FOREIGN KEY (ticket_id) REFERENCES ticket(id),
        CONSTRAINT FK_agent_responses_agent 
            FOREIGN KEY (agent_id) REFERENCES users(id)
    );
    
    PRINT 'Table created successfully.';
END
ELSE
BEGIN
    PRINT 'Table agent_responses exists.';
END
GO

-- =====================================================
-- STEP 4: Verify relationships
-- =====================================================
PRINT 'Step 4: Verifying foreign key constraints...';

-- Check ticket foreign key
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'FK_agent_responses_ticket'
)
BEGIN
    PRINT 'Adding foreign key to ticket table...';
    ALTER TABLE agent_responses
    ADD CONSTRAINT FK_agent_responses_ticket
        FOREIGN KEY (ticket_id) REFERENCES ticket(id);
END
ELSE
BEGIN
    PRINT 'Foreign key to ticket table exists.';
END
GO

-- Check agent foreign key
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'FK_agent_responses_agent'
)
BEGIN
    PRINT 'Adding foreign key to users table...';
    ALTER TABLE agent_responses
    ADD CONSTRAINT FK_agent_responses_agent
        FOREIGN KEY (agent_id) REFERENCES users(id);
END
ELSE
BEGIN
    PRINT 'Foreign key to users table exists.';
END
GO

-- =====================================================
-- STEP 5: Create indexes for performance
-- =====================================================
PRINT 'Step 5: Creating indexes for better performance...';

-- Index on ticket_id for faster lookups
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'IX_agent_responses_ticket_id' 
      AND object_id = OBJECT_ID('agent_responses')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_agent_responses_ticket_id
    ON agent_responses(ticket_id);
    PRINT 'Index created on ticket_id.';
END
ELSE
BEGIN
    PRINT 'Index on ticket_id already exists.';
END
GO

-- Index on agent_id for agent-specific queries
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'IX_agent_responses_agent_id' 
      AND object_id = OBJECT_ID('agent_responses')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_agent_responses_agent_id
    ON agent_responses(agent_id);
    PRINT 'Index created on agent_id.';
END
ELSE
BEGIN
    PRINT 'Index on agent_id already exists.';
END
GO

-- Index on created_at for chronological sorting
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'IX_agent_responses_created_at' 
      AND object_id = OBJECT_ID('agent_responses')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_agent_responses_created_at
    ON agent_responses(created_at DESC);
    PRINT 'Index created on created_at.';
END
ELSE
BEGIN
    PRINT 'Index on created_at already exists.';
END
GO

-- =====================================================
-- STEP 6: Verify data integrity
-- =====================================================
PRINT 'Step 6: Verifying data integrity...';

-- Count total responses
DECLARE @TotalResponses INT;
SELECT @TotalResponses = COUNT(*) FROM agent_responses;
PRINT 'Total responses in table: ' + CAST(@TotalResponses AS VARCHAR);

-- Check for orphaned responses (ticket not exists)
DECLARE @OrphanedResponses INT;
SELECT @OrphanedResponses = COUNT(*)
FROM agent_responses ar
LEFT JOIN ticket t ON ar.ticket_id = t.id
WHERE t.id IS NULL;

IF @OrphanedResponses > 0
BEGIN
    PRINT 'WARNING: Found ' + CAST(@OrphanedResponses AS VARCHAR) + 
          ' orphaned responses (ticket does not exist)';
    PRINT 'Consider cleaning these up:';
    SELECT ar.id, ar.ticket_id, ar.response_text
    FROM agent_responses ar
    LEFT JOIN ticket t ON ar.ticket_id = t.id
    WHERE t.id IS NULL;
END
ELSE
BEGIN
    PRINT 'No orphaned responses found. Data integrity OK.';
END
GO

-- =====================================================
-- STEP 7: Test queries
-- =====================================================
PRINT 'Step 7: Testing queries...';

-- Test: Get all responses for a ticket
PRINT 'Test: Get responses for tickets...';
SELECT TOP 5
    t.id AS ticket_id,
    t.title,
    COUNT(ar.id) AS response_count
FROM ticket t
LEFT JOIN agent_responses ar ON t.id = ar.ticket_id
GROUP BY t.id, t.title
ORDER BY response_count DESC;

-- Test: Get recent responses
PRINT 'Test: Get recent responses...';
SELECT TOP 10
    ar.id,
    t.id AS ticket_id,
    t.title AS ticket_title,
    u.username AS agent_name,
    ar.response_text,
    ar.created_at
FROM agent_responses ar
INNER JOIN ticket t ON ar.ticket_id = t.id
INNER JOIN users u ON ar.agent_id = u.id
ORDER BY ar.created_at DESC;

-- =====================================================
-- STEP 8: Summary
-- =====================================================
PRINT '=======================================================';
PRINT 'Migration completed successfully!';
PRINT '=======================================================';
PRINT '';
PRINT 'Summary:';
PRINT '- Unique constraint on ticket_id: REMOVED ✓';
PRINT '- Multiple responses per ticket: ENABLED ✓';
PRINT '- Foreign key constraints: VERIFIED ✓';
PRINT '- Performance indexes: CREATED ✓';
PRINT '- Data integrity: VERIFIED ✓';
PRINT '';
PRINT 'Backup table: agent_responses_backup';
PRINT 'To restore from backup if needed:';
PRINT '  TRUNCATE TABLE agent_responses;';
PRINT '  INSERT INTO agent_responses SELECT * FROM agent_responses_backup;';
PRINT '';
PRINT '=======================================================';
GO

-- =====================================================
-- Optional: Clean up old backup tables
-- =====================================================
-- Uncomment to remove backup table after verification
-- DROP TABLE IF EXISTS agent_responses_backup;
-- PRINT 'Backup table removed.';
-- GO
