import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

// Firebase configuration
// Using your existing Firebase project: rebuy-2025
const firebaseConfig = {
  apiKey: "AIzaSyA-VLo-1Ja2AmITcZalRZ8PGqJ0FjHivEw",
  authDomain: "rebuy-2025.firebaseapp.com",
  projectId: "rebuy-2025",
  storageBucket: "rebuy-2025.firebasestorage.app",
  messagingSenderId: "156145761387",
  appId: "1:156145761387:android:865f852b491addfdd8852e" // Using Android app ID temporarily - register web app for production
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Firebase services
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

export default app;
