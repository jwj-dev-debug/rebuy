import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { auth } from './firebase/config';
import { onAuthStateChanged, User } from 'firebase/auth';
import Login from './pages/Login';
import Register from './pages/Register';
import Main from './pages/Main';
import ProductDetail from './pages/ProductDetail';
import PostDetail from './pages/PostDetail';
import CreatePost from './pages/CreatePost';
import MyProfile from './pages/MyProfile';
import ImpactDashboard from './pages/ImpactDashboard';
import './App.css';

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/" element={<Main user={user} />} />
        <Route path="/product/:id" element={<ProductDetail user={user} />} />
        <Route path="/post/:id" element={<PostDetail user={user} />} />
        <Route path="/create-post" element={user ? <CreatePost user={user} /> : <Navigate to="/login" />} />
        <Route path="/profile" element={user ? <MyProfile user={user} /> : <Navigate to="/login" />} />
        <Route path="/impact" element={user ? <ImpactDashboard user={user} /> : <Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
