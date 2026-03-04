# My Posts Fix - Firestore Index Issue

## Overview
This document addresses the issue where posts created by a user don't appear in the "My Posts" section (내 게시글) of My Page.

## Problem Description

**User Report:**
"Currently, if I write a post using my account, it won't appear in 'My Posts' on My Page."

**Technical Issue:**
When a user creates a post, it gets saved to Firebase correctly with the `authorUid` field. However, when the app tries to load "My Posts" in MyPostsFragment, the query fails silently and returns no results.

**Flow of the Problem:**

```
User creates post → Saved to Firebase with authorUid
                           ↓
              User goes to My Page → 내 게시글
                           ↓
      App queries: .whereEqualTo("authorUid", uid).orderBy("createdAt")
                           ↓
        ❌ Query fails (requires Firestore composite index)
                           ↓
           No posts returned, empty state shown
```

## Root Cause

### The Issue: Missing Firestore Composite Index

**File**: `CommunityRepository.kt`
**Method**: `getMyPosts()`

**Old Code (BROKEN):**
```kotlin
suspend fun getMyPosts(): Result<List<CommunityPost>> {
    return try {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return Result.failure(Exception("User not authenticated"))
        }

        val snapshot = postsCollection
            .whereEqualTo("authorUid", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING) // ❌ REQUIRES INDEX
            .get()
            .await()

        val posts = snapshot.toObjects(CommunityPost::class.java)
        Result.success(posts)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Why This Failed:**
1. Firestore query combines `.whereEqualTo()` with `.orderBy()` on different fields
2. This requires a **composite index** to be created in Firebase Console
3. Without the index, the query either fails or returns no results
4. Error message (if visible): "The query requires an index"

**Same Issue as Product Favorites:**
This is the exact same problem that affected `getFavoritedProducts()` - both methods used `.orderBy()` after a `.whereEqualTo()` without the required index.

## Solution Implemented ✅

### Approach: Remove Firestore Index Requirement

Instead of requiring a Firestore composite index, sort the results in memory after fetching.

### Changes Made:

#### 1. CommunityRepository.kt - Fixed getMyPosts()

**File**: `app/src/main/java/com/yourcompany/re_buy/repository/CommunityRepository.kt`
**Lines**: 417-444

```kotlin
/**
 * Get all posts created by current user
 */
suspend fun getMyPosts(): Result<List<CommunityPost>> {
    return try {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("CommunityRepo", "Cannot get my posts - user not authenticated")
            return Result.failure(Exception("User not authenticated"))
        }

        android.util.Log.d("CommunityRepo", "Getting posts for user: ${currentUser.uid}")

        // No .orderBy() - avoids index requirement
        val snapshot = postsCollection
            .whereEqualTo("authorUid", currentUser.uid)
            .get()
            .await()

        android.util.Log.d("CommunityRepo", "Found ${snapshot.size()} posts by user")

        // Sort in memory to avoid index requirement
        val posts = snapshot.toObjects(CommunityPost::class.java)
            .sortedByDescending { it.createdAt }

        android.util.Log.d("CommunityRepo", "Successfully retrieved ${posts.size} posts by current user")
        Result.success(posts)
    } catch (e: Exception) {
        android.util.Log.e("CommunityRepo", "Error getting my posts: ${e.message}", e)
        Result.failure(e)
    }
}
```

**Changes:**
1. ✅ Removed `.orderBy("createdAt", Query.Direction.DESCENDING)`
2. ✅ Added `.sortedByDescending { it.createdAt }` for in-memory sorting
3. ✅ Added comprehensive logging at each step
4. ✅ Added error logging with stack trace

#### 2. MyPostsFragment.kt - Enhanced Logging

**File**: `app/src/main/java/com/yourcompany/re_buy/MyPostsFragment.kt`
**Lines**: 58-90

```kotlin
private fun loadMyPosts() {
    android.util.Log.d("MyPostsFragment", "Loading my posts...")
    binding.progressBar.visibility = View.VISIBLE

    lifecycleScope.launch {
        val result = repository.getMyPosts()
        binding.progressBar.visibility = View.GONE

        result.onSuccess { posts ->
            android.util.Log.d("MyPostsFragment", "Successfully loaded ${posts.size} posts")
            if (posts.isEmpty()) {
                android.util.Log.d("MyPostsFragment", "No posts found, showing empty state")
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvMyPosts.visibility = View.GONE
            } else {
                android.util.Log.d("MyPostsFragment", "Displaying ${posts.size} posts")
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvMyPosts.visibility = View.VISIBLE
                postAdapter.updatePosts(posts)
            }
        }.onFailure { e ->
            android.util.Log.e("MyPostsFragment", "Failed to load posts: ${e.message}", e)
            Toast.makeText(
                requireContext(),
                "게시글을 불러올 수 없습니다: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvMyPosts.visibility = View.GONE
        }
    }
}
```

**Added Logging:**
- "Loading my posts..." - When loading starts
- "Successfully loaded X posts" - When query succeeds
- "No posts found" - When user has no posts
- "Displaying X posts" - When showing posts
- Error logs with full exception details

## How It Works Now ✅

### Flow After Fix:

```
User creates post → Saved to Firebase with authorUid
                           ↓
              User goes to My Page → 내 게시글
                           ↓
         App queries: .whereEqualTo("authorUid", uid)
                           ↓
              ✅ Query succeeds (no index needed)
                           ↓
        Results sorted in memory: .sortedByDescending { it.createdAt }
                           ↓
           Posts displayed in reverse chronological order
```

### Post Creation Verification:

The post creation process is already correct in `CreatePostActivity.kt`:

```kotlin
val currentUser = auth.currentUser!!
val post = CommunityPost(
    title = title,
    content = content,
    authorUid = currentUser.uid,  // ✅ Correctly set
    authorName = currentUser.displayName ?: currentUser.email ?: "익명",
    authorEmail = currentUser.email ?: "",
    region = region,
    imageUrls = imageUrls
)
```

## Testing Instructions

### Test Scenario 1: Create a Post

1. **Sign in to the app**

2. **Create a new post:**
   - Go to Community tab
   - Click FAB (+ button)
   - Fill in title and content
   - Select region (optional)
   - Click "게시" (Post)

3. **Enable logcat monitoring:**
   ```bash
   adb logcat | findstr "CommunityRepo MyPostsFragment"
   ```

4. **Go to My Page:**
   - Click My Page tab
   - Click "내 게시글" (My Posts) tab

5. **Verify post appears:**
   - ✅ Your new post should be visible
   - ✅ Should be at the top (newest first)

6. **Check logcat:**
   ```
   D/CommunityRepo: Getting posts for user: [YOUR_UID]
   D/CommunityRepo: Found 1 posts by user
   D/CommunityRepo: Successfully retrieved 1 posts by current user
   D/MyPostsFragment: Successfully loaded 1 posts
   D/MyPostsFragment: Displaying 1 posts
   ```

### Test Scenario 2: Multiple Posts

1. **Create 3 posts** (different titles)

2. **Go to My Page → 내 게시글**

3. **Verify all posts appear:**
   - ✅ All 3 posts should be visible
   - ✅ Ordered by creation time (newest first)

4. **Check logcat:**
   ```
   D/CommunityRepo: Found 3 posts by user
   D/CommunityRepo: Successfully retrieved 3 posts by current user
   D/MyPostsFragment: Displaying 3 posts
   ```

### Test Scenario 3: Edit/Delete Posts

1. **Create a post**

2. **Verify it appears in My Page → 내 게시글**

3. **Edit the post:**
   - Click on the post
   - Click edit menu (⋮) → Edit
   - Change title/content
   - Save

4. **Return to My Page → 내 게시글**
   - ✅ Updated post should still appear

5. **Delete the post:**
   - Click on the post
   - Click delete menu (⋮) → Delete
   - Confirm deletion

6. **Return to My Page → 내 게시글**
   - ✅ Post should be gone
   - ✅ Empty state if no other posts

### Expected Logcat Output

**Success - Loading Posts:**
```
D/MyPostsFragment: Loading my posts...
D/CommunityRepo: Getting posts for user: xyz123
D/CommunityRepo: Found 2 posts by user
D/CommunityRepo: Successfully retrieved 2 posts by current user
D/MyPostsFragment: Successfully loaded 2 posts
D/MyPostsFragment: Displaying 2 posts
```

**Success - No Posts Yet:**
```
D/MyPostsFragment: Loading my posts...
D/CommunityRepo: Getting posts for user: xyz123
D/CommunityRepo: Found 0 posts by user
D/CommunityRepo: Successfully retrieved 0 posts by current user
D/MyPostsFragment: Successfully loaded 0 posts
D/MyPostsFragment: No posts found, showing empty state
```

**Error - If Still Failing:**
```
E/CommunityRepo: Error getting my posts: [ERROR MESSAGE]
E/MyPostsFragment: Failed to load posts: [ERROR MESSAGE]
```

## Troubleshooting

### If Posts Still Don't Appear

1. **Verify Post Was Created:**
   - Go to Community tab
   - Check if your post appears in the main feed
   - If not, post creation failed

2. **Check Firebase Console:**
   - Go to Firebase Console → Firestore Database
   - Open "posts" collection
   - Find your post
   - Verify it has `authorUid` field matching your user ID

3. **Check User Authentication:**
   - Make sure you're signed in
   - Go to Firebase Console → Authentication → Users
   - Find your user account
   - Note the UID

4. **Check Logcat for Errors:**
   ```bash
   adb logcat *:E | findstr "CommunityRepo MyPostsFragment"
   ```
   - Look for error messages
   - Common errors:
     - "User not authenticated" → Sign in required
     - "PERMISSION_DENIED" → Firebase rules issue
     - Network errors → Internet connection

5. **Clear App Data and Try Again:**
   ```bash
   adb shell pm clear com.yourcompany.re_buy
   ```
   Then sign in and create a new post

### If authorUid is Missing

If you have old posts in Firebase without `authorUid`:

1. **Go to Firebase Console → Firestore**
2. **Find posts without authorUid**
3. **Manually add authorUid field** with your user ID
4. **Or delete old posts** and create new ones

## Performance Considerations

### In-Memory Sorting vs Firestore Ordering

**Question:** Is in-memory sorting slower than Firestore ordering?

**Answer:** For typical usage, no noticeable difference:

1. **Small Dataset:**
   - Most users have < 50 posts
   - Sorting 50 items in memory: < 1ms
   - Network latency dominates: 100-500ms

2. **Firestore Advantages:**
   - Sorting happens on server
   - Can use pagination more efficiently
   - But requires index creation and maintenance

3. **In-Memory Advantages:**
   - ✅ No index configuration needed
   - ✅ Works immediately
   - ✅ No Firebase Console setup
   - ✅ One less thing to maintain

**Trade-off:**
- For 1000+ posts, Firestore sorting would be faster
- For typical usage (< 100 posts), no difference
- Simplicity > Micro-optimization

## Alternative Solution (If You Want Firestore Sorting)

If you prefer server-side sorting, create a composite index:

### Option 1: Let Firestore Auto-Create Index

1. **Run the old code** (with `.orderBy()`)
2. **Check logcat** for index creation URL
3. **Click the URL** in error message
4. **Wait 5-10 minutes** for index to build

### Option 2: Manually Create Index

1. **Add to `firestore.indexes.json`:**
   ```json
   {
     "indexes": [
       {
         "collectionGroup": "posts",
         "queryScope": "COLLECTION",
         "fields": [
           { "fieldPath": "authorUid", "order": "ASCENDING" },
           { "fieldPath": "createdAt", "order": "DESCENDING" }
         ]
       }
     ]
   }
   ```

2. **Deploy:**
   ```bash
   firebase deploy --only firestore:indexes
   ```

3. **Wait** 5-10 minutes for index to build

**Our fix avoids this complexity entirely!**

## Summary

### Problem:
Posts created by user don't appear in My Page → 내 게시글

### Root Cause:
`getMyPosts()` used `.orderBy()` after `.whereEqualTo()` which requires Firestore composite index

### Solution:
- ✅ Removed `.orderBy()` from Firestore query
- ✅ Added in-memory sorting with `.sortedByDescending()`
- ✅ Added comprehensive logging
- ✅ No Firebase Console configuration needed

### Files Modified:

| File | Lines | Change |
|------|-------|--------|
| CommunityRepository.kt | 417-444 | Fixed `getMyPosts()` method |
| MyPostsFragment.kt | 58-90 | Added comprehensive logging |

### Result:
✅ Posts created by user now appear in My Page → 내 게시글
✅ Sorted newest first (same behavior as before)
✅ Works immediately without index creation
✅ Detailed logging for debugging

## Testing Checklist

- [ ] Build and install app
- [ ] Sign in to app
- [ ] Create a new post in Community
- [ ] Go to My Page → 내 게시글
- [ ] Verify post appears
- [ ] Create 2 more posts
- [ ] Verify all 3 posts appear, newest first
- [ ] Edit a post
- [ ] Verify it still appears
- [ ] Delete a post
- [ ] Verify it's removed
- [ ] Check logcat for success logs
- [ ] Verify no errors in logcat

All changes complete and ready to test! 🎉

## Related Issues Fixed

This fix follows the same pattern as:
- ✅ Product Favorites loading issue (fixed in `CRITICAL_FIXES_PRODUCT_FAVORITES.md`)
- ✅ Both used `.orderBy()` without required indexes
- ✅ Both now sort in memory instead
- ✅ Both work immediately without Firebase configuration
