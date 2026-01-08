// Mock Data for PetGuardian

const categories = [
    { id: 'walk', name: '散步', icon: '🦮' },
    { id: 'clean', name: '清潔', icon: '🧹' },
    { id: 'bath', name: '洗澡', icon: '🛁' },
    { id: 'groom', name: '美容', icon: '✂️' }
];

const sitters = [
    {
        id: 1,
        name: '王小美',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
        rating: 4.9,
        reviews: 120,
        location: '台北市大安區',
        services: ['walk', 'bath'],
        price: 300,
        desc: '擁有3年養狗經驗，非常有耐心，愛狗如命！'
    },
    {
        id: 2,
        name: '陳大山',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka',
        rating: 4.8,
        reviews: 85,
        location: '新北市板橋區',
        services: ['walk', 'groom', 'clean'],
        price: 500,
        desc: '專業寵物美容師退役，提供最專業的服務。'
    },
    {
        id: 3,
        name: '林沐沐',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Coco',
        rating: 5.0,
        reviews: 42,
        location: '台北市信義區',
        services: ['clean', 'walk'],
        price: 250,
        desc: '週末兼職，喜歡貓貓狗狗，家裡有兩隻布偶貓。'
    }
];

const products = [
    {
        id: 101,
        title: '全新未拆封 貓咪自動餵食器',
        price: 1200,
        image: 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&q=80&w=400',
        seller: 'CatLover99',
        category: '用品'
    },
    {
        id: 102,
        title: '二手大型犬外出籠 (九成新)',
        price: 800,
        image: 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?auto=format&fit=crop&q=80&w=400',
        seller: 'DoggyDad',
        category: '外出'
    },
    {
        id: 103,
        title: '寵物保暖墊 (冬季必備)',
        price: 350,
        image: 'https://images.unsplash.com/photo-1520038410233-7141dd782f08?auto=format&fit=crop&q=80&w=400',
        seller: 'CozyPet',
        category: '寢具'
    }
];

const currentUser = {
    id: 'u123',
    name: '李小明',
    email: 'ming@example.com',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack',
    pits: [
        { id: 'p1', name: '皮皮', type: 'Dog', breed: '柯基', age: 3 },
        { id: 'p2', name: '咪咪', type: 'Cat', breed: '米克斯', age: 2 }
    ]
};
