-- ============================================
-- ENABLE SUPABASE REALTIME (Early Access Fix)
-- ============================================
-- Run this in Supabase SQL Editor
-- ============================================

-- STEP 1: Check current publication status
SELECT 
  schemaname, 
  tablename 
FROM pg_publication_tables 
WHERE pubname = 'supabase_realtime';

-- If the above returns empty or is missing "messages", continue:

-- STEP 2: Recreate the publication with explicit table list
DROP PUBLICATION IF EXISTS supabase_realtime CASCADE;

CREATE PUBLICATION supabase_realtime FOR TABLE 
  messages, 
  profiles, 
  chats, 
  chat_participants;

-- STEP 3: Verify it worked
SELECT 
  schemaname, 
  tablename,
  pubname
FROM pg_publication_tables 
WHERE pubname = 'supabase_realtime'
ORDER BY tablename;

-- You should see:
-- public | chat_participants | supabase_realtime
-- public | chats            | supabase_realtime
-- public | messages         | supabase_realtime
-- public | profiles         | supabase_realtime

-- STEP 4: Enable realtime for REPLICA IDENTITY
-- This is critical for realtime to work!
ALTER TABLE messages REPLICA IDENTITY FULL;
ALTER TABLE profiles REPLICA IDENTITY FULL;
ALTER TABLE chats REPLICA IDENTITY FULL;
ALTER TABLE chat_participants REPLICA IDENTITY FULL;

-- STEP 5: Verify REPLICA IDENTITY is set
SELECT 
  schemaname,
  tablename,
  relreplident
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
JOIN pg_tables t ON t.tablename = c.relname AND t.schemaname = n.nspname
WHERE tablename IN ('messages', 'profiles', 'chats', 'chat_participants')
AND schemaname = 'public';

-- relreplident should be 'f' (FULL) for all tables

-- ============================================
-- ALTERNATIVE: If publication doesn't work
-- Use this to add tables one by one:
-- ============================================

-- First check if publication exists
SELECT * FROM pg_publication WHERE pubname = 'supabase_realtime';

-- If it exists, add tables individually:
-- ALTER PUBLICATION supabase_realtime ADD TABLE messages;
-- ALTER PUBLICATION supabase_realtime ADD TABLE profiles;
-- ALTER PUBLICATION supabase_realtime ADD TABLE chats;
-- ALTER PUBLICATION supabase_realtime ADD TABLE chat_participants;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Check what's being replicated:
SELECT 
  c.relname as table_name,
  c.relreplident as replica_identity
FROM pg_class c
JOIN pg_publication_rel pr ON pr.prrelid = c.oid
JOIN pg_publication p ON p.oid = pr.prpubid
WHERE p.pubname = 'supabase_realtime';

-- Should show:
-- messages      | f
-- profiles      | f
-- chats         | f
-- chat_participants | f

-- ============================================
-- AFTER RUNNING THIS SQL:
-- ============================================
-- 1. Wait 30 seconds for changes to propagate
-- 2. Restart your app
-- 3. Test real-time messaging
-- 4. Check logs: adb logcat | findstr "ChatRepository"
-- 5. Should see: "✓ Channel subscribed successfully!"
-- ============================================
