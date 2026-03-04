import { useState } from 'react';
import { User } from 'firebase/auth';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebase/config';
import { signOut } from 'firebase/auth';
import Home from '../components/Home';
import Search from '../components/Search';
import Community from '../components/Community';
import Map from '../components/Map';
import './Main.css';

interface MainProps {
  user: User | null;
}

type TabType = 'home' | 'search' | 'community' | 'map';

function Main({ user }: MainProps) {
  const [activeTab, setActiveTab] = useState<TabType>('home');
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await signOut(auth);
      setActiveTab('home');
    } catch (error) {
      console.error('Logout error:', error);
    }
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case 'home':
        return <Home user={user} />;
      case 'search':
        return <Search user={user} />;
      case 'community':
        return <Community user={user} />;
      case 'map':
        return <Map />;
      default:
        return <Home user={user} />;
    }
  };

  return (
    <div className="main-container">
      <header className="main-header">
        <div className="header-content">
          <h1 className="header-logo">Re:Buy</h1>
          <div className="header-actions">
            {user ? (
              <>
                <button onClick={() => navigate('/profile')} className="btn btn-small btn-secondary">
                  프로필
                </button>
                <button onClick={handleLogout} className="btn btn-small btn-secondary">
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <button onClick={() => navigate('/login')} className="btn btn-small btn-secondary">
                  로그인
                </button>
                <button onClick={() => navigate('/register')} className="btn btn-small btn-primary">
                  회원가입
                </button>
              </>
            )}
          </div>
        </div>
      </header>

      <div className="main-content">
        <nav className="tab-navigation">
          <button
            className={`tab-button ${activeTab === 'home' ? 'active' : ''}`}
            onClick={() => setActiveTab('home')}
          >
            홈
          </button>
          <button
            className={`tab-button ${activeTab === 'search' ? 'active' : ''}`}
            onClick={() => setActiveTab('search')}
          >
            검색
          </button>
          <button
            className={`tab-button ${activeTab === 'community' ? 'active' : ''}`}
            onClick={() => setActiveTab('community')}
          >
            커뮤니티
          </button>
          <button
            className={`tab-button ${activeTab === 'map' ? 'active' : ''}`}
            onClick={() => setActiveTab('map')}
          >
            지도
          </button>
        </nav>

        <div className="tab-content">
          {renderTabContent()}
        </div>
      </div>

      {/* Floating Action Button for Community posts (only on community tab) */}
      {activeTab === 'community' && user && (
        <button
          className="fab"
          onClick={() => navigate('/create-post')}
          aria-label="새 글 작성"
        >
          +
        </button>
      )}
    </div>
  );
}

export default Main;
