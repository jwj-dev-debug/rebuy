# Firebase Security Rules Fix - Favorites Permission Denied

## Error
```
PERMISSION_DENIED: Missing or insufficient permissions.
```

This happens because Firebase Firestore security rules are blocking access to the `favorites` collection.

---

## Solution: Update Firestore Security Rules

### Step 1: Go to Firebase Console

1. Open [Firebase Console](https://console.firebase.google.com/)
2. Select your **Re:Buy** project
3. Click **Firestore Database** in the left menu
4. Click the **Rules** tab at the top

### Step 2: Update Security Rules

Replace your current rules with these **updated rules**:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }

    // Helper function to check if user owns the document
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    // Posts collection
    match /posts/{postId} {
      // Anyone can read posts
      allow read: if true;

      // Only authenticated users can create posts
      allow create: if isAuthenticated() &&
                      request.resource.data.authorUid == request.auth.uid;

      // Only post author can update/delete their posts
      allow update, delete: if isAuthenticated() &&
                               resource.data.authorUid == request.auth.uid;
    }

    // Comments collection
    match /comments/{commentId} {
      // Anyone can read comments
      allow read: if true;

      // Only authenticated users can create comments
      allow create: if isAuthenticated() &&
                      request.resource.data.authorUid == request.auth.uid;

      // Only comment author can delete their comments
      allow delete: if isAuthenticated() &&
                      resource.data.authorUid == request.auth.uid;
    }

    // Favorites collection - THIS IS THE FIX!
    match /favorites/{favoriteId} {
      // Users can read their own favorites
      allow read: if isAuthenticated() &&
                    resource.data.userId == request.auth.uid;

      // Users can create favorites for themselves
      allow create: if isAuthenticated() &&
                      request.resource.data.userId == request.auth.uid;

      // Users can delete their own favorites
      allow delete: if isAuthenticated() &&
                      resource.data.userId == request.auth.uid;

      // Users can update their own favorites
      allow update: if isAuthenticated() &&
                      resource.data.userId == request.auth.uid;
    }

    // Messages collection (if you have messaging)
    match /messages/{messageId} {
      allow read: if isAuthenticated() &&
                    (resource.data.senderId == request.auth.uid ||
                     resource.data.receiverId == request.auth.uid);

      allow create: if isAuthenticated() &&
                      request.resource.data.senderId == request.auth.uid;
    }

    // User profiles (if you have user collection)
    match /users/{userId} {
      allow read: if true;
      allow write: if isAuthenticated() && userId == request.auth.uid;
    }
  }
}
```

### Step 3: Publish the Rules

1. Click **Publish** button at the top
2. Wait for confirmation message
3. Rules are now active!

---

## What These Rules Do

### Favorites Collection Rules:
```javascript
match /favorites/{favoriteId} {
  // ✅ Users can only read their own favorites
  allow read: if isAuthenticated() &&
                resource.data.userId == request.auth.uid;

  // ✅ Users can only create favorites for themselves
  allow create: if isAuthenticated() &&
                  request.resource.data.userId == request.auth.uid;

  // ✅ Users can only delete their own favorites
  allow delete: if isAuthenticated() &&
                  resource.data.userId == request.auth.uid;
}
```

### Security Features:
- ✅ Users must be logged in
- ✅ Users can only access their own favorites
- ✅ Users cannot see other users' favorites
- ✅ Users cannot modify other users' favorites
- ✅ Prevents unauthorized access

---

## Alternative: Quick Test Rules (NOT FOR PRODUCTION!)

If you want to test quickly (⚠️ **INSECURE** - for development only):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**⚠️ WARNING**: These rules allow any authenticated user to access ALL data. Only use for testing!

---

## Verification Steps

### After Updating Rules:

1. **Wait 1-2 minutes** for rules to propagate
2. **Restart your app** (force close and reopen)
3. **Login** to the app
4. **Try to favorite a product**:
   - Tap star on any product
   - Should work without error!

### Check if it worked:
1. Go to Firebase Console → Firestore Database
2. Click **Data** tab
3. Look for `favorites` collection
4. You should see new documents being created

---

## Common Issues

### Issue: Still getting permission denied
**Solutions**:
1. Wait 2-3 minutes (rules take time to propagate)
2. Make sure you clicked **Publish**
3. Restart the app completely
4. Check you're logged in
5. Verify the collection name is exactly `favorites` (not `favorite`)

### Issue: Rules won't save
**Solutions**:
1. Check for syntax errors (look for red underlines)
2. Make sure `rules_version = '2';` is at the top
3. Copy-paste the rules exactly as shown above

### Issue: Error says "undefined"
**Solution**:
- The document doesn't exist yet (first time creating)
- This is normal for `create` operations
- The rules handle this correctly

---

## Testing Your Rules

You can test rules directly in Firebase Console:

1. Go to **Firestore Database** → **Rules** tab
2. Click **Rules Playground** button
3. Set:
   - **Location**: `/favorites/test123`
   - **Read/Write**: Read or Write
   - **Authenticated**: Yes
   - **UID**: `testuser123`
4. Click **Run**

**Expected Results**:
- ✅ Read: Should ALLOW if userId matches
- ✅ Write: Should ALLOW if userId matches
- ❌ Should DENY if not authenticated or different user

---

## Firestore Structure Check

Make sure your favorites documents have this structure:

```javascript
{
  userId: "abc123",           // ✅ Required - must match auth.uid
  itemId: "product_or_post_id",
  itemType: "product" or "post",
  itemTitle: "Title here",
  itemImage: "https://...",
  createdAt: Timestamp
}
```

**Important**: The `userId` field MUST exist and match the authenticated user's UID!

---

## Quick Fix Commands

If you need to rebuild after updating rules:

```bash
cd C:\Android\Re_Buy
gradlew.bat clean
gradlew.bat assembleDebug
gradlew.bat installDebug
```

---

## Summary

1. ✅ Go to Firebase Console
2. ✅ Click Firestore Database → Rules tab
3. ✅ Copy the security rules from above
4. ✅ Click Publish
5. ✅ Wait 1-2 minutes
6. ✅ Restart app and test

**The favorites feature will work after this fix!** 🎉

---

## Need Help?

If you still get errors after following these steps:

1. Check Firebase Console → Firestore → Usage tab for error logs
2. Check Android logcat: `adb logcat | grep "Firestore"`
3. Verify you're logged in: Check `FirebaseAuth.getInstance().currentUser`
4. Make sure collection name is `favorites` (not plural/different spelling)

---

**Status**: Security rules provided ✅
**Action Required**: Update Firebase Console ✅
**Estimated Time**: 5 minutes
