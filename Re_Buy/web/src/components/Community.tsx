import { useState, useEffect } from 'react';
import { User } from 'firebase/auth';
import { collection, getDocs, query, orderBy } from 'firebase/firestore';
import { db } from '../firebase/config';
import { CommunityPost } from '../types';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import './Community.css';

interface CommunityProps {
  user: User | null;
}

function Community({ user }: CommunityProps) {
  const [posts, setPosts] = useState<CommunityPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const navigate = useNavigate();

  useEffect(() => {
    loadPosts();
  }, []);

  const loadPosts = async () => {
    try {
      const postsRef = collection(db, 'community_posts');
      const q = query(postsRef, orderBy('createdAt', 'desc'));
      const snapshot = await getDocs(q);

      const postsData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data(),
        createdAt: doc.data().createdAt?.toDate(),
        updatedAt: doc.data().updatedAt?.toDate()
      })) as CommunityPost[];

      setPosts(postsData);
    } catch (error) {
      console.error('Error loading posts:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredPosts = filter === 'all'
    ? posts
    : posts.filter(post => post.region === filter);

  const formatDate = (date: Date) => {
    try {
      return format(date, 'yyyy.MM.dd HH:mm');
    } catch {
      return '';
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>게시글을 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="community-container">
      <div className="community-header">
        <h2 className="community-title">커뮤니티</h2>
        <div className="region-filter">
          <button
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            전체
          </button>
          <button
            className={`filter-btn ${filter === 'dongdaemun' ? 'active' : ''}`}
            onClick={() => setFilter('dongdaemun')}
          >
            동대문구
          </button>
          <button
            className={`filter-btn ${filter === 'seodaemun' ? 'active' : ''}`}
            onClick={() => setFilter('seodaemun')}
          >
            서대문구
          </button>
        </div>
      </div>

      {filteredPosts.length === 0 ? (
        <div className="empty-state">
          <p>아직 게시글이 없습니다.</p>
          {user && <p className="empty-subtitle">첫 게시글을 작성해보세요!</p>}
        </div>
      ) : (
        <div className="posts-list">
          {filteredPosts.map((post) => (
            <div
              key={post.id}
              className="post-card"
              onClick={() => navigate(`/post/${post.id}`)}
            >
              <div className="post-header">
                <div className="post-user-info">
                  <span className="post-author">{post.userEmail?.split('@')[0] || '익명'}</span>
                  <span className="post-region-tag">
                    {post.region === 'dongdaemun' ? '동대문구' : '서대문구'}
                  </span>
                </div>
                <span className="post-date">{formatDate(post.createdAt)}</span>
              </div>
              <h3 className="post-title">{post.title}</h3>
              <p className="post-content">{post.content}</p>
              {post.imageUrls && post.imageUrls.length > 0 && (
                <div className="post-images">
                  {post.imageUrls.slice(0, 3).map((url, index) => (
                    <img key={index} src={url} alt={`Post image ${index + 1}`} loading="lazy" />
                  ))}
                  {post.imageUrls.length > 3 && (
                    <div className="more-images">+{post.imageUrls.length - 3}</div>
                  )}
                </div>
              )}
              <div className="post-footer">
                <span className="post-stat">좋아요 {post.likes || 0}</span>
                <span className="post-stat">댓글 {post.commentCount || 0}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Community;
