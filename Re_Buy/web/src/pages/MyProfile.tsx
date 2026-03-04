import { useState, useEffect } from 'react';
import { User } from 'firebase/auth';
import { useNavigate } from 'react-router-dom';
import { collection, query, where, getDocs, orderBy } from 'firebase/firestore';
import { db } from '../firebase/config';
import { Purchase, Favorite } from '../types';
import './MyProfile.css';

interface MyProfileProps {
  user: User;
}

function MyProfile({ user }: MyProfileProps) {
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [favorites, setFavorites] = useState<Favorite[]>([]);
  const [activeTab, setActiveTab] = useState<'purchases' | 'favorites'>('purchases');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadUserData();
  }, [user]);

  const loadUserData = async () => {
    try {
      // Load purchases
      const purchasesRef = collection(db, 'purchases');
      const purchasesQ = query(
        purchasesRef,
        where('userId', '==', user.uid),
        orderBy('purchaseDate', 'desc')
      );
      const purchasesSnapshot = await getDocs(purchasesQ);
      const purchasesData = purchasesSnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data(),
        purchaseDate: doc.data().purchaseDate?.toDate()
      })) as Purchase[];
      setPurchases(purchasesData);

      // Load favorites
      const favoritesRef = collection(db, 'favorites');
      const favoritesQ = query(
        favoritesRef,
        where('userId', '==', user.uid),
        orderBy('addedAt', 'desc')
      );
      const favoritesSnapshot = await getDocs(favoritesQ);
      const favoritesData = favoritesSnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data(),
        addedAt: doc.data().addedAt?.toDate()
      })) as Favorite[];
      setFavorites(favoritesData);
    } catch (error) {
      console.error('Error loading user data:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="profile-container">
      <button onClick={() => navigate(-1)} className="back-button">
        ← 뒤로 가기
      </button>

      <div className="profile-content">
        <div className="profile-header">
          <div className="profile-avatar">
            {user.email?.[0].toUpperCase() || 'U'}
          </div>
          <div className="profile-info">
            <h1 className="profile-name">{user.email?.split('@')[0]}</h1>
            <p className="profile-email">{user.email}</p>
          </div>
        </div>

        <div className="profile-stats">
          <div className="stat-item">
            <span className="stat-number">{purchases.length}</span>
            <span className="stat-label">구매</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">{favorites.length}</span>
            <span className="stat-label">즐겨찾기</span>
          </div>
          <div className="stat-item" onClick={() => navigate('/impact')} style={{ cursor: 'pointer' }}>
            <span className="stat-number">🌱</span>
            <span className="stat-label">환경 영향</span>
          </div>
        </div>

        <div className="profile-tabs">
          <button
            className={`tab-btn ${activeTab === 'purchases' ? 'active' : ''}`}
            onClick={() => setActiveTab('purchases')}
          >
            구매 내역
          </button>
          <button
            className={`tab-btn ${activeTab === 'favorites' ? 'active' : ''}`}
            onClick={() => setActiveTab('favorites')}
          >
            즐겨찾기
          </button>
        </div>

        <div className="profile-tab-content">
          {loading ? (
            <div className="loading-container">
              <div className="spinner"></div>
              <p>로딩 중...</p>
            </div>
          ) : activeTab === 'purchases' ? (
            <div className="purchases-list">
              {purchases.length === 0 ? (
                <div className="empty-state">
                  <p>구매 내역이 없습니다.</p>
                </div>
              ) : (
                purchases.map((purchase) => (
                  <div key={purchase.id} className="purchase-item">
                    <div className="purchase-image">
                      {purchase.productImage ? (
                        <img src={purchase.productImage} alt={purchase.productTitle} />
                      ) : (
                        <div className="image-placeholder">No Image</div>
                      )}
                    </div>
                    <div className="purchase-info">
                      <h3 className="purchase-title">{purchase.productTitle}</h3>
                      <p className="purchase-price">{purchase.productPrice}</p>
                      <p className="purchase-status">상태: {purchase.status}</p>
                    </div>
                  </div>
                ))
              )}
            </div>
          ) : (
            <div className="favorites-list">
              {favorites.length === 0 ? (
                <div className="empty-state">
                  <p>즐겨찾기한 상품이 없습니다.</p>
                </div>
              ) : (
                <div className="favorites-grid">
                  {favorites.map((favorite) => (
                    <div
                      key={favorite.id}
                      className="favorite-card"
                      onClick={() => navigate(`/product/${favorite.productId}`)}
                    >
                      <p>상품 ID: {favorite.productId}</p>
                      <button className="btn btn-small btn-primary">보기</button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default MyProfile;
