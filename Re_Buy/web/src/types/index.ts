export interface Product {
  id?: string;
  title: string;
  price: string;
  link: string;
  image: string;
  center: string;
  category: string;
  createdAt: string;
  crawledAt: string;
  sourceUrl: string;
  status: ProductStatus;
  condition: ProductCondition;
  quantity: number;
  description: string;
  centerId: string;
  images: string[];
  lastUpdated: Date | null;
  reservedBy: string;
  reservedUntil: Date | null;
}

export enum ProductStatus {
  AVAILABLE = 'AVAILABLE',
  RESERVED = 'RESERVED',
  SOLD = 'SOLD',
  PICKED_UP = 'PICKED_UP',
  REMOVED = 'REMOVED'
}

export enum ProductCondition {
  LIKE_NEW = 'LIKE_NEW',
  EXCELLENT = 'EXCELLENT',
  GOOD = 'GOOD',
  FAIR = 'FAIR',
  AS_IS = 'AS_IS'
}

export interface CommunityPost {
  id?: string;
  userId: string;
  userEmail: string;
  title: string;
  content: string;
  region: string;
  createdAt: Date;
  updatedAt: Date;
  likes: number;
  commentCount: number;
  imageUrls: string[];
}

export interface Comment {
  id?: string;
  postId: string;
  userId: string;
  userEmail: string;
  content: string;
  createdAt: Date;
  likes: number;
}

export interface Favorite {
  id?: string;
  userId: string;
  productId: string;
  addedAt: Date;
}

export interface Purchase {
  id?: string;
  userId: string;
  productId: string;
  productTitle: string;
  productPrice: string;
  productImage: string;
  purchaseDate: Date;
  status: string;
}

export interface ImpactMetrics {
  userId: string;
  totalPurchases: number;
  co2Saved: number;
  waterSaved: number;
  energySaved: number;
  wasteDiverted: number;
}

export interface RecyclingCenter {
  id?: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  phone: string;
  hours: string;
  region: string;
}

export interface Achievement {
  id?: string;
  userId: string;
  title: string;
  description: string;
  icon: string;
  unlockedAt: Date;
  category: string;
}

export const conditionLabels: Record<ProductCondition, { name: string; desc: string }> = {
  [ProductCondition.LIKE_NEW]: { name: '거의 새것', desc: '사용감이 거의 없는 상태' },
  [ProductCondition.EXCELLENT]: { name: '최상', desc: '약간의 사용감은 있으나 기능과 외관이 우수함' },
  [ProductCondition.GOOD]: { name: '양호', desc: '정상 작동하며 사용감이 있음' },
  [ProductCondition.FAIR]: { name: '보통', desc: '작동하나 외관상 흠집이나 손상이 있음' },
  [ProductCondition.AS_IS]: { name: '현재상태', desc: '작동 미확인 또는 수리 필요' }
};
