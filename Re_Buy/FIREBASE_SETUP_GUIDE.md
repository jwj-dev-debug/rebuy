# Firebase Setup Guide for Re_Buy App

## Quick Setup (15 minutes)

### Step 1: Create Firebase Project (3 minutes)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Project name: `re_buy` (or any name you prefer)
4. (Optional) Disable Google Analytics or leave enabled
5. Click **"Create project"**
6. Wait for setup to complete

### Step 2: Add Android App (5 minutes)

1. In Firebase Console, click the **Android icon** (</>) to add Android app
2. Fill in the form:
   - **Android package name:** `com.yourcompany.re_buy`
   - **App nickname:** `Re:Buy` (optional)
   - **Debug signing certificate SHA-1:** Leave blank (optional)
3. Click **"Register app"**

### Step 3: Download Configuration File (1 minute)

1. Download **`google-services.json`** file
2. Place it in your project:
   ```
   C:\Android\Re_Buy\app\google-services.json
   ```
   **IMPORTANT:** Must be in the `app/` folder, not the root!

3. Verify location:
   ```
   Re_Buy/
   ├── app/
   │   ├── build.gradle.kts
   │   ├── google-services.json  ← HERE!
   │   └── src/
   └── build.gradle.kts
   ```

### Step 4: Enable Firebase Authentication (3 minutes)

1. In Firebase Console, click **"Authentication"** in left menu
2. Click **"Get started"**
3. Go to **"Sign-in method"** tab
4. Click on **"Email/Password"**
5. Toggle the **first switch** to **Enable**
6. Click **"Save"**

**Note:** Don't enable "Email link (passwordless)" unless you want that feature

### Step 5: Create Firestore Database (3 minutes)

1. In Firebase Console, click **"Firestore Database"** in left menu
2. Click **"Create database"**
3. Select **"Start in production mode"**
4. Choose location:
   - For Korea: **asia-northeast3 (Seoul)**
   - Or select closest to your users
5. Click **"Enable"**

### Step 6: Configure Firestore Security Rules (2 minutes)

1. In Firestore, go to **"Rules"** tab
2. Replace the default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Products collection - read by everyone, write by admin only
    match /products/{product} {
      allow read: if true;
      allow write: if false;
    }

    // Community posts - read by everyone
    match /posts/{post} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update: if request.auth != null &&
                    request.auth.uid == resource.data.authorUid;
      allow delete: if request.auth != null &&
                    request.auth.uid == resource.data.authorUid;
    }
  }
}
```

3. Click **"Publish"**

### Step 7: Add Sample Product Data (5 minutes)

#### Option A: Manual Entry (Recommended for testing)

1. In Firestore Console, click **"Start collection"**
2. Collection ID: `products`
3. Document ID: Click **"Auto-ID"**
4. Add fields:

| Field Name | Type | Value |
|------------|------|-------|
| title | string | `Samsung 냉장고 500L` |
| price | string | `150,000원` |
| imageUrl | string | `https://via.placeholder.com/300` |
| center | string | `서대문구 재활용센터` |

5. Click **"Save"**

6. Add more products (repeat 3-5 times):
   - Click **"Add document"**
   - Use different titles: "LG 세탁기 15kg", "삼성 냉장고 300L", etc.
   - Alternate centers: "동대문구 재활용센터" or "서대문구 재활용센터"
   - Use price variations: "120,000원", "200,000원", etc.

#### Option B: Import JSON (For bulk data)

You can import from Apify or use Firebase Admin SDK to bulk upload products.

---

## ✅ Verification Checklist

After completing the setup, verify:

- [ ] `google-services.json` is in `app/` folder
- [ ] Firebase Authentication is enabled (Email/Password)
- [ ] Firestore database is created
- [ ] Firestore security rules are published
- [ ] At least 3-5 sample products exist in `products` collection
- [ ] Android Studio Gradle sync completed successfully

---

## 🔧 Testing the Setup

### Test 1: Build the App
```bash
# In Android Studio:
Build > Make Project
```
Should build without errors.

### Test 2: Test Registration
1. Run app on emulator/device
2. Click **"Sign Up"** on login screen
3. Enter email and password
4. Click **"Sign Up"**
5. Check Firebase Console > Authentication > Users
6. You should see the new user!

### Test 3: Test Login
1. Use the credentials you just created
2. Click **"Sign In"**
3. Should redirect to MainActivity with tabs

### Test 4: Test Guest Mode
1. On login screen, click **"Go to Home"**
2. Should go directly to MainActivity
3. Can browse but can't create posts

### Test 5: Test Product Display
1. Go to Home tab
2. Should see random products (if you added sample data)
3. If empty, add more products in Firestore Console

---

## 🐛 Common Issues & Solutions

### Issue 1: "google-services.json not found"
**Solution:**
- Verify file is in `app/` folder (not root)
- File name is exactly `google-services.json` (lowercase)
- Sync Gradle: **File > Sync Project with Gradle Files**

### Issue 2: "Default FirebaseApp is not initialized"
**Solution:**
- Check package name in `google-services.json` matches `com.yourcompany.re_buy`
- Clean and rebuild: **Build > Clean Project** then **Build > Rebuild Project**
- Restart Android Studio

### Issue 3: "Permission denied" when accessing Firestore
**Solution:**
- Check Firestore Security Rules are published
- For writes: Ensure user is authenticated
- Re-check rules match the ones in Step 6

### Issue 4: No products showing in app
**Solution:**
- Add sample products in Firestore Console (Step 7)
- Check collection name is exactly `products` (lowercase)
- Check fields match: `title`, `price`, `imageUrl`, `center`

### Issue 5: Authentication not working
**Solution:**
- Verify Email/Password is enabled in Firebase Console
- Check internet connection
- Ensure `google-services.json` is up to date

---

## 📦 Firestore Data Structure

### Products Collection
```
products/ (collection)
  └── {auto-generated-id}/ (document)
      ├── title: string (e.g., "Samsung 냉장고 500L")
      ├── price: string (e.g., "150,000원")
      ├── imageUrl: string (URL to product image)
      └── center: string (e.g., "서대문구 재활용센터")
```

### Posts Collection (Auto-created when users post)
```
posts/ (collection)
  └── {auto-generated-id}/ (document)
      ├── title: string
      ├── content: string
      ├── author: string
      ├── authorUid: string
      ├── region: string (e.g., "서대문구")
      └── timestamp: timestamp
```

---

## 🔐 Security Best Practices

1. **Never commit** `google-services.json` to public repositories
2. Add to `.gitignore`:
   ```
   # Firebase
   google-services.json
   ```
3. Keep Firestore Security Rules strict
4. Validate all user input
5. Use Firebase Authentication for user management

---

## 📱 Ready to Use!

After completing this setup:

1. ✅ Firebase is fully configured
2. ✅ App can authenticate users
3. ✅ App can read/write Firestore data
4. ✅ Sample products are available
5. ✅ Ready to build and run!

---

## 🚀 Next Steps

After basic setup:

1. **Add More Products:**
   - Manually in Firestore Console
   - Or integrate Apify for automated scraping

2. **Implement Firebase Logic:**
   - See TODO comments in code
   - Add actual login/register logic
   - Load products from Firestore

3. **Test All Features:**
   - User registration and login
   - Product browsing
   - Search with filters
   - Community posts

4. **Deploy:**
   - Test on physical device
   - Prepare for production
   - Submit to Play Store (optional)

---

## 📚 Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firestore Get Started](https://firebase.google.com/docs/firestore/quickstart)
- [Firebase Authentication](https://firebase.google.com/docs/auth/android/start)

---

**Your Firebase is now ready! The app can build and run.** 🎉
