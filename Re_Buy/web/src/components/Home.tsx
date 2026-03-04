import { useState, useEffect } from 'react';
import { User } from 'firebase/auth';
import { collection, getDocs, query, limit } from 'firebase/firestore';
import { db } from '../firebase/config';
import { Product } from '../types';
import { useNavigate } from 'react-router-dom';
import './Home.css';

interface HomeProps {
  user: User | null;
}

function Home({ user }: HomeProps) {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      const productsRef = collection(db, 'products');
      const q = query(productsRef, limit(20));
      const snapshot = await getDocs(q);

      const productsData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as Product[];

      setProducts(productsData);
    } catch (error) {
      console.error('Error loading products:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>상품을 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="home-container">
      <h2 className="home-title">재활용 상품 둘러보기</h2>
      <p className="home-subtitle">서대문구 & 동대문구 재활용센터의 상품들</p>

      {products.length === 0 ? (
        <div className="empty-state">
          <p>아직 등록된 상품이 없습니다.</p>
          <p className="empty-subtitle">곧 새로운 상품이 올라올 예정입니다!</p>
        </div>
      ) : (
        <div className="products-grid">
          {products.map((product) => (
            <div
              key={product.id}
              className="product-card"
              onClick={() => navigate(`/product/${product.id}`)}
            >
              <div className="product-image">
                {product.image ? (
                  <img src={product.image} alt={product.title} loading="lazy" />
                ) : (
                  <div className="product-image-placeholder">
                    <span>이미지 없음</span>
                  </div>
                )}
              </div>
              <div className="product-info">
                <h3 className="product-title">{product.title}</h3>
                <p className="product-price">{product.price}</p>
                <p className="product-center">{product.center}</p>
                {product.category && (
                  <span className="product-category">{product.category}</span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Home;
