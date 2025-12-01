-- Fix user_roles check constraint to allow ADMIN, STAFF, CUSTOMER
-- Drop existing constraint if it exists
ALTER TABLE user_roles 
DROP CONSTRAINT IF EXISTS user_roles_roles_check;

-- Create new constraint that allows all three roles
ALTER TABLE user_roles 
ADD CONSTRAINT user_roles_roles_check 
CHECK (roles IN ('ADMIN', 'STAFF', 'CUSTOMER'));

