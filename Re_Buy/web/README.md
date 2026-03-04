# Re:Buy Web Version

A responsive web application for the Re:Buy recycled goods marketplace, built with React, TypeScript, and Firebase.

## Features

### Authentication
- Email/password login and registration with Firebase Auth
- Guest mode for browsing without login

### Product Browsing
- Home page with product grid
- Product detail pages
- Favorite products
- Search and filter by region and category

### Community
- View community posts
- Create new posts (authenticated users)
- Comment on posts
- Filter by region (서대문구, 동대문구)

### Map
- View recycling center locations
- Center information display

### User Profile
- View purchase history
- Manage favorites
- Environmental impact dashboard

### Environmental Impact
- Track CO2 savings
- Monitor water conservation
- View energy savings
- Achievement badges

## Tech Stack

- **Frontend:** React 18 with TypeScript
- **Build Tool:** Vite
- **Styling:** Pure CSS with CSS Variables
- **Backend:** Firebase (Auth, Firestore, Storage)
- **Routing:** React Router v6
- **Date Formatting:** date-fns

## Mobile Responsive Design

The app is fully responsive with:
- Mobile-first CSS approach
- Breakpoints at 480px, 768px, and 1024px
- Touch-friendly UI elements
- Optimized for all screen sizes
- Test with browser DevTools (F12 → Toggle device toolbar)

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Firebase account

### Installation

1. **Navigate to the web directory:**
   ```bash
   cd C:\Android\Re_Buy\web
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Configure Firebase:**

   Edit `src/firebase/config.ts` and replace with your Firebase config:
   ```typescript
   const firebaseConfig = {
     apiKey: "YOUR_API_KEY",
     authDomain: "YOUR_AUTH_DOMAIN",
     projectId: "YOUR_PROJECT_ID",
     storageBucket: "YOUR_STORAGE_BUCKET",
     messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
     appId: "YOUR_APP_ID"
   };
   ```

4. **Run the development server:**
   ```bash
   npm run dev
   ```

5. **Open in browser:**
   - The app will open automatically at `http://localhost:3000`
   - Or manually navigate to the URL shown in terminal

## Testing Mobile View

### Method 1: Browser DevTools (F12)
1. Open the app in Chrome/Edge/Firefox
2. Press **F12** to open Developer Tools
3. Click the device toggle icon (or press Ctrl+Shift+M)
4. Select a mobile device preset (iPhone, Samsung Galaxy, etc.)
5. Test the responsive design

### Method 2: Resize Browser Window
1. Open the app
2. Resize your browser window to different widths
3. Observe the responsive layout changes

## Firebase Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Add a web app to your project

### 2. Enable Services

**Authentication:**
- Go to Authentication → Sign-in method
- Enable Email/Password

**Firestore Database:**
- Go to Firestore Database → Create database
- Start in test mode (for development)

**Storage:**
- Go to Storage → Get Started
- Start in test mode (for development)

### 3. Add Sample Data

Create these collections in Firestore:

**products:**
```json
{
  "title": "냉장고 LG 디오스",
  "price": "150,000원",
  "image": "https://example.com/image.jpg",
  "center": "서대문구 재활용센터",
  "category": "냉장고",
  "createdAt": "2024-01-15",
  "crawledAt": "2024-01-15",
  "sourceUrl": "https://example.com",
  "status": "AVAILABLE",
  "condition": "GOOD",
  "quantity": 1,
  "description": "상태 좋은 냉장고입니다."
}
```

**recycling_centers:**
```json
{
  "name": "서대문구 재활용센터",
  "address": "서울시 서대문구 ...",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "phone": "02-123-4567",
  "hours": "09:00 - 18:00",
  "region": "seodaemun"
}
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Project Structure

```
web/
├── src/
│   ├── components/          # Reusable components
│   │   ├── Home.tsx         # Home tab component
│   │   ├── Search.tsx       # Search tab component
│   │   ├── Community.tsx    # Community tab component
│   │   └── Map.tsx          # Map tab component
│   ├── pages/               # Page components
│   │   ├── Login.tsx        # Login page
│   │   ├── Register.tsx     # Registration page
│   │   ├── Main.tsx         # Main app with tabs
│   │   ├── ProductDetail.tsx
│   │   ├── PostDetail.tsx
│   │   ├── CreatePost.tsx
│   │   ├── MyProfile.tsx
│   │   └── ImpactDashboard.tsx
│   ├── firebase/            # Firebase configuration
│   │   └── config.ts
│   ├── types/               # TypeScript types
│   │   └── index.ts
│   ├── App.tsx              # Main app component
│   ├── App.css
│   ├── main.tsx             # Entry point
│   └── index.css            # Global styles
├── public/                  # Static assets
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## Opening in VS Code

1. **Open VS Code**
2. **File → Open Folder**
3. **Navigate to:** `C:\Android\Re_Buy\web`
4. **Click "Select Folder"**

Or from command line:
```bash
cd C:\Android\Re_Buy\web
code .
```

## Responsive Breakpoints

- **Desktop:** 1024px and above
- **Tablet:** 768px - 1023px
- **Mobile:** 480px - 767px
- **Small Mobile:** < 480px

## CSS Variables

The app uses CSS variables for theming (defined in `src/index.css`):

```css
--primary-color: #6200ea
--primary-dark: #4a00b8
--primary-light: #7c35f0
--secondary-color: #03dac6
--background: #ffffff
--surface: #f5f5f5
--error: #b00020
--text-primary: #000000
--text-secondary: #666666
--border: #e0e0e0
```

## Troubleshooting

### Port Already in Use
```bash
# Kill process on port 3000
npx kill-port 3000
# Or use a different port
npm run dev -- --port 3001
```

### Firebase Errors
- Verify Firebase config in `src/firebase/config.ts`
- Check Firebase console for enabled services
- Ensure Firestore security rules allow read/write (for development)

### Module Not Found
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

## Building for Production

```bash
npm run build
```

The build output will be in the `dist/` directory. You can deploy this to:
- Firebase Hosting
- Netlify
- Vercel
- Any static hosting service

## Deployment (Firebase Hosting)

1. Install Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```

2. Login to Firebase:
   ```bash
   firebase login
   ```

3. Initialize Firebase:
   ```bash
   firebase init hosting
   ```

4. Build and deploy:
   ```bash
   npm run build
   firebase deploy
   ```

## License

MIT License

## Support

For issues or questions, please refer to the main project README at `C:\Android\Re_Buy\README.md`

---

**Ready to use!** Start the dev server and test the responsive design with F12 in your browser.
