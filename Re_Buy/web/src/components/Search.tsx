import { useState, useEffect } from 'react';
import { User } from 'firebase/auth';
import { collection, getDocs, query, where } from 'firebase/firestore';
import { db } from '../firebase/config';
import { Product } from '../types';
import { useNavigate } from 'react-router-dom';
import './Search.css';

interface SearchProps {
  user: User | null;
}

function Search({ user }: SearchProps) {
  const [searchText, setSearchText] = useState('');
  const [region, setRegion] = useState('all');
  const [category, setCategory] = useState('all');
  const [products, setProducts] = useState<Product[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadProducts();
  }, []);

  useEffect(() => {
    filterProducts();
  }, [searchText, region, category, products]);

  const loadProducts = async () => {
    try {
      const productsRef = collection(db, 'products');
      const snapshot = await getDocs(productsRef);

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

  const filterProducts = () => {
    let filtered = [...products];

    // Filter by search text
    if (searchText.trim()) {
      filtered = filtered.filter(p =>
        p.title.toLowerCase().includes(searchText.toLowerCase()) ||
        p.category.toLowerCase().includes(searchText.toLowerCase())
      );
    }

    // Filter by region
    if (region !== 'all') {
      filtered = filtered.filter(p => p.center.includes(region));
    }

    // Filter by category
    if (category !== 'all') {
      filtered = filtered.filter(p =>
        p.title.toLowerCase().includes(category.toLowerCase())
      );
    }

    setFilteredProducts(filtered);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    filterProducts();
  };

  return (
    <div className="search-container">
      <h2 className="search-title">상품 검색</h2>

      <form onSubmit={handleSearch} className="search-form">
        <div className="search-filters">
          <div className="filter-group">
            <label>지역</label>
            <select value={region} onChange={(e) => setRegion(e.target.value)}>
              <option value="all">전체</option>
              <option value="서대문">서대문구</option>
              <option value="동대문">동대문구</option>
            </select>
          </div>

          <div className="filter-group">
            <label>카테고리</label>
            <select value={category} onChange={(e) => setCategory(e.target.value)}>
              <option value="all">전체</option>
              <option value="냉장고">냉장고</option>
              <option value="세탁기">세탁기</option>
              <option value="전자렌지">전자렌지</option>
              <option value="TV">TV</option>
            </select>
          </div>
        </div>

        <div className="search-input-wrapper">
          <input
            type="text"
            placeholder="상품명을 입력하세요"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            className="search-input"
          />
          <button type="submit" className="btn btn-primary">
            검색
          </button>
        </div>
      </form>

      <div className="search-results">
        <p className="results-count">
          {filteredProducts.length}개의 상품
        </p>

        {loading ? (
          <div className="loading-container">
            <div className="spinner"></div>
            <p>검색 중...</p>
          </div>
        ) : filteredProducts.length === 0 ? (
          <div className="empty-state">
            <p>검색 결과가 없습니다.</p>
            <p className="empty-subtitle">다른 검색어로 시도해보세요.</p>
          </div>
        ) : (
          <div className="products-grid">
            {filteredProducts.map((product) => (
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
    </div>
  );
}

export default Search;
