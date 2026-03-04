import { useState, useEffect } from 'react';
import { User } from 'firebase/auth';
import { useNavigate } from 'react-router-dom';
import { doc, getDoc } from 'firebase/firestore';
import { db } from '../firebase/config';
import { ImpactMetrics } from '../types';
import './ImpactDashboard.css';

interface ImpactDashboardProps {
  user: User;
}

function ImpactDashboard({ user }: ImpactDashboardProps) {
  const [metrics, setMetrics] = useState<ImpactMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadMetrics();
  }, [user]);

  const loadMetrics = async () => {
    try {
      const metricsDoc = await getDoc(doc(db, 'impact_metrics', user.uid));
      if (metricsDoc.exists()) {
        setMetrics(metricsDoc.data() as ImpactMetrics);
      } else {
        // Create default metrics
        setMetrics({
          userId: user.uid,
          totalPurchases: 0,
          co2Saved: 0,
          waterSaved: 0,
          energySaved: 0,
          wasteDiverted: 0
        });
      }
    } catch (error) {
      console.error('Error loading metrics:', error);
    } finally {
      setLoading(false);
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

  return (
    <div className="impact-container">
      <button onClick={() => navigate(-1)} className="back-button">
        ← 뒤로 가기
      </button>

      <div className="impact-content">
        <div className="impact-header">
          <h1 className="impact-title">환경 영향 대시보드</h1>
          <p className="impact-subtitle">
            재활용 상품 구매로 당신이 만든 긍정적인 변화를 확인하세요
          </p>
        </div>

        <div className="impact-stats">
          <div className="impact-card">
            <div className="impact-icon">🌍</div>
            <div className="impact-value">{metrics?.co2Saved.toFixed(1) || 0} kg</div>
            <div className="impact-label">CO2 감소</div>
            <div className="impact-desc">탄소 배출 저감량</div>
          </div>

          <div className="impact-card">
            <div className="impact-icon">💧</div>
            <div className="impact-value">{metrics?.waterSaved.toFixed(0) || 0} L</div>
            <div className="impact-label">물 절약</div>
            <div className="impact-desc">절약된 물 사용량</div>
          </div>

          <div className="impact-card">
            <div className="impact-icon">⚡</div>
            <div className="impact-value">{metrics?.energySaved.toFixed(1) || 0} kWh</div>
            <div className="impact-label">에너지 절약</div>
            <div className="impact-desc">절약된 에너지</div>
          </div>

          <div className="impact-card">
            <div className="impact-icon">♻️</div>
            <div className="impact-value">{metrics?.wasteDiverted.toFixed(1) || 0} kg</div>
            <div className="impact-label">폐기물 감소</div>
            <div className="impact-desc">매립 방지된 폐기물</div>
          </div>
        </div>

        <div className="impact-info">
          <h2 className="info-title">총 구매 수: {metrics?.totalPurchases || 0}개</h2>
          <p className="info-text">
            재활용 상품을 구매하면 환경 보호에 기여할 수 있습니다.
            새 제품 생산에 필요한 자원과 에너지를 절약하고,
            폐기물을 줄여 지구를 더 깨끗하게 만듭니다.
          </p>
        </div>

        <div className="impact-achievements">
          <h2 className="achievements-title">달성 업적</h2>
          <div className="achievements-grid">
            <div className={`achievement-badge ${(metrics?.totalPurchases || 0) >= 1 ? 'unlocked' : 'locked'}`}>
              <div className="badge-icon">🏆</div>
              <div className="badge-name">첫 구매</div>
            </div>
            <div className={`achievement-badge ${(metrics?.totalPurchases || 0) >= 5 ? 'unlocked' : 'locked'}`}>
              <div className="badge-icon">🌟</div>
              <div className="badge-name">재활용 초보</div>
            </div>
            <div className={`achievement-badge ${(metrics?.totalPurchases || 0) >= 10 ? 'unlocked' : 'locked'}`}>
              <div className="badge-icon">💚</div>
              <div className="badge-name">환경 보호자</div>
            </div>
            <div className={`achievement-badge ${(metrics?.totalPurchases || 0) >= 20 ? 'unlocked' : 'locked'}`}>
              <div className="badge-icon">🌱</div>
              <div className="badge-name">지구 지킴이</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ImpactDashboard;
