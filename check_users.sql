-- Check Users Table and Fix Admin Access

PRINT '=========================================';
PRINT '  User Account Diagnostic';
PRINT '=========================================';
PRINT '';

-- Check if users table exists
IF OBJECT_ID('users', 'U') IS NOT NULL
BEGIN
    PRINT '✓ Users table exists';
    PRINT '';
    
    -- Show all users
    PRINT 'Current users in database:';
    PRINT '--------------------------------------------';
    SELECT 
        id,
        username,
        role,
        phone_number,
        address
    FROM users
    ORDER BY role, username;
    
    PRINT '';
    PRINT 'User count by role:';
    PRINT '--------------------------------------------';
    SELECT 
        role,
        COUNT(*) as [Count]
    FROM users
    GROUP BY role;
    
    PRINT '';
    
    -- Check for admin users
    IF EXISTS (SELECT * FROM users WHERE role LIKE '%ADMIN%')
    BEGIN
        PRINT '✓ Admin users exist:';
        SELECT username, role FROM users WHERE role LIKE '%ADMIN%';
    END
    ELSE
    BEGIN
        PRINT '✗ WARNING: No admin users found!';
        PRINT '';
        PRINT 'You need to create an admin user or promote an existing user.';
    END
    
END
ELSE
BEGIN
    PRINT '✗ Users table does NOT exist!';
END

PRINT '';
PRINT '=========================================';
PRINT '  Recommendations';
PRINT '=========================================';
PRINT '';
PRINT 'If you need to create an admin user, run one of these:';
PRINT '';
PRINT '1. Promote existing user to admin:';
PRINT '   UPDATE users SET role = ''ROLE_ADMIN'' WHERE username = ''your_username'';';
PRINT '';
PRINT '2. Create new admin user:';
PRINT '   INSERT INTO users (username, password, role, phone_number, address)';
PRINT '   VALUES (''admin'', ''$2a$10$...hashed_password...'', ''ROLE_ADMIN'', ''0771234567'', ''Admin Address'');';
PRINT '';
PRINT 'Note: Password must be BCrypt hashed. Use the registration page or hash it manually.';
