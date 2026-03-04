import { useState, useEffect } from 'react';
import { User } from 'firebase/auth';
import { useParams, useNavigate } from 'react-router-dom';
import { doc, getDoc, collection, addDoc, query, where, getDocs, orderBy } from 'firebase/firestore';
import { db } from '../firebase/config';
import { CommunityPost, Comment } from '../types';
import { format } from 'date-fns';
import './PostDetail.css';

interface PostDetailProps {
  user: User | null;
}

function PostDetail({ user }: PostDetailProps) {
  const { id } = useParams<{ id: string }>();
  const [post, setPost] = useState<CommunityPost | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentText, setCommentText] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      loadPost();
      loadComments();
    }
  }, [id]);

  const loadPost = async () => {
    try {
      const postDoc = await getDoc(doc(db, 'community_posts', id!));
      if (postDoc.exists()) {
        setPost({
          id: postDoc.id,
          ...postDoc.data(),
          createdAt: postDoc.data().createdAt?.toDate(),
          updatedAt: postDoc.data().updatedAt?.toDate()
        } as CommunityPost);
      }
    } catch (error) {
      console.error('Error loading post:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async () => {
    try {
      const commentsRef = collection(db, 'comments');
      const q = query(commentsRef, where('postId', '==', id), orderBy('createdAt', 'desc'));
      const snapshot = await getDocs(q);

      const commentsData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data(),
        createdAt: doc.data().createdAt?.toDate()
      })) as Comment[];

      setComments(commentsData);
    } catch (error) {
      console.error('Error loading comments:', error);
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (!commentText.trim()) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }

    try {
      await addDoc(collection(db, 'comments'), {
        postId: id,
        userId: user.uid,
        userEmail: user.email,
        content: commentText,
        createdAt: new Date(),
        likes: 0
      });

      setCommentText('');
      loadComments();
      alert('댓글이 작성되었습니다.');
    } catch (error) {
      console.error('Error adding comment:', error);
      alert('댓글 작성에 실패했습니다.');
    }
  };

  const formatDate = (date: Date) => {
    try {
      return format(date, 'yyyy.MM.dd HH:mm');
    } catch {
      return '';
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

  if (!post) {
    return (
      <div className="error-screen">
        <p>게시글을 찾을 수 없습니다.</p>
        <button onClick={() => navigate('/')} className="btn btn-primary">
          홈으로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="post-detail-container">
      <button onClick={() => navigate(-1)} className="back-button">
        ← 뒤로 가기
      </button>

      <div className="post-detail-content">
        <div className="post-detail-header">
          <h1 className="post-detail-title">{post.title}</h1>
          <div className="post-detail-meta">
            <span className="post-author">{post.userEmail?.split('@')[0] || '익명'}</span>
            <span className="post-region-tag">
              {post.region === 'dongdaemun' ? '동대문구' : '서대문구'}
            </span>
            <span className="post-date">{formatDate(post.createdAt)}</span>
          </div>
        </div>

        <div className="post-detail-body">
          <p className="post-content-full">{post.content}</p>

          {post.imageUrls && post.imageUrls.length > 0 && (
            <div className="post-images-full">
              {post.imageUrls.map((url, index) => (
                <img key={index} src={url} alt={`Post image ${index + 1}`} />
              ))}
            </div>
          )}
        </div>

        <div className="post-stats">
          <span>좋아요 {post.likes || 0}</span>
          <span>댓글 {comments.length}</span>
        </div>
      </div>

      <div className="comments-section">
        <h2 className="comments-title">댓글 ({comments.length})</h2>

        {user && (
          <form onSubmit={handleAddComment} className="comment-form">
            <textarea
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="댓글을 입력하세요..."
              rows={3}
            />
            <button type="submit" className="btn btn-primary">
              댓글 작성
            </button>
          </form>
        )}

        <div className="comments-list">
          {comments.length === 0 ? (
            <p className="empty-comments">아직 댓글이 없습니다. 첫 댓글을 작성해보세요!</p>
          ) : (
            comments.map((comment) => (
              <div key={comment.id} className="comment-item">
                <div className="comment-header">
                  <span className="comment-author">{comment.userEmail?.split('@')[0] || '익명'}</span>
                  <span className="comment-date">{formatDate(comment.createdAt)}</span>
                </div>
                <p className="comment-content">{comment.content}</p>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default PostDetail;
