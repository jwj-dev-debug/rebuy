import { useState, useEffect } from 'react';
import { collection, getDocs } from 'firebase/firestore';
import { db } from '../firebase/config';
import { RecyclingCenter } from '../types';
import './Map.css';

function Map() {
  const [centers, setCenters] = useState<RecyclingCenter[]>([]);
  const [selectedCenter, setSelectedCenter] = useState<RecyclingCenter | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCenters();
  }, []);

  const loadCenters = async () => {
    try {
      const centersRef = collection(db, 'recycling_centers');
      const snapshot = await getDocs(centersRef);

      const centersData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as RecyclingCenter[];

      setCenters(centersData);
    } catch (error) {
      console.error('Error loading centers:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="map-container">
      <h2 className="map-title">재활용 센터 위치</h2>

      <div className="map-content">
        <div className="map-placeholder">
          <p>지도 API 연동 필요</p>
          <p className="map-subtitle">Google Maps API를 설정하면 지도가 표시됩니다</p>
        </div>

        <div className="centers-list">
          <h3 className="list-title">재활용 센터 목록</h3>
          {loading ? (
            <div className="loading-small">
              <div className="spinner"></div>
            </div>
          ) : centers.length === 0 ? (
            <div className="empty-small">
              <p>등록된 센터가 없습니다.</p>
            </div>
          ) : (
            <div className="centers-items">
              {centers.map((center) => (
                <div
                  key={center.id}
                  className={`center-card ${selectedCenter?.id === center.id ? 'selected' : ''}`}
                  onClick={() => setSelectedCenter(center)}
                >
                  <h4 className="center-name">{center.name}</h4>
                  <p className="center-address">{center.address}</p>
                  {center.phone && <p className="center-phone">📞 {center.phone}</p>}
                  {center.hours && <p className="center-hours">🕐 {center.hours}</p>}
                  <span className="center-region-tag">
                    {center.region === 'dongdaemun' ? '동대문구' : '서대문구'}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {selectedCenter && (
        <div className="selected-center-info">
          <h3>{selectedCenter.name}</h3>
          <p><strong>주소:</strong> {selectedCenter.address}</p>
          {selectedCenter.phone && <p><strong>전화:</strong> {selectedCenter.phone}</p>}
          {selectedCenter.hours && <p><strong>운영시간:</strong> {selectedCenter.hours}</p>}
        </div>
      )}
    </div>
  );
}

export default Map;
