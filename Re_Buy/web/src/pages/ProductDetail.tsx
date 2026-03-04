import { useState, useEffect } from 'react';
import { User } from 'firebase/auth';
import { useParams, useNavigate } from 'react-router-dom';
import { doc, getDoc, collection, addDoc, query, where, getDocs, deleteDoc } from 'firebase/firestore';
import { db } from '../firebase/config';
import { Product, conditionLabels } from '../types';
import './ProductDetail.css';

interface ProductDetailProps {
  user: User | null;
}

function ProductDetail({ user }: ProductDetailProps) {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [isFavorite, setIsFavorite] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      loadProduct();
      if (user) {
        checkFavorite();
      }
    }
  }, [id, user]);

  const loadProduct = async () => {
    try {
      const productDoc = await getDoc(doc(db, 'products', id!));
      if (productDoc.exists()) {
        setProduct({ id: productDoc.id, ...productDoc.data() } as Product);
      }
    } catch (error) {
      console.error('Error loading product:', error);
    } finally {
      setLoading(false);
    }
  };

  const checkFavorite = async () => {
    try {
      const favRef = collection(db, 'favorites');
      const q = query(favRef, where('userId', '==', user!.uid), where('productId', '==', id));
      const snapshot = await getDocs(q);
      setIsFavorite(!snapshot.empty);
    } catch (error) {
      console.error('Error checking favorite:', error);
    }
  };

  const toggleFavorite = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      const favRef = collection(db, 'favorites');
      const q = query(favRef, where('userId', '==', user.uid), where('productId', '==', id));
      const snapshot = await getDocs(q);

      if (snapshot.empty) {
        await addDoc(favRef, {
          userId: user.uid,
          productId: id,
          addedAt: new Date()
        });
        setIsFavorite(true);
        alert('즐겨찾기에 추가되었습니다.');
      } else {
        await deleteDoc(snapshot.docs[0].ref);
        setIsFavorite(false);
        alert('즐겨찾기에서 제거되었습니다.');
      }
    } catch (error) {
      console.error('Error toggling favorite:', error);
      alert('오류가 발생했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="error-screen">
        <p>상품을 찾을 수 없습니다.</p>
        <button onClick={() => navigate('/')} className="btn btn-primary">
          홈으로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="product-detail-container">
      <button onClick={() => navigate(-1)} className="back-button">
        ← 뒤로 가기
      </button>

      <div className="product-detail-content">
        <div className="product-image-large">
          {product.image ? (
            <img src={product.image} alt={product.title} />
          ) : (
            <div className="image-placeholder-large">이미지 없음</div>
          )}
        </div>

        <div className="product-details">
          <h1 className="product-detail-title">{product.title}</h1>
          <p className="product-detail-price">{product.price}</p>

          <div className="product-meta">
            <div className="meta-item">
              <strong>센터:</strong> {product.center}
            </div>
            {product.category && (
              <div className="meta-item">
                <strong>카테고리:</strong> {product.category}
              </div>
            )}
            {product.condition && (
              <div className="meta-item">
                <strong>상태:</strong> {conditionLabels[product.condition]?.name || product.condition}
              </div>
            )}
            {product.createdAt && (
              <div className="meta-item">
                <strong>등록일:</strong> {product.createdAt}
              </div>
            )}
          </div>

          {product.description && (
            <div className="product-description">
              <h3>상품 설명</h3>
              <p>{product.description}</p>
            </div>
          )}

          <div className="product-actions">
            <button onClick={toggleFavorite} className={`btn ${isFavorite ? 'btn-secondary' : 'btn-primary'}`}>
              {isFavorite ? '❤️ 즐겨찾기 해제' : '🤍 즐겨찾기 추가'}
            </button>
            {product.link && (
              <a href={product.link} target="_blank" rel="noopener noreferrer" className="btn btn-primary">
                원본 페이지 보기
              </a>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetail;
