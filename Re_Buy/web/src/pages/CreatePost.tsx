import { useState } from 'react';
import { User } from 'firebase/auth';
import { useNavigate } from 'react-router-dom';
import { collection, addDoc } from 'firebase/firestore';
import { db } from '../firebase/config';
import './CreatePost.css';

interface CreatePostProps {
  user: User;
}

function CreatePost({ user }: CreatePostProps) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [region, setRegion] = useState('seodaemun');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim() || !content.trim()) {
      alert('제목과 내용을 입력해주세요.');
      return;
    }

    setLoading(true);

    try {
      await addDoc(collection(db, 'community_posts'), {
        userId: user.uid,
        userEmail: user.email,
        title,
        content,
        region,
        createdAt: new Date(),
        updatedAt: new Date(),
        likes: 0,
        commentCount: 0,
        imageUrls: []
      });

      alert('게시글이 작성되었습니다.');
      navigate('/');
    } catch (error) {
      console.error('Error creating post:', error);
      alert('게시글 작성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-post-container">
      <div className="create-post-box">
        <div className="create-post-header">
          <button onClick={() => navigate(-1)} className="back-button">
            ← 취소
          </button>
          <h1 className="create-post-title">새 게시글 작성</h1>
        </div>

        <form onSubmit={handleSubmit} className="create-post-form">
          <div className="form-group">
            <label htmlFor="region">지역</label>
            <select
              id="region"
              value={region}
              onChange={(e) => setRegion(e.target.value)}
            >
              <option value="seodaemun">서대문구</option>
              <option value="dongdaemun">동대문구</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="title">제목</label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="제목을 입력하세요"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="content">내용</label>
            <textarea
              id="content"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="내용을 입력하세요"
              rows={10}
              required
            />
          </div>

          <div className="form-actions">
            <button type="button" onClick={() => navigate(-1)} className="btn btn-secondary">
              취소
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? '작성 중...' : '작성 완료'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreatePost;
