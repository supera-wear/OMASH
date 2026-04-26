import React, { useState, useEffect, createContext, useContext, useRef, cloneElement } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  ShoppingBag, 
  Menu as MenuIcon, 
  X, 
  ChevronRight,
  ChevronLeft,
  Star, 
  Clock, 
  MapPin, 
  Phone, 
  Search,
  Instagram, 
  Facebook, 
  Twitter,
  ArrowRight,
  Plus,
  Minus,
  Info,
  Globe,
  User,
  LogIn,
  LogOut,
  Mail,
  Lock,
  CreditCard,
  History,
  Activity,
  Zap,
  Flame,
  Award,
  Save,
  Loader2,
  Youtube,
  MessageCircle,
  MessageSquare,
  Navigation,
  Wheat,
  Egg,
  Fish,
  Milk,
  Bean,
  Shell,
  FlaskConical,
  Flower2,
  CircleDot,
  ChefHat,
  ShieldCheck,
  Leaf,
  HeartHandshake,
  Target,
  TrendingUp,
  Building2,
  Globe2
} from 'lucide-react';
import { cn } from './lib/utils';
import { MENU_ITEMS, MenuItem } from './data/menu';
import { auth, googleProvider, db } from './firebase';
import { 
  onAuthStateChanged, 
  signInWithPopup, 
  signOut, 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword,
  User as FirebaseUser 
} from 'firebase/auth';
import { 
  doc, 
  getDoc, 
  getDocs,
  setDoc, 
  updateDoc, 
  onSnapshot, 
  collection, 
  query, 
  where, 
  orderBy, 
  addDoc, 
  serverTimestamp,
  increment,
  Timestamp
} from 'firebase/firestore';

// --- Constants ---
const HardalIcon = ({ className }: { className?: string }) => (
  <svg 
    xmlns="http://www.w3.org/2000/svg" 
    viewBox="0 0 100 100" 
    fill="none" 
    stroke="currentColor" 
    strokeWidth="8" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    <g transform="translate(50, 50) rotate(32) scale(0.85) translate(-50, -50)">
      {/* Spout */}
      <path d="M42 27 L46 10 C 47 4, 53 4, 54 10 L 58 27" />
      {/* Cap */}
      <rect x="25" y="27" width="50" height="10" rx="4" />
      {/* Body */}
      <path d="M33 37 C 25 50, 16 70, 16 85 C 26 100, 74 100, 84 85 C 84 70, 75 50, 67 37" />
      {/* Label Box inside */}
      <path d="M32 54 L 43 54 A 7 7 0 0 1 57 54 L 68 54 L 73 80 C 60 88, 40 88, 27 80 Z" />
    </g>
  </svg>
);

const SusamIcon = ({ className }: { className?: string }) => (
  <svg 
    xmlns="http://www.w3.org/2000/svg" 
    viewBox="0 0 100 100" 
    fill="none" 
    stroke="currentColor" 
    strokeWidth="7" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    <defs>
      {/* slightly thicker bottom seed shape based on the image */}
      <path id="seed" d="M 0,-15 C 10,-3 13,11 0,16 C -13,11 -10,-3 0,-15 Z" />
    </defs>
    {/* Background mask to prevent stroke overlap inside the cluster if they touch */}
    <g transform="translate(50, 50) scale(0.95)">
      {/* Top Left Seed - pointing right */}
      <use href="#seed" x="-20" y="-18" transform="rotate(75 -20 -18)" />
      
      {/* Top Right Seed - pointing up-left */}
      <use href="#seed" x="22" y="-5" transform="rotate(-30 22 -5)" />
      
      {/* Bottom Left Seed - pointing up-right */}
      <use href="#seed" x="-12" y="16" transform="rotate(40 -12 16)" />
      
      {/* Bottom Right Seed - pointing up-left, slightly touching bottom-left */}
      <use href="#seed" x="14" y="26" transform="rotate(-35 14 26)" />
    </g>
  </svg>
);

const PeanutIcon = ({ className }: { className?: string }) => (
  <svg 
    xmlns="http://www.w3.org/2000/svg" 
    viewBox="0 0 100 100" 
    fill="none" 
    stroke="currentColor" 
    strokeWidth="6" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    <g transform="translate(50, 50) rotate(45) scale(0.9) translate(-50, -50)">
      <defs>
        {/* Defining the shell path once so it can be used for both rendering and clipping */}
        <path id="peanut-shell" d="M50 6 C72 6 80 28 64 42 C59 47 59 53 64 58 C80 72 72 94 50 94 C28 94 20 72 36 58 C41 53 41 47 36 42 C20 28 28 6 50 6 Z" />
        <clipPath id="peanut-clip">
          <use href="#peanut-shell" />
        </clipPath>
      </defs>

      {/* Grid lines placed INSIDE the mask constraint */}
      <g clipPath="url(#peanut-clip)">
        {/* Lengthwise curved grid lines drawn well past the edges to ensure no gaps */}
        <path d="M 32 -10 Q 50 50 32 110" strokeWidth="5" />
        <path d="M 68 -10 Q 50 50 68 110" strokeWidth="5" />
        
        {/* Crosswise grid lines drawn well past edges */}
        <path d="M -10 26 Q 50 36 110 26" strokeWidth="5" />
        <path d="M -10 50 L 110 50" strokeWidth="5" />
        <path d="M -10 74 Q 50 64 110 74" strokeWidth="5" />
      </g>
      
      {/* Shell Outer Boundary drawn ON TOP to cap off the clipped edges seamlessly */}
      <use href="#peanut-shell" />
    </g>
  </svg>
);

const ALLERGEN_ICONS: Record<string, { icon: React.ReactNode; nameEn: string; nameTr: string; descEn: string; descTr: string }> = {
  gluten: { 
    icon: <Wheat className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Gluten', 
    nameTr: 'Glüten',
    descEn: 'Cereals containing gluten, namely: wheat, rye, barley, oats, spelt, kamut or their hybridised strains, and products thereof.',
    descTr: 'Glüten içeren tahıllar: buğday, çavdar, arpa, yulaf, kılçıksız buğday, kamut veya bunların melez türleri ve bunların ürünleri.'
  },
  crustaceans: { 
    icon: <Shell className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Crustaceans', 
    nameTr: 'Kabuklular',
    descEn: 'Crustaceans and products thereof (e.g. crabs, lobster, prawns, shrimp).',
    descTr: 'Kabuklular ve bunların ürünleri (örn. yengeç, ıstakoz, karides vb.).'
  },
  eggs: { 
    icon: <Egg className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Eggs', 
    nameTr: 'Yumurta',
    descEn: 'Eggs and products thereof.',
    descTr: 'Yumurta ve yumurta ürünleri.'
  },
  fish: { 
    icon: <Fish className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Fish', 
    nameTr: 'Balık',
    descEn: 'Fish and products thereof.',
    descTr: 'Balık ve balık ürünleri.'
  },
  peanuts: { 
    icon: <PeanutIcon className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Peanuts', 
    nameTr: 'Yerfıstığı',
    descEn: 'Peanuts and products thereof.',
    descTr: 'Yerfıstığı ve yerfıstığı ürünleri.'
  },
  soy: { 
    icon: <Bean className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Soy', 
    nameTr: 'Soya',
    descEn: 'Soybeans and products thereof.',
    descTr: 'Soya fasulyesi ve bunların ürünleri.'
  },
  dairy: { 
    icon: <Milk className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Dairy', 
    nameTr: 'Süt Ürünü',
    descEn: 'Milk and products thereof (including lactose).',
    descTr: 'Süt ve süt ürünleri (laktaz dahil).'
  },
  nuts: { 
    icon: <CircleDot className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Nuts', 
    nameTr: 'Sert Kabuklu Meyveler',
    descEn: 'Nuts, namely: almonds, hazelnuts, walnuts, cashews, pecan nuts, Brazil nuts, pistachio nuts, macadamia or Queensland nuts, and products thereof.',
    descTr: 'Sert kabuklu meyveler: badem, fındık, ceviz, kaju fıstığı, pikan cevizi, Brezilya fındığı, Antep fıstığı, makademya fındığı ve bunların ürünleri.'
  },
  celery: { 
    icon: <Bean className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Celery', 
    nameTr: 'Kereviz',
    descEn: 'Celery and products thereof.',
    descTr: 'Kereviz ve kereviz ürünleri.'
  },
  mustard: { 
    icon: <HardalIcon className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Mustard', 
    nameTr: 'Hardal',
    descEn: 'Mustard and products thereof.',
    descTr: 'Hardal ve hardal ürünleri.'
  },
  sesame: { 
    icon: <SusamIcon className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Sesame', 
    nameTr: 'Susam',
    descEn: 'Sesame seeds and products thereof.',
    descTr: 'Susam tohumu ve bunların ürünleri.'
  },
  sulphites: { 
    icon: <FlaskConical className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Sulphites', 
    nameTr: 'Sülfitler',
    descEn: 'Sulphur dioxide and sulphites at concentrations of more than 10 mg/kg or 10 mg/litre in terms of the total SO2.',
    descTr: 'Kükürt dioksit ve sülfitler (toplam SO2 cinsinden 10 mg/kg veya 10 mg/L\'den fazla konsantrasyonlarda).'
  },
  lupin: { 
    icon: <Flower2 className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Lupin', 
    nameTr: 'Acı Bakla',
    descEn: 'Lupin and products thereof.',
    descTr: 'Acı bakla (Lupin) ve ürünleri.'
  },
  molluscs: { 
    icon: <Shell className="w-5 h-5 text-brand-red" />, 
    nameEn: 'Molluscs', 
    nameTr: 'Yumuşakçalar',
    descEn: 'Molluscs and products thereof (e.g. squid, mussels, oysters).',
    descTr: 'Yumuşakçalar ve bunların ürünleri (örn. kalamar, midye, istiridye vb.).'
  }
};

const LOCATIONS = [
  { id: 1, name: 'OMASH Beşiktaş', address: 'Sinanpaşa, Beşiktaş Cd. No:1, 34353 Beşiktaş/İstanbul', lat: 41.0428, lng: 29.0075 },
  { id: 2, name: 'OMASH Kadıköy', address: 'Caferağa, Mühürdar Cd. No:20, 34710 Kadıköy/İstanbul', lat: 40.9910, lng: 29.0260 },
  { id: 3, name: 'OMASH Şişli', address: 'Merkez, Büyükdere Cd. No:136, 34360 Şişli/İstanbul', lat: 41.0635, lng: 28.9850 },
  { id: 4, name: 'OMASH Bakırköy', address: 'Zeytinlik, Ebuzziya Cd. No:15, 34140 Bakırköy/İstanbul', lat: 40.9775, lng: 28.8720 },
];

const INFO_CONTENT: Record<string, { titleEn: string; titleTr: string; contentEn: string[]; contentTr: string[] }> = {
  kvkk: {
    titleEn: 'Personal Data Protection',
    titleTr: 'Kişisel Verilerin Korunması',
    contentEn: [
      'In accordance with the Law on the Protection of Personal Data (KVKK) No. 6698, OMASH processes your personal data transparently, securely, and purely for intended business purposes.',
      'We collect data strictly required to deliver orders, maintain service quality, and verify user identity. This includes your name, location, order history, and contact information.',
      'OMASH implements appropriate technical and administrative measures to ensure a sufficient level of security. Your data is not shared with third parties without your explicit consent, unless absolutely required by legal proceedings.',
      'If you wish to view, update, or permanently delete your stored personal data, you may contact our data protection team at any time.'
    ],
    contentTr: [
      '6698 sayılı Kişisel Verilerin Korunması Kanunu (KVKK) uyarınca OMASH, kişisel verilerinizi şeffaf, güvenli ve yalnızca amaçlanan ticari hedefler doğrultusunda işler.',
      'Yalnızca siparişlerin tesliminde, hizmet kalitesinin devamlılığında ve kullanıcı kimliğinin doğrulanmasında zorunlu olan verileri topluyoruz. Bunlar adınız, lokasyonunuz, sipariş geçmişiniz ve iletişim bilgilerinizdir.',
      'OMASH, yeterli güvenlik seviyesini sağlamak adına gereken teknik ve idari tedbirleri uygular. Verileriniz, yasal zorunluluklar dışında, açık rızanız olmadan üçüncü şahıslarla asla paylaşılmaz.',
      'İstediğiniz zaman kayıtlı kişisel verilerinizi görüntülemek, güncellemek veya kalıcı olarak silmek isterseniz, veri koruma ekibimizle iletişime geçebilirsiniz.'
    ]
  },
  privacy: {
    titleEn: 'Privacy Policy',
    titleTr: 'Gizlilik Politikası',
    contentEn: [
      'We deeply respect your privacy. This Privacy Policy details how your personal information is collected, used, and safeguarded when using the OMASH platform.',
      'By utilizing our web applications and ordering endpoints, you agree to the collection and use of information pertaining to this policy. We do not engage in unauthorized selling or distributing of user-identified profiles.',
      'Usage metrics and analytics are processed purely to improve the performance and taste metrics of OMASH ecosystem.'
    ],
    contentTr: [
      'Gizliliğinize büyük saygı duyuyoruz. Bu Gizlilik Politikası, OMASH platformunu kullanırken kişisel bilgilerinizin nasıl toplandığını, kullanıldığını ve korunduğunu detaylandırmaktadır.',
      'Uygulamalarımızı ve sipariş altyapımızı kullanarak, bu politikayla bağlantılı bilgilerin toplanmasını kabul etmiş olursunuz. Kullanıcı kimliğine dayalı profilleri izinsiz satmıyor veya dağıtmıyoruz.',
      'Kullanım metrikleri, yalnızca OMASH ekosisteminin performansını iyileştirmek amacıyla işlenir.'
    ]
  },
  terms: {
    titleEn: 'Terms of Service',
    titleTr: 'Kullanım Koşulları',
    contentEn: [
      'By accessing and ordering through OMASH services, you agree to comply with our binding terms and conditions. These govern your authorized access to our platforms.',
      'All listed prices, deals, and menu items are subject to change without prior notice depending on regional availability and stock restrictions.',
      'OMASH reserves the right to cancel or refund orders if fraudulent activity, service outages, or unforeseen technical problems occur.'
    ],
    contentTr: [
      'OMASH hizmetlerine erişerek ve sipariş vererek, bağlayıcı şartlarımıza ve koşullarımıza uymayı kabul ediyorsunuz. Bunlar platformlarımıza olan yetkili erişiminizi düzenler.',
      'Listelenen tüm fiyatlar, fırsatlar ve menü öğeleri, bölgesel uygunluk ve stok kısıtlamalarına bağlı olarak önceden haber verilmeksizin değiştirilebilir.',
      'OMASH, hileli faaliyetler, hizmet kesintileri veya öngörülemeyen teknik sorunlar meydana gelmesi durumunda siparişleri iptal etme veya iade etme hakkını saklı tutar.'
    ]
  },
  hygiene: {
    titleEn: 'Hygiene Policy',
    titleTr: 'Hijyen Politikası',
    contentEn: [
      'At OMASH, absolute hygiene is our foundational priority. We seamlessly integrate rigorous food safety protocols across all our branches and ghost kitchens.',
      'All working surfaces are sterilized at steady intervals. Our operational staff conducts daily health checks and attends mandatory hygiene reinforcement training each quarter.',
      'From ingredient sourcing to final custom packaging, we adhere to recognized international sanitary standards.'
    ],
    contentTr: [
      'OMASH\'ta mutlak hijyen temel önceliğimizdir. Sıkı gıda güvenliği protokollerini tüm şubelerimize sorunsuz bir şekilde entegre ediyoruz.',
      'Tüm çalışma yüzeyleri düzenli aralıklarla sterilize edilir. Operasyonel personelimiz günlük sağlık kontrollerinden geçer ve her çeyrekte zorunlu hijyen eğitimlerine katılır.',
      'Malzeme tedarikinden nihai paketlemeye kadar, tanınmış uluslararası hijyen standartlarına uyuyoruz.'
    ]
  },
  halal: {
    titleEn: 'Halal Certificates',
    titleTr: 'Helal Belgeleri',
    contentEn: [
      'We proudly affirm that ALL our meat and poultry products are exclusively sourced from 100% certified Halal suppliers.',
      'OMASH maintains full transparency regarding its supply chain and storage protocols. Certified documentation mapping our exact butcher pipelines can be presented physically upon request at any branch location.',
      'Cross-contamination protocols ensure that our preparation stations remain strictly compliant with Islamic dietary laws.'
    ],
    contentTr: [
      'TÜM et ve tavuk ürünlerimizin %100 sertifikalı helal tedarikçilerden temin edildiğini gururla beyan ederiz.',
      'OMASH, tedarik zinciri ve depolama protokolleri konusunda tam şeffaflığı korur. Tam kasap boru hatlarımızı haritalandıran sertifikalı belgeler, herhangi bir şube lokasyonunda talep üzerine fiziksel olarak sunulabilir.',
      'Çapraz bulaşma protokolleri, hazırlık istasyonlarımızın İslami beslenme kurallarına kesinlikle uygun kalmasını garanti eder.'
    ]
  },
  allergen: {
    titleEn: 'Allergen Information',
    titleTr: 'Alerjen Bilgilendirmesi',
    contentEn: [
      'In compliance with food safety regulations, we actively monitor and label the presence of the 14 major allergens.',
      'These include: 1) Cereals containing gluten, 2) Crustaceans, 3) Eggs, 4) Fish, 5) Peanuts, 6) Soybeans, 7) Milk & dairy, 8) Nuts, 9) Celery, 10) Mustard, 11) Sesame seeds, 12) Sulphur dioxide, 13) Lupin, 14) Molluscs.',
      'Even when not explicitly listed, traces may be present in our kitchens. Please inform our staff prior to ordering if you suffer from severe allergies.'
    ],
    contentTr: [
      'Gıda güvenliği düzenlemelerine uygun olarak, ürünlerimizdeki 14 temel alerjenin varlığını aktif olarak izleyip listeliyoruz.',
      'Bunlar şunlardır: 1) Glüten içeren tahıllar, 2) Kabuklular, 3) Yumurta, 4) Balık, 5) Yer fıstığı, 6) Soya, 7) Süt ürünleri, 8) Kuruyemişler, 9) Kereviz, 10) Hardal, 11) Susam tohumu, 12) Kükürt dioksit, 13) Acı bakla, 14) Yumuşakçalar.',
      'Açıkça listelenmemiş olsa bile, mutfaklarımızda eser miktarda çapraz bulaşmalar meydana gelebilir. Şiddetli alerjiniz varsa sipariş vermeden önce personelimize bildiriniz.'
    ]
  },
  investment: {
    titleEn: 'Investment Opportunities',
    titleTr: 'Yatırım Fırsatları',
    contentEn: [
      'The OMASH network is expanding. We offer scalable franchise architectures and investment tiers for ambitious operators looking to dominate the fast-casual burger market.',
      'Our centralized operational backbone, high-retention brand identity, and optimized logistics provide our partners with industry-leading profit margins.',
      'For more detailed franchising applications, prospectus materials, and ROI metrics, reach out to our corporate expansion team.'
    ],
    contentTr: [
      'OMASH ağı genişliyor. Hızlı tüketim (fast-casual) burger pazarında lider olmayı hedefleyen iddialı işletmeciler için ölçeklenebilir franchise mimarileri sunuyoruz.',
      'Merkezi operasyonel altyapımız, güçlü marka kimliğimiz ve optimize edilmiş lojistiğimiz, iş ortaklarımıza sektör lideri kar marjları sağlar.',
      'Daha detaylı franchising başvuruları, tanıtım materyalleri ve yatırım getirisi (ROI) metrikleri için kurumsal genişleme ekibimize ulaşın.'
    ]
  },
  disclosure: {
    titleEn: 'Responsible Disclosure',
    titleTr: 'Sorumlu Bildirim',
    contentEn: [
      'OMASH greatly values the global security community. We openly encourage the responsible discovery and reporting of security vulnerabilities within our infrastructure (Web, Mobile, Backend).',
      'Should you uncover any exploitable logic flaw, please report it to us immediately rather than disclosing it publicly.',
      'We reward coordinated disclosure that helps keep our ecosystem and our customers perfectly safe.'
    ],
    contentTr: [
      'OMASH, küresel güvenlik topluluğuna büyük değer verir. Altyapımızdaki (Web, Mobil, Arka Uç) güvenlik açıklarının sorumlu bir şekilde keşfedilmesini ve bildirilmesini açıkça destekliyoruz.',
      'İstismar edilebilir bir eksiklik ortaya çıkarırsanız, bunu kamuya açıklamak yerine lütfen hemen bize bildirin.',
      'Tüm sistemimizi ve müşterilerimizi güvende tutmamıza katkı sağlayan bu bildirimleri dikkate alıyor ve takdir ediyoruz.'
    ]
  },
  society: {
    titleEn: 'Information Society Services',
    titleTr: 'Bilgi Toplum Hizmetleri',
    contentEn: [
      'As required by Turkish commercial codes for digital service providers, detailed legal entity information, auditing authorities, and registration figures are digitally archived.',
      'OMASH Gıda A.Ş. operates entirely under verifiable corporate taxation infrastructures. Contact our legal entities strictly for compliance requirements or information audits.'
    ],
    contentTr: [
      'Dijital hizmet sağlayıcıları için Türk Ticaret Kanunu\'nun gerektirdiği üzere, ayrıntılı tüzel kişi bilgileri, denetim makamları ve kayıt rakamları yasal olarak arşivlenmektedir.',
      'OMASH Gıda A.Ş. tamamen doğrulanabilir kurumlar vergisi altyapıları altında faaliyet göstermektedir. Sadece uyumluluk ve denetim gereksinimleri için yasal birimlerimizle iletişime geçebilirsiniz.'
    ]
  },
  satisfaction: {
    titleEn: '%100 Customer Satisfaction',
    titleTr: '%100 Müşteri Memnuniyeti',
    contentEn: [
      'Your satisfaction is our absolute guarantee. We pride ourselves on the perfection of every order sent from an OMASH kitchen.',
      'If you are not entirely satisfied with your meal, or encounter performance flaws with our applications, reach out directly. We value your feedback and strive to make every interaction better.'
    ],
    contentTr: [
      'Memnuniyetiniz kesin garantimizdir. Bir OMASH mutfağından gönderilen her siparişin mükemmelliğiyle gurur duyuyoruz.',
      'Yemeğinizden tamamen memnun kalmazsanız veya uygulamalarımızda herhangi bir sorunla karşılaşırsanız doğrudan bize ulaşın. Geri bildirimlerinize değer veriyor ve her etkileşimi daha iyi hale getirmeye çalışıyoruz.'
    ]
  },
  contactless: {
    titleEn: 'Contactless Delivery',
    titleTr: 'Temassız Teslimat',
    contentEn: [
      'For your safety and absolute convenience, we offer a strict 100% contactless delivery protocol for all addresses.',
      'Our courier fleets operate under stringent hygiene measures, keeping zero direct interactions upon request. Orders are sealed, tamper-proof, and can be left safely at your door.'
    ],
    contentTr: [
      'Güvenliğiniz ve mutlak rahatlığınız için, tüm adresler için katı bir \%100 temassız teslimat protokolü sunuyoruz.',
      'Kurye filolarımız sıkı hijyen önlemleri altında çalışır ve talebinize göre sıfır fiziksel etkileşim prensibini uygular. Siparişleriniz mühürlüdür ve güvenle kapınıza bırakılabilir.'
    ]
  }
};

// --- Types ---
interface ExtraIngredient {
  id: string;
  name: string;
  nameTr: string;
  price: number;
}

const EXTRA_INGREDIENTS: ExtraIngredient[] = [
  { id: 'extra-patty', name: 'Extra Patty', nameTr: 'Ekstra Köfte', price: 45 },
  { id: 'extra-cheese', name: 'Extra Cheese', nameTr: 'Ekstra Peynir', price: 15 },
  { id: 'bacon', name: 'Beef Bacon', nameTr: 'Dana Bacon', price: 25 },
  { id: 'caramelized-onion', name: 'Caramelized Onion', nameTr: 'Karamelize Soğan', price: 10 },
  { id: 'jalapeno', name: 'Jalapeño', nameTr: 'Jalapeño', price: 8 },
  { id: 'mushroom', name: 'Mushroom', nameTr: 'Mantar', price: 12 },
  { id: 'extra-sauce', name: 'Extra Sauce', nameTr: 'Ekstra Sos', price: 10 },
  { id: 'crispy-onion', name: 'Crispy Onion', nameTr: 'Çıtır Soğan', price: 8 },
];

const DRINK_OPTIONS = [
  { id: 'cola', name: 'OMASH COLA', nameTr: 'OMASH COLA' },
  { id: 'cola-zero', name: 'OMASH COLA ZERO', nameTr: 'OMASH COLA ZERO' },
  { id: 'orange', name: 'OMASH ORANGE', nameTr: 'OMASH ORANGE' },
  { id: 'lime', name: 'OMASH LIME BLAST', nameTr: 'OMASH LIME BLAST' },
  { id: 'ayran', name: 'AYRAN', nameTr: 'AYRAN' },
  { id: 'water', name: 'SU', nameTr: 'SU' },
];

interface UserProfile {
  uid: string;
  email: string;
  displayName?: string;
  phoneNumber?: string;
  address?: string;
  points: number;
  profilePictureUrl?: string; // New feature
}

interface Order {
  id: string;
  userId: string;
  items: any[];
  totalPrice: number;
  pointsEarned: number;
  createdAt: any;
  status: 'pending' | 'completed' | 'cancelled';
}

// --- Auth Context ---
const AuthContext = createContext<{
  user: FirebaseUser | null;
  profile: UserProfile | null;
  loading: boolean;
  loginWithGoogle: () => Promise<void>;
  logout: () => Promise<void>;
  updateProfile: (data: Partial<UserProfile>) => Promise<void>;
}>({
  user: null,
  profile: null,
  loading: true,
  loginWithGoogle: async () => {},
  logout: async () => {},
  updateProfile: async () => {},
});

const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<FirebaseUser | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let unsubProfile: (() => void) | null = null;
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      setUser(firebaseUser);
      
      if (unsubProfile) {
        unsubProfile();
        unsubProfile = null;
      }
      
      if (firebaseUser) {
        // Sync profile from Firestore
        const userDocRef = doc(db, 'users', firebaseUser.uid);
        
        // Use onSnapshot for real-time profile updates (like points)
        unsubProfile = onSnapshot(userDocRef, (docSnap) => {
          if (docSnap.exists()) {
            setProfile(docSnap.data() as UserProfile);
          } else {
            // Create initial profile if it doesn't exist
            const initialProfile: UserProfile = {
              uid: firebaseUser.uid,
              email: firebaseUser.email || '',
              displayName: firebaseUser.displayName || '',
              points: 0,
            };
            setDoc(userDocRef, initialProfile);
            setProfile(initialProfile);
          }
        });

        setLoading(false);
      } else {
        setProfile(null);
        setLoading(false);
      }
    });

    return () => {
      unsubscribe();
      if (unsubProfile) {
        unsubProfile();
      }
    };
  }, []);

  const loginWithGoogle = async () => {
    try {
      await signInWithPopup(auth, googleProvider);
    } catch (error) {
      console.error("Google Login Error:", error);
    }
  };

  const logout = async () => {
    try {
      await signOut(auth);
    } catch (error) {
      console.error("Logout Error:", error);
    }
  };

  const updateProfile = async (data: Partial<UserProfile>) => {
    if (!user) return;
    try {
      const userDocRef = doc(db, 'users', user.uid);
      await updateDoc(userDocRef, data);
    } catch (error) {
      console.error("Update Profile Error:", error);
      throw error;
    }
  };

  return (
    <AuthContext.Provider value={{ user, profile, loading, loginWithGoogle, logout, updateProfile }}>
      {children}
    </AuthContext.Provider>
  );
};

const useAuth = () => useContext(AuthContext);

// --- Cart Context ---
interface CartItem extends MenuItem {
  cartId: string;
  quantity: number;
  selectedExtras: ExtraIngredient[];
}

const CartContext = createContext<{
  cart: CartItem[];
  addToCart: (item: MenuItem, extras?: ExtraIngredient[]) => void;
  removeFromCart: (cartId: string) => void;
  updateQuantity: (cartId: string, delta: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalPrice: number;
}>({
  cart: [],
  addToCart: () => {},
  removeFromCart: () => {},
  updateQuantity: () => {},
  clearCart: () => {},
  totalItems: 0,
  totalPrice: 0,
});

const CartProvider = ({ children }: { children: React.ReactNode }) => {
  const [cart, setCart] = useState<CartItem[]>([]);

  const addToCart = (item: MenuItem, extras: ExtraIngredient[] = []) => {
    setCart(prev => {
      const extrasId = extras.map(e => e.id).sort().join(',');
      const cartId = `${item.id}-${extrasId}`;
      
      const existing = prev.find(i => i.cartId === cartId);
      if (existing) {
        return prev.map(i => i.cartId === cartId ? { ...i, quantity: i.quantity + 1 } : i);
      }
      return [...prev, { ...item, cartId, quantity: 1, selectedExtras: extras }];
    });
  };

  const removeFromCart = (cartId: string) => {
    setCart(prev => prev.filter(i => i.cartId !== cartId));
  };

  const updateQuantity = (cartId: string, delta: number) => {
    setCart(prev => prev.map(i => {
      if (i.cartId === cartId) {
        const newQty = Math.max(1, i.quantity + delta);
        return { ...i, quantity: newQty };
      }
      return i;
    }));
  };

  const clearCart = () => setCart([]);

  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = cart.reduce((sum, item) => {
    const extrasTotal = item.selectedExtras.reduce((s, e) => s + e.price, 0);
    return sum + ((item.price + extrasTotal) * item.quantity);
  }, 0);

  return (
    <CartContext.Provider value={{ cart, addToCart, removeFromCart, updateQuantity, clearCart, totalItems, totalPrice }}>
      {children}
    </CartContext.Provider>
  );
};

const useCart = () => useContext(CartContext);

// --- Language Context ---
type Language = 'en' | 'tr';
const LanguageContext = createContext<{
  lang: Language;
  setLang: (l: Language) => void;
  t: (en: string, tr: string) => string;
  formatPrice: (price: number) => string;
}>({
  lang: 'tr',
  setLang: () => {},
  t: (en) => en,
  formatPrice: (p) => `$${p.toFixed(2)}`,
});

const LanguageProvider = ({ children }: { children: React.ReactNode }) => {
  const [lang, setLang] = useState<Language>('tr');
  const t = (en: string, tr: string) => (lang === 'en' ? en : tr);
  const formatPrice = (price: number) => {
    if (lang === 'en') return `$${price.toFixed(2)}`;
    return `${price.toLocaleString('tr-TR', { minimumFractionDigits: 0, maximumFractionDigits: 0 })} TL`;
  };
  return (
    <LanguageContext.Provider value={{ lang, setLang, t, formatPrice }}>
      {children}
    </LanguageContext.Provider>
  );
};

const useTranslation = () => {
  const context = useContext(LanguageContext);
  return {
    ...context,
    formatPrice: context.formatPrice
  };
};

// --- Search Context ---
const SearchContext = createContext<{
  searchQuery: string;
  setSearchQuery: (query: string) => void;
}>({
  searchQuery: '',
  setSearchQuery: () => {},
});

const SearchProvider = ({ children }: { children: React.ReactNode }) => {
  const [searchQuery, setSearchQuery] = useState('');
  return (
    <SearchContext.Provider value={{ searchQuery, setSearchQuery }}>
      {children}
    </SearchContext.Provider>
  );
};

const useSearch = () => useContext(SearchContext);

// --- Components ---

const Logo = ({ className }: { className?: string }) => {
  const { t } = useTranslation();
  return (
    <div className={cn("flex items-center select-none", className)}>
      <div className="flex items-baseline">
        <span className="text-3xl md:text-5xl font-black italic tracking-tighter">OMASH</span>
        <span className="text-xs font-bold ml-0.5">®</span>
      </div>
    </div>
  );
};

const Navbar = ({ onOpenMenu, onOpenLogin, setCurrentPage, onOpenMobileMenu }: { onOpenMenu: () => void, onOpenLogin: () => void, setCurrentPage: (page: any) => void, onOpenMobileMenu: () => void }) => {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isSearchVisible, setIsSearchVisible] = useState(false);
  const { lang, setLang, t } = useTranslation();
  const { user, profile } = useAuth();
  const { totalItems } = useCart();
  const { searchQuery, setSearchQuery } = useSearch();

  useEffect(() => {
    const handleScroll = () => setIsScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <nav className={cn(
      "fixed top-0 left-0 right-0 z-50 transition-all duration-300 px-4 py-4 md:px-8",
      isScrolled ? "bg-brand-red/95 backdrop-blur-md shadow-lg py-3" : "bg-brand-red"
    )}>
      <div className="max-w-[1400px] mx-auto flex items-center">
        {/* Left: Logo */}
        <div className="flex-shrink-0 flex items-center">
          <button onClick={() => { setCurrentPage('home'); setSearchQuery(''); }} className="hover:opacity-90 transition-opacity text-white">
            <Logo />
          </button>
        </div>

        {/* Center: Links */}
        <div className="hidden lg:flex flex-1 items-center justify-center gap-8 font-bold text-lg text-white">
          <button onClick={() => { setCurrentPage('full-menu'); setSearchQuery(''); window.scrollTo({ top: 0, behavior: 'instant' as any }); }} className="hover:text-white/80 transition-colors uppercase tracking-tight whitespace-nowrap">{t('Menu', 'Menü')}</button>
          <button onClick={() => { setCurrentPage('locations'); setSearchQuery(''); window.scrollTo({ top: 0, behavior: 'instant' as any }); }} className="hover:text-white/80 transition-colors uppercase tracking-tight whitespace-nowrap">{t('Locations', 'Şubeler')}</button>
          <button onClick={() => { setCurrentPage('deals'); setSearchQuery(''); window.scrollTo({ top: 0, behavior: 'instant' as any }); }} className="hover:text-white/80 transition-colors uppercase tracking-tight whitespace-nowrap">{t('Deals', 'Fırsatlar')}</button>
        </div>
        
        {/* Right: Actions */}
        <div className="flex-1 flex items-center justify-end gap-2 md:gap-4">
          {/* Mobile Search Toggle */}
          <button 
            onClick={() => setIsSearchVisible(!isSearchVisible)}
            className="lg:hidden p-2 text-white hover:bg-white/10 rounded-full transition-colors"
          >
            {isSearchVisible ? <X className="w-6 h-6" /> : <Search className="w-6 h-6" />}
          </button>

          <div className="hidden md:flex items-center gap-4">
            {/* Search Bar (Desktop) */}
            <div className="hidden lg:flex w-64">
              <div className="relative w-full group">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-white/60 group-focus-within:text-white transition-colors" />
                <input 
                  type="text" 
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder={t('Search...', 'Ara...')}
                  className="w-full bg-white/10 border border-white/20 rounded-2xl py-2 pl-11 pr-10 text-white placeholder:text-white/60 focus:outline-none focus:bg-white/20 focus:border-white/40 transition-all font-medium text-sm"
                />
                {searchQuery && (
                  <button 
                    onClick={() => setSearchQuery('')}
                    className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-white/10 rounded-full transition-colors"
                  >
                    <X className="w-3 h-3 text-white/60 hover:text-white" />
                  </button>
                )}
              </div>
            </div>

            <button 
              onClick={() => setLang(lang === 'en' ? 'tr' : 'en')}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white/20 text-white hover:bg-white/30 transition-colors font-bold text-sm border border-white/10"
            >
              <Globe className="w-4 h-4" />
              {lang === 'en' ? 'TR' : 'EN'}
            </button>

            {user ? (
              <div className="flex items-center gap-3">
                <div className="hidden md:flex flex-col items-end">
                  <button 
                    onClick={() => setCurrentPage('profile')}
                    className="text-xs font-bold text-white truncate max-w-[120px] hover:text-white/80 transition-colors"
                  >
                    {profile?.displayName || user.displayName || user.email}
                  </button>
                </div>
                <button 
                  onClick={() => setCurrentPage('profile')}
                  className="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center border-2 border-white/30 overflow-hidden hover:scale-105 transition-transform"
                >
                  {user.photoURL ? (
                    <img src={user.photoURL} alt="Profile" className="w-full h-full object-cover" />
                  ) : (
                    <User className="w-5 h-5 text-white" />
                  )}
                </button>
              </div>
            ) : (
              <button 
                onClick={onOpenLogin}
                className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-white text-brand-red hover:bg-white/90 transition-all font-black text-sm shadow-sm"
              >
                <LogIn className="w-4 h-4" />
                <span className="hidden sm:inline">{t('Login', 'Giriş')}</span>
              </button>
            )}
          </div>
          
          <div 
            onClick={onOpenMenu}
            className="relative w-10 h-10 md:w-12 md:h-12 bg-brand-charcoal text-white rounded-full shadow-lg cursor-pointer flex items-center justify-center hover:scale-105 active:scale-95 transition-all border border-white/10"
          >
            <ShoppingBag className="w-5 h-5 md:w-6 md:h-6" />
            {totalItems > 0 && (
              <span className="absolute -top-1 -right-1 bg-white text-brand-red text-[10px] font-black w-4 h-4 md:w-5 md:h-5 flex items-center justify-center rounded-full border-2 border-brand-red">
                {totalItems}
              </span>
            )}
          </div>

          <button 
            onClick={onOpenMobileMenu}
            className="lg:hidden w-10 h-10 md:w-12 md:h-12 bg-white/20 text-white rounded-full flex items-center justify-center hover:bg-white/30 transition-colors border border-white/10"
          >
            <MenuIcon className="w-5 h-5 md:w-6 md:h-6" />
          </button>
        </div>
      </div>

      {/* Mobile Search Bar Overlay */}
      <AnimatePresence>
        {isSearchVisible && (
          <motion.div 
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="lg:hidden overflow-hidden mt-4"
          >
            <div className="relative group">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-white/60 group-focus-within:text-white transition-colors" />
              <input 
                autoFocus
                type="text" 
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder={t('Search for burgers, locations, FAQ...', 'Burger, şube, SSS ara...')}
                className="w-full bg-white/10 border border-white/20 rounded-2xl py-4 pl-11 pr-12 text-white placeholder:text-white/60 focus:outline-none focus:bg-white/20 focus:border-white/40 transition-all font-bold text-base"
              />
              {searchQuery && (
                <button 
                  onClick={() => setSearchQuery('')}
                  className="absolute right-4 top-1/2 -translate-y-1/2 p-2 hover:bg-white/10 rounded-full transition-colors"
                >
                  <X className="w-5 h-5 text-white/60 hover:text-white" />
                </button>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};

const MobileMenu = ({ isOpen, onClose, onOpenLogin, setCurrentPage }: { isOpen: boolean, onClose: () => void, onOpenLogin: () => void, setCurrentPage: (page: any) => void }) => {
  const { lang, setLang, t } = useTranslation();
  const { user, profile } = useAuth();
  const { searchQuery, setSearchQuery } = useSearch();

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-brand-charcoal/60 backdrop-blur-sm z-[80]"
          />
          <motion.div 
            initial={{ x: '-100%' }}
            animate={{ x: 0 }}
            exit={{ x: '-100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="fixed top-0 left-0 h-full w-full max-w-[300px] bg-white z-[90] shadow-2xl flex flex-col"
          >
            <div className="p-6 border-b flex items-center justify-between">
              <Logo className="text-brand-red" />
              <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-6 space-y-8">
              {/* Search Bar (Mobile) */}
              <div className="relative group">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-brand-red transition-colors" />
                <input 
                  type="text" 
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder={t('Search...', 'Ara...')}
                  className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-3.5 pl-11 pr-10 font-bold focus:outline-none focus:border-brand-red transition-colors text-sm"
                />
                {searchQuery && (
                  <button 
                    onClick={() => setSearchQuery('')}
                    className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-gray-100 rounded-full transition-colors"
                  >
                    <X className="w-4 h-4 text-gray-400 hover:text-brand-red" />
                  </button>
                )}
              </div>

              <div className="space-y-4">
                <button 
                  onClick={() => { setCurrentPage('full-menu'); onClose(); window.scrollTo({ top: 0, behavior: 'instant' as any }); }}
                  className="w-full text-left text-2xl font-black italic hover:text-brand-red transition-colors"
                >
                  {t('MENU', 'MENÜ')}
                </button>
                <button 
                  onClick={() => { setCurrentPage('locations'); onClose(); window.scrollTo({ top: 0, behavior: 'instant' as any }); }}
                  className="w-full text-left text-2xl font-black italic hover:text-brand-red transition-colors"
                >
                  {t('LOCATIONS', 'ŞUBELER')}
                </button>
                <button 
                  onClick={() => { setCurrentPage('deals'); onClose(); window.scrollTo({ top: 0, behavior: 'instant' as any }); }}
                  className="w-full text-left text-2xl font-black italic hover:text-brand-red transition-colors"
                >
                  {t('DEALS', 'FIRSATLAR')}
                </button>
              </div>
            </div>

            <div className="p-6 border-t border-gray-100 space-y-8">
              <div>
                <p className="text-xs font-black text-gray-400 uppercase tracking-widest mb-4">{t('LANGUAGE', 'DİL')}</p>
                <button 
                  onClick={() => setLang(lang === 'en' ? 'tr' : 'en')}
                  className="flex items-center justify-between px-4 py-3 rounded-2xl bg-brand-red text-white w-full font-bold shadow-lg shadow-brand-red/20 active:scale-95 transition-all"
                >
                  <div className="flex items-center gap-3">
                    <Globe className="w-5 h-5" />
                    <span>{lang === 'en' ? 'Türkçe (TR)' : 'English (EN)'}</span>
                  </div>
                  <ArrowRight className="w-5 h-5" />
                </button>
              </div>

              {user ? (
                <button 
                  onClick={() => { setCurrentPage('profile'); onClose(); }}
                  className="flex items-center gap-4 w-full p-3 rounded-2xl bg-gray-50 hover:bg-gray-100 transition-colors"
                >
                  <div className="w-12 h-12 rounded-full bg-brand-red/10 flex items-center justify-center border-2 border-brand-red/20 overflow-hidden">
                    {user.photoURL ? (
                      <img src={user.photoURL} alt="Profile" className="w-full h-full object-cover" />
                    ) : (
                      <User className="w-6 h-6 text-brand-red" />
                    )}
                  </div>
                  <div className="text-left">
                    <p className="font-black italic text-sm">{profile?.displayName || user.displayName || user.email}</p>
                    <p className="text-xs text-gray-500 font-bold">{t('View Profile', 'Profili Gör')}</p>
                  </div>
                </button>
              ) : (
                <button 
                  onClick={() => { onOpenLogin(); onClose(); }}
                  className="w-full bg-brand-charcoal text-white py-4 rounded-2xl font-black uppercase tracking-widest hover:bg-brand-red transition-colors flex items-center justify-center gap-2"
                >
                  <LogIn className="w-5 h-5" />
                  {t('LOGIN', 'GİRİŞ YAP')}
                </button>
              )}
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

const Hero = () => {
  const { t } = useTranslation();
  const [currentSlide, setCurrentSlide] = useState(0);

  const slides = [
    {
      image: "https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&q=80&w=2000",
      title: "UPGRADE YOUR",
      highlight: "PERFORMANCE.",
      subtitle: t(
        'SMASHED FRESH. LOADED FOR POWER.',
        'TAZE SMASH. GÜÇ İÇİN YÜKLENDİ.'
      )
    },
    {
      image: "https://images.unsplash.com/photo-1571091718767-18b5b1457add?auto=format&fit=crop&q=80&w=2000",
      title: "SMASHED TO",
      highlight: "PERFECTION.",
      subtitle: t(
        'HAND-PRESSED. NEVER FROZEN. ALWAYS JUICY.',
        'ELLE BASILMIŞ. ASLA DONDURULMAMIŞ. DAİMA SUYU İÇİNDE.'
      )
    },
    {
      image: "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?auto=format&fit=crop&q=80&w=2000",
      title: "THE ULTIMATE",
      highlight: "FLAVOR.",
      subtitle: t(
        'PREMIUM INGREDIENTS. LEGENDARY TASTE.',
        'BİRİNCİ SINIF MALZEMELER. EFSANE TAT.'
      )
    }
  ];

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length);
    }, 5000);
    return () => clearInterval(timer);
  }, [slides.length]);

  return (
    <section className="relative min-h-[50vh] md:min-h-[60vh] flex items-center pt-32 pb-8 overflow-hidden bg-brand-charcoal">
      <AnimatePresence initial={false}>
        <motion.div
          key={currentSlide}
          initial={{ x: '100%' }}
          animate={{ x: 0 }}
          exit={{ x: '-100%' }}
          transition={{ 
            x: { type: "spring", stiffness: 300, damping: 30 },
            opacity: { duration: 0.2 }
          }}
          className="absolute inset-0 z-0"
        >
          <img 
            key={slides[currentSlide].image}
            src={slides[currentSlide].image} 
            alt={slides[currentSlide].title} 
            className="w-full h-full object-cover brightness-[0.4]"
            referrerPolicy="no-referrer"
          />
        </motion.div>
      </AnimatePresence>
      
      <div className="container mx-auto px-4 md:px-8 relative z-10">
        <div className="max-w-3xl grid grid-cols-1 grid-rows-1">
          <AnimatePresence initial={false}>
            <motion.div
              key={currentSlide}
              initial={{ x: 100, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: -100, opacity: 0 }}
              transition={{ 
                x: { type: "spring", stiffness: 300, damping: 30 },
                opacity: { duration: 0.2 }
              }}
              className="col-start-1 row-start-1"
            >
              <h1 className="text-5xl md:text-8xl font-black text-white leading-[0.9] tracking-tighter mb-8 italic h-[2.5em] md:h-[2em]">
                <span className="block">{slides[currentSlide].title}</span>
                <span className="text-brand-red block">{slides[currentSlide].highlight}</span>
              </h1>
              <p className="text-lg md:text-2xl text-white/90 max-w-xl mb-10 font-black italic tracking-tight leading-tight uppercase h-[3.5em] md:h-[2.5em]">
                {slides[currentSlide].subtitle}
              </p>
            </motion.div>
          </AnimatePresence>
        </div>
      </div>

      {/* Navigation Dots */}
      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-20 flex gap-3">
        {slides.map((_, index) => (
          <button
            key={index}
            onClick={() => setCurrentSlide(index)}
            className={`w-3 h-3 rounded-full transition-all duration-300 ${
              currentSlide === index ? 'bg-brand-red w-8' : 'bg-white/30 hover:bg-white/50'
            }`}
          />
        ))}
      </div>
    </section>
  );
};

const NutritionModal = ({ item, isOpen, onClose }: { item: MenuItem | null, isOpen: boolean, onClose: () => void }) => {
  const { lang, t } = useTranslation();
  if (!item) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[120] flex items-center justify-center p-4">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-brand-charcoal/60 backdrop-blur-sm"
          />
          <motion.div
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.9, y: 20 }}
            className="relative bg-white w-full max-w-md rounded-[2.5rem] overflow-hidden shadow-2xl max-h-[90vh] flex flex-col"
          >
            <div className="p-8 overflow-y-auto">
              <div className="flex justify-between items-start mb-6">
                <div>
                  <h3 className="text-3xl font-black italic tracking-tighter leading-none mb-2 uppercase">
                    {t('NUTRITION', 'BESİN DEĞERLERİ')}
                  </h3>
                  <div className="flex flex-col mb-2">
                    {item.level && <span className="text-brand-red text-xl font-black italic">{item.level}</span>}
                    {(lang === 'en' ? item.tagline : item.taglineTr) && (
                      <span className="inline-block bg-brand-red text-white text-[10px] font-black italic uppercase tracking-tighter px-2 py-0.5 mb-1">
                        {lang === 'en' ? item.tagline : item.taglineTr}
                      </span>
                    )}
                  </div>
                  <p className="text-brand-charcoal font-black text-sm uppercase tracking-widest">{item.name}</p>
                </div>
                <button 
                  onClick={onClose}
                  className="p-2 bg-gray-100 rounded-full hover:bg-brand-red hover:text-white transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {item.nutrition && (
                <div className="grid grid-cols-2 gap-4 mb-4">
                  <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                    <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('PROTEIN', 'PROTEİN')}</p>
                    <p className="text-xl font-black italic">{item.nutrition.protein}g</p>
                  </div>
                  <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                    <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('FAT', 'YAĞ')}</p>
                    <p className="text-xl font-black italic">{item.nutrition.fat}g</p>
                  </div>
                  <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                    <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('CARBS', 'KARBONHİDRAT')}</p>
                    <p className="text-xl font-black italic">{item.nutrition.carbs}g</p>
                  </div>
                  {item.nutrition.sodium !== undefined && (
                    <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                      <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('SODIUM', 'SODYUM')}</p>
                      <p className="text-xl font-black italic">{item.nutrition.sodium}mg</p>
                    </div>
                  )}
                  <div className={cn("bg-gray-50 p-4 rounded-2xl border border-gray-100", item.nutrition.sodium !== undefined ? "col-span-2" : "")}>
                    <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('CALORIES', 'KALORİ')}</p>
                    <p className="text-xl font-black italic text-brand-red">{item.calories} kcal</p>
                  </div>
                </div>
              )}

              <AllergenGridDisplay allergens={item.allergens} lang={lang} t={t} />

              {item.ingredients && (
                <div>
                  <h4 className="text-xs font-black uppercase tracking-widest mb-4 flex items-center gap-2">
                    <div className="w-1 h-4 bg-brand-red rounded-full" />
                    {t('INGREDIENTS', 'İÇİNDEKİLER')}
                  </h4>
                  <div className="flex flex-wrap gap-2">
                    {(t('en', 'tr') === 'en' ? item.ingredients : (item.ingredientsTr || item.ingredients)).map((ing, i) => (
                      <span key={i} className="px-3 py-1.5 bg-gray-100 rounded-lg text-xs font-bold text-gray-600">
                        {ing}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="p-6 md:p-8 border-t border-gray-100 bg-white">
              <button 
                onClick={onClose}
                className="w-full bg-brand-charcoal text-white py-4 rounded-2xl font-black text-sm uppercase tracking-widest hover:bg-brand-red transition-colors"
              >
                {t('CLOSE', 'KAPAT')}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

const UpgradeModal = ({ item, isOpen, onClose, onShowNutrition }: { item: MenuItem | null, isOpen: boolean, onClose: () => void, onShowNutrition?: (item: MenuItem) => void }) => {
  const { lang, t, formatPrice } = useTranslation();
  const { addToCart } = useCart();
  const [selectedExtras, setSelectedExtras] = useState<ExtraIngredient[]>([]);
  const [selectedDrink, setSelectedDrink] = useState<string>('cola');

  if (!item) return null;

  const toggleExtra = (extra: ExtraIngredient) => {
    setSelectedExtras(prev => 
      prev.find(e => e.id === extra.id) 
        ? prev.filter(e => e.id !== extra.id)
        : [...prev, extra]
    );
  };

  const handleAddToCart = () => {
    const extrasWithDrink = [...selectedExtras];
    if (item.category === 'menus') {
      const drink = DRINK_OPTIONS.find(d => d.id === selectedDrink);
      if (drink) {
        extrasWithDrink.push({
          id: `drink-${drink.id}`,
          name: `Drink: ${drink.name}`,
          nameTr: `İçecek: ${drink.nameTr}`,
          price: 0
        });
      }
    }
    addToCart(item, extrasWithDrink);
    setSelectedExtras([]);
    setSelectedDrink('cola');
    onClose();
  };

  const extrasTotal = selectedExtras.reduce((sum, e) => sum + e.price, 0);

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[120] flex items-center justify-center p-4">
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-brand-charcoal/60 backdrop-blur-sm"
          />
          <motion.div 
            initial={{ scale: 0.9, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.9, opacity: 0, y: 20 }}
            className="bg-white w-full max-w-md rounded-[2.5rem] overflow-hidden shadow-2xl relative z-10 max-h-[90vh] flex flex-col"
          >
            <div className="p-6 md:p-8 overflow-y-auto">
              <div className="flex justify-between items-start mb-6 sticky top-0 bg-white z-10 pb-2">
                <div>
                  <h3 className="text-xl font-black italic uppercase leading-tight">
                    {t('UPGRADE YOUR', 'YÜKSELT')} <br />
                    <span className="text-brand-red">{item.name}</span>
                  </h3>
                  <div className="flex flex-col mt-3 mb-1">
                    {item.level && <span className="text-brand-red text-xl font-black italic">{item.level}</span>}
                    {(lang === 'en' ? item.tagline : item.taglineTr) && (
                      <span className="inline-block bg-brand-red text-white text-[10px] font-black italic uppercase tracking-tighter px-2 py-0.5 mb-1">
                        {lang === 'en' ? item.tagline : item.taglineTr}
                      </span>
                    )}
                  </div>
                </div>
                <button 
                  onClick={onClose}
                  className="p-2 bg-gray-100 rounded-full hover:bg-brand-red hover:text-white transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <AllergenGridDisplay allergens={item.allergens} lang={lang} t={t} />

              <div className="space-y-6 mb-8">
                {item.category === 'menus' && (
                  <div>
                    <h4 className="text-xs font-black uppercase tracking-widest mb-4 flex items-center gap-2">
                      <div className="w-1 h-4 bg-brand-red rounded-full" />
                      {t('CHOOSE YOUR DRINK', 'İÇECEĞİNİ SEÇ')}
                    </h4>
                    <div className="grid grid-cols-2 gap-2">
                      {DRINK_OPTIONS.map((drink) => (
                        <button
                          key={drink.id}
                          onClick={() => setSelectedDrink(drink.id)}
                          className={cn(
                            "flex items-center justify-between p-3 rounded-xl border-2 transition-all text-left",
                            selectedDrink === drink.id 
                              ? "border-brand-red bg-brand-red/5" 
                              : "border-gray-100 hover:border-gray-200"
                          )}
                        >
                          <span className="font-bold text-xs">{lang === 'en' ? drink.name : drink.nameTr}</span>
                          {selectedDrink === drink.id && <div className="w-2 h-2 rounded-full bg-brand-red" />}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <div>
                  <h4 className="text-xs font-black uppercase tracking-widest mb-4 flex items-center gap-2">
                    <div className="w-1 h-4 bg-brand-red rounded-full" />
                    {t('ADD EXTRAS', 'EKSTRALAR EKLE')}
                  </h4>
                  <div className="space-y-3">
                    {EXTRA_INGREDIENTS.filter(e => {
                      if (item.category === 'doner' || item.id.includes('doner')) return e.id !== 'extra-patty' && e.id !== 'bacon';
                      return true;
                    }).map((extra) => {
                      const isSelected = selectedExtras.find(e => e.id === extra.id);
                      return (
                        <button
                          key={extra.id}
                          onClick={() => toggleExtra(extra)}
                          className={cn(
                            "w-full flex items-center justify-between p-4 rounded-2xl border-2 transition-all",
                            isSelected 
                              ? "border-brand-red bg-brand-red/5" 
                              : "border-gray-100 hover:border-gray-200"
                          )}
                        >
                          <div className="flex items-center gap-3">
                            <div className={cn(
                              "w-5 h-5 rounded-md border-2 flex items-center justify-center transition-colors",
                              isSelected ? "bg-brand-red border-brand-red" : "border-gray-300"
                            )}>
                              {isSelected && <Plus className="w-3 h-3 text-white" />}
                            </div>
                            <span className="font-bold text-sm">{lang === 'en' ? extra.name : extra.nameTr}</span>
                          </div>
                          <span className="font-black text-brand-red text-sm">+{formatPrice(extra.price)}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>

              <div className="p-6 bg-brand-white rounded-3xl border border-gray-100">
                <div className="flex justify-between mb-2 text-sm font-bold text-gray-500">
                  <span>{t('Base Price', 'Başlangıç Fiyatı')}</span>
                  <span>{formatPrice(item.price)}</span>
                </div>
                <div className="flex justify-between mb-4 text-sm font-bold text-brand-red">
                  <span>{t('Extras', 'Ekstralar')}</span>
                  <span>+{formatPrice(extrasTotal)}</span>
                </div>
                <div className="flex justify-between font-black text-xl italic">
                  <span>{t('TOTAL', 'TOPLAM')}</span>
                  <span>{formatPrice(item.price + extrasTotal)}</span>
                </div>
              </div>
            </div>

            <div className="p-6 md:p-8 border-t border-gray-100 bg-white">
              <button 
                onClick={handleAddToCart}
                className="w-full bg-brand-red text-white py-5 rounded-2xl font-black text-lg uppercase tracking-widest hover:bg-brand-charcoal transition-all shadow-xl shadow-brand-red/20 active:scale-95"
              >
                {t('ADD TO BAG', 'SEPETE EKLE')}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

const WorkingHoursModal = ({ isOpen, onClose, storeName }: { isOpen: boolean, onClose: () => void, storeName: string }) => {
  const { t } = useTranslation();
  
  const hours = [
    { day: t('Monday', 'Pazartesi'), hours: '11:00 - 23:00' },
    { day: t('Tuesday', 'Salı'), hours: '11:00 - 23:00' },
    { day: t('Wednesday', 'Çarşamba'), hours: '11:00 - 23:00' },
    { day: t('Thursday', 'Perşembe'), hours: '11:00 - 23:00' },
    { day: t('Friday', 'Cuma'), hours: '11:00 - 00:00' },
    { day: t('Saturday', 'Cumartesi'), hours: '11:00 - 00:00' },
    { day: t('Sunday', 'Pazar'), hours: '11:00 - 23:00' },
  ];

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[110] flex items-center justify-center p-4">
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-brand-charcoal/60 backdrop-blur-sm"
          />
          <motion.div 
            initial={{ scale: 0.9, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.9, opacity: 0, y: 20 }}
            className="bg-white w-full max-w-sm rounded-[2.5rem] overflow-hidden shadow-2xl relative z-10"
          >
            <div className="p-8">
              <div className="flex justify-between items-start mb-6">
                <div>
                  <h3 className="text-2xl font-black italic uppercase leading-tight">
                    {t('OPENING', 'AÇILIŞ')} <br />
                    <span className="text-brand-red">{t('HOURS', 'SAATLERİ')}</span>
                  </h3>
                  <p className="text-xs font-bold text-gray-400 mt-1 uppercase tracking-widest">{storeName}</p>
                </div>
                <button 
                  onClick={onClose}
                  className="p-2 bg-gray-100 rounded-full hover:bg-brand-red hover:text-white transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-3">
                {hours.map((item) => (
                  <div key={item.day} className="flex justify-between items-center p-3 rounded-xl bg-gray-50 border border-gray-100">
                    <span className="font-bold text-sm text-gray-600">{item.day}</span>
                    <span className="font-black text-sm italic text-brand-charcoal">{item.hours}</span>
                  </div>
                ))}
              </div>

              <button 
                onClick={onClose}
                className="w-full mt-8 bg-brand-charcoal text-white py-4 rounded-2xl font-black text-sm uppercase tracking-widest hover:bg-brand-red transition-colors"
              >
                {t('CLOSE', 'KAPAT')}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

const ExpandableDescription = ({ text, id, t }: { text: string; id: string; t: any }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [isTruncated, setIsTruncated] = useState(false);
  const textRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    const checkTruncation = () => {
      if (textRef.current) {
        const isOverflowing = textRef.current.scrollHeight > textRef.current.clientHeight;
        setIsTruncated(isOverflowing);
      }
    };

    checkTruncation();
    window.addEventListener('resize', checkTruncation);
    return () => window.removeEventListener('resize', checkTruncation);
  }, [text]);

  return (
    <div className="mb-2 md:mb-3">
      <p 
        ref={textRef}
        className={cn(
          "text-gray-500 text-[10px] md:text-sm font-medium leading-relaxed transition-all duration-300",
          !isExpanded && "line-clamp-2"
        )}
      >
        {text}
      </p>
      {(isTruncated || isExpanded) && (
        <button 
          onClick={(e) => { e.stopPropagation(); setIsExpanded(!isExpanded); }}
          className="mt-1 text-brand-red text-[10px] md:text-xs font-black uppercase tracking-widest flex items-center gap-1 hover:underline"
        >
          {isExpanded ? (
            <>
              <Minus className="w-3 h-3" />
              {t('SHOW LESS', 'DAHA AZ GÖSTER')}
            </>
          ) : (
            <>
              <Plus className="w-3 h-3" />
              {t('SHOW ALL', 'TÜMÜNÜ GÖR')}
            </>
          )}
        </button>
      )}
    </div>
  );
};

const formatItemName = (name: string) => {
  const trimmedName = name.trim();
  // Keep emojis with the last word using non-breaking space
  const lastSpaceIndex = trimmedName.lastIndexOf(' ');
  if (lastSpaceIndex === -1) return trimmedName;
  return trimmedName.substring(0, lastSpaceIndex) + '\u00A0' + trimmedName.substring(lastSpaceIndex + 1);
};

const AllergenGridDisplay = ({ allergens, lang, t }: { allergens?: string[]; lang: string; t: any }) => {
  if (!allergens || allergens.length === 0) return null;
  return (
    <div className="w-full mt-4 mb-2 select-text">
      <h4 className="text-[14px] font-black uppercase tracking-widest mb-3 flex items-center gap-2 text-[#1a1a1a]">
        <div className="w-1.5 h-5 bg-brand-red rounded-full" />
        {t('ALLERGENS', 'ALERJENLER')}
      </h4>
      <div className="grid grid-cols-2 gap-3">
        {allergens.map((allergen, i) => {
          const info = ALLERGEN_ICONS[allergen];
          if (!info) return null;
          return (
            <div 
              key={`allergen-grid-${allergen}-${i}`}
              className="flex items-center gap-3 px-4 py-3 rounded-full bg-[#f9fafb] border border-gray-100"
            >
              <div className="flex-shrink-0 text-brand-red">
                {info.icon}
              </div>
              <span className="text-[13px] font-bold text-[#4a505c]">
                {/* Ensure standard casing (e.g. Süt Ürünü) */}
                {lang === 'en' ? info.nameEn : info.nameTr}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

const MenuSection = ({ onShowFullMenu }: { onShowFullMenu?: () => void }) => {
  const [activeCategory, setActiveCategory] = useState<'burgers' | 'chicken' | 'doner' | 'pizza' | 'sides' | 'menus' | 'drinks' | 'kids' | 'desserts' | 'breakfast' | 'sauces'>('burgers');
  const [activePerformanceFilter, setActivePerformanceFilter] = useState<'all' | 'high-protein' | 'low-carb' | 'low-cal' | 'mass-gainer'>('all');
  const [selectedNutritionItem, setSelectedNutritionItem] = useState<MenuItem | null>(null);
  const [selectedUpgradeItem, setSelectedUpgradeItem] = useState<MenuItem | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<MenuItem | null>(null);
  const [expandedItems, setExpandedItems] = useState<Record<string, boolean>>({});
  const [starsOfTheWeek, setStarsOfTheWeek] = useState<Record<string, string>>({});
  const { lang, t, formatPrice } = useTranslation();
  const { addToCart } = useCart();
  const { searchQuery } = useSearch();
  const sliderRef = useRef<HTMLDivElement>(null);
  const categoryContainerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchStars = async () => {
      try {
        const sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
        
        const q = query(
          collection(db, 'orders'),
          where('createdAt', '>=', Timestamp.fromDate(sevenDaysAgo))
        );
        
        const querySnapshot = await getDocs(q);
        const salesCount: Record<string, Record<string, number>> = {};
        
        querySnapshot.forEach((doc) => {
          const order = doc.data();
          if (order.items && Array.isArray(order.items)) {
            order.items.forEach((item: any) => {
              const baseId = item.id.replace('-menu', '');
              const menuItem = MENU_ITEMS.find(mi => mi.id === baseId);
              if (menuItem) {
                const category = menuItem.category;
                if (!salesCount[category]) salesCount[category] = {};
                salesCount[category][menuItem.id] = (salesCount[category][menuItem.id] || 0) + (item.quantity || 1);
              }
            });
          }
        });
        
        const newStars: Record<string, string> = {};
        Object.entries(salesCount).forEach(([category, products]) => {
          let maxSales = 0;
          let starId = '';
          Object.entries(products).forEach(([id, count]) => {
            if (count > maxSales) {
              maxSales = count;
              starId = id;
            }
          });
          if (starId) newStars[category] = starId;
        });
        
        setStarsOfTheWeek(newStars);
      } catch (error) {
        console.error("Error fetching stars of the week:", error);
      }
    };
    
    fetchStars();
  }, []);

  const defaultStars = React.useMemo(() => {
    const stars: Record<string, string> = {};
    const categories = ['burgers', 'doner', 'pizza', 'menus', 'chicken', 'breakfast', 'sides', 'kids', 'drinks', 'sauces', 'desserts'];
    categories.forEach(cat => {
      const firstItem = MENU_ITEMS.find(item => item.category === cat);
      if (firstItem) stars[cat] = firstItem.id;
    });
    return stars;
  }, []);

  const currentStars = { ...defaultStars, ...starsOfTheWeek };

  useEffect(() => {
    if (sliderRef.current) {
      sliderRef.current.scrollTo({ left: 0, behavior: 'smooth' });
    }
    if (categoryContainerRef.current) {
      const activeBtn = categoryContainerRef.current.querySelector(`[data-category="${activeCategory}"]`);
      if (activeBtn) {
        activeBtn.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'start' });
      }
    }
  }, [activeCategory, searchQuery]);

  const scrollRight = () => {
    if (sliderRef.current) {
      const scrollAmount = sliderRef.current.clientWidth * 0.8;
      sliderRef.current.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  };
  
  const filteredItems = MENU_ITEMS.flatMap(item => {
    if (item.category === 'doner') {
      const isBox = item.id.includes('box');
      return [
        item,
        { 
          ...item, 
          id: `${item.id}-menu`, 
          name: `${item.name} MENU`, 
          price: isBox ? item.price + 60 : item.price + 110, 
          category: 'menus', 
          description: isBox ? item.description + ' + Drink' : item.description + ' + Fries + Drink', 
          descriptionTr: isBox 
            ? (item.descriptionTr || item.description) + ' + İçecek'
            : (item.descriptionTr || item.description) + ' + Patates + İçecek' 
        } 
      ];
    }
    return [item];
  }).filter(item => {
    const query = searchQuery.toLowerCase();
    const matchesSearch = searchQuery === '' || 
      item.name.toLowerCase().includes(query) ||
      item.description.toLowerCase().includes(query) ||
      (item.descriptionTr && item.descriptionTr.toLowerCase().includes(query)) ||
      (item.tagline && item.tagline.toLowerCase().includes(query)) ||
      (item.taglineTr && item.taglineTr.toLowerCase().includes(query));
    
    if (searchQuery !== '') return matchesSearch;
    
    // Apply category filter
    if (item.category !== activeCategory) return false;

    // Apply performance filter
    if (activePerformanceFilter === 'high-protein') return item.nutrition && item.nutrition.protein >= 30;
    if (activePerformanceFilter === 'low-carb') return item.nutrition && item.nutrition.carbs <= 40;
    if (activePerformanceFilter === 'low-cal') return item.calories && item.calories <= 500;
    if (activePerformanceFilter === 'mass-gainer') return item.calories && item.calories >= 800;

    return true;
  });

  const performanceFilters = [
    { id: 'all', label: t('ALL', 'TÜMÜ'), icon: <MenuIcon className="w-4 h-4" /> },
    { id: 'high-protein', label: t('PROTEIN', 'PROTEİN'), icon: <Activity className="w-4 h-4" />, sub: '> 30g' },
    { id: 'low-carb', label: t('LOW CARB', 'DÜŞÜK KARB'), icon: <Wheat className="w-4 h-4" />, sub: '< 40g' },
    { id: 'low-cal', label: t('LOW CAL', 'DÜŞÜK KAL'), icon: <Zap className="w-4 h-4" />, sub: '< 500' },
    { id: 'mass-gainer', label: t('MASS', 'KÜTLE'), icon: <Flame className="w-4 h-4" />, sub: '> 800' },
  ];

  return (
    <section id="menu" className="pt-8 pb-12 bg-white scroll-mt-16">
      <div className="container mx-auto px-4 md:px-8">
        <div className="flex items-center gap-2 md:gap-3 mb-8 overflow-hidden md:-ml-4">
          <div className="shrink-0">
            {!searchQuery ? (
              <h2 className="text-4xl md:text-6xl font-black tracking-tighter italic uppercase">
                <span className="text-brand-red">MENU</span>
              </h2>
            ) : (
              <div className="flex flex-col">
                <h2 className="text-4xl md:text-6xl font-black tracking-tighter italic uppercase">
                  SEARCH <span className="text-brand-red">RESULTS</span>
                </h2>
                <p className="text-gray-500 font-medium max-w-md mt-2">
                  {t(`Showing results for "${searchQuery}"`, `"${searchQuery}" için sonuçlar gösteriliyor`)}
                </p>
              </div>
            )}
          </div>
          
          {!searchQuery && (
            <div 
              ref={categoryContainerRef}
              className="flex-1 flex gap-1 bg-brand-white p-1.5 rounded-2xl overflow-x-auto no-scrollbar border border-gray-100 scroll-smooth scroll-pl-1.5"
            >
              {(['burgers', 'doner', 'pizza', 'menus', 'chicken', 'breakfast', 'sides', 'kids', 'drinks', 'sauces', 'desserts'] as const).map((cat) => (
                <button
                  key={cat}
                  data-category={cat}
                  onClick={() => setActiveCategory(cat)}
                  className={cn(
                    "px-8 py-3 rounded-xl font-bold text-sm uppercase tracking-wider transition-all whitespace-nowrap",
                    activeCategory === cat 
                      ? "bg-brand-red text-white shadow-lg shadow-brand-red/20" 
                      : "text-gray-500 hover:text-brand-charcoal"
                  )}
                >
                  {t(cat === 'kids' ? 'Kids Menu' : cat === 'desserts' ? 'Desserts' : cat === 'breakfast' ? 'Breakfast' : cat === 'sauces' ? 'Sauces' : cat.charAt(0).toUpperCase() + cat.slice(1), 
                    cat === 'burgers' ? 'Burger' : 
                    cat === 'menus' ? 'Menüler' : 
                    cat === 'chicken' ? 'Tavuk' : 
                    cat === 'doner' ? 'Döner' : 
                    cat === 'pizza' ? 'Pizza' : 
                    cat === 'sides' ? 'Yan Ürünler' : 
                    cat === 'drinks' ? 'İçecekler' : 
                    cat === 'kids' ? 'Mini Menü' :
                    cat === 'breakfast' ? 'Kahvaltı' :
                    cat === 'sauces' ? 'Soslar' :
                    'Tatlılar'
                  )}
                </button>
              ))}
            </div>
          )}
        </div>

        {filteredItems.length === 0 ? (
          <div className="py-20 text-center">
            <div className="bg-gray-50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
              <Search className="w-8 h-8 text-gray-300" />
            </div>
            <h3 className="text-2xl font-black italic mb-2 uppercase">{t('NO RESULTS FOUND', 'SONUÇ BULUNAMADI')}</h3>
            <p className="text-gray-500 font-medium">{t('Try searching for something else', 'Başka bir şey aramayı deneyin')}</p>
          </div>
        ) : (
          <div className="relative group/slider">
            <AnimatePresence mode="wait">
              <motion.div 
                key={activeCategory}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.2 }}
                ref={sliderRef}
                className="grid grid-cols-1 md:flex md:overflow-x-auto overflow-y-visible no-scrollbar gap-2 md:gap-2 pb-6 md:pb-16 -mx-4 px-4 md:mx-0 md:px-0 md:snap-x md:snap-mandatory min-h-0 md:min-h-[500px]"
              >
                {filteredItems.map((item) => (
                  <div
                    key={`menu-section-${item.id}`}
                    onClick={() => setSelectedProduct(item)}
                    className="w-full md:shrink-0 md:w-[45%] lg:w-[28%] md:snap-start group bg-brand-white rounded-3xl hover:shadow-2xl transition-all border border-gray-100 hover:border-brand-red/20 flex flex-col cursor-pointer active:scale-[0.98]"
                  >
                  <div className="relative h-40 md:h-64 overflow-hidden rounded-t-3xl bg-gray-100">
                    <img 
                      key={item.image}
                      src={item.image} 
                      alt={item.name} 
                      className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                      referrerPolicy="no-referrer"
                    />
                    {currentStars[item.category] === item.id && (
                      <div className="absolute top-4 left-4 z-20 bg-brand-red text-white px-3 py-1.5 rounded-xl flex items-center gap-2 shadow-lg border border-white/20">
                        <Star className="w-3 h-3 fill-white" />
                        <span className="text-[10px] font-black italic uppercase tracking-tighter whitespace-nowrap">
                          {t('STAR OF THE WEEK', 'HAFTANIN YILDIZI')}
                        </span>
                      </div>
                    )}
                    {item.isBestseller && (
                      <div className="absolute top-4 right-4 bg-brand-red text-white px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest animate-pulse">
                        {t('MOST ORDERED', 'EN ÇOK SATAN')}
                      </div>
                    )}
                    {item.isLimited && (
                      <div className="absolute top-4 right-4 bg-black text-white px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest">
                        {t('LIMITED', 'SINIRLI')}
                      </div>
                    )}
                  </div>
                  
                  <div className="p-4 md:p-8 pb-2 md:pb-3">
                    <div className="flex justify-between items-start mb-1">
                      <h3 className="font-black tracking-tight italic leading-tight">
                        <div className="mb-2">
                          {item.level && <span className="block text-brand-red text-xl md:text-4xl leading-none mb-1">{item.level}</span>}
                          {(lang === 'en' ? item.tagline : item.taglineTr) && (
                            <span className="inline-block bg-brand-red text-white text-[10px] md:text-sm font-black italic uppercase tracking-tighter px-2 py-0.5 mb-1">
                              {lang === 'en' ? item.tagline : item.taglineTr}
                            </span>
                          )}
                        </div>
                        <span className="block text-sm md:text-xl leading-tight line-clamp-3 mb-1">{formatItemName(item.name)}</span>
                      </h3>
                      <span className="text-sm md:text-lg font-black text-brand-red whitespace-nowrap ml-2">{formatPrice(item.price)}</span>
                    </div>
                    <ExpandableDescription 
                      text={lang === 'en' ? item.description : (item.descriptionTr || item.description)} 
                      id={item.id} 
                      t={t} 
                    />

                    {item.allergens && item.allergens.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mb-2">
                        {item.allergens.map((allergen, i) => {
                          const info = ALLERGEN_ICONS[allergen];
                          if (!info) return null;
                          return (
                            <button 
                              key={`fullmenu-allergen-${item.id}-${allergen}-${i}`}
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedNutritionItem(item);
                              }}
                              title={lang === 'en' ? info.nameEn : info.nameTr}
                              className="w-6 h-6 rounded-lg bg-gray-50 border border-gray-100 flex items-center justify-center text-xs hover:bg-brand-red/5 hover:border-brand-red/20 transition-all active:scale-90"
                            >
                              {info.icon}
                            </button>
                          );
                        })}
                      </div>
                    )}
                    
                    <div className="flex items-center justify-between">
                      <div className="flex flex-col">
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedNutritionItem(item);
                          }}
                          className="flex items-center gap-2 text-[10px] font-bold text-gray-400 uppercase tracking-wider hover:text-brand-red transition-colors group/info"
                        >
                          <Info className="w-3.5 h-3.5 group-hover/info:scale-110 transition-transform" />
                          {item.calories} {t('kcal', 'kalori')}
                        </button>
                        {(item.category === 'burgers' || item.category === 'menus' || item.category === 'doner') && item.level !== '128 BIT' && (
                          <button 
                            onClick={() => setSelectedUpgradeItem(item)}
                            className="text-[10px] font-black text-brand-red uppercase mt-1 hover:underline text-left"
                          >
                            {t('UPGRADE AVAILABLE', 'YÜKSELTME MEVCUT')}
                          </button>
                        )}
                      </div>
                      <button 
                        onClick={(e) => {
                          e.stopPropagation();
                          if ((item.category === 'burgers' || item.category === 'menus' || item.category === 'doner') && item.level !== '128 BIT') {
                            setSelectedUpgradeItem(item);
                          } else {
                            addToCart(item);
                          }
                        }}
                        className="bg-brand-white border border-gray-100 p-2 md:p-3 rounded-xl shadow-sm hover:bg-brand-red hover:text-white transition-all active:scale-90 group"
                      >
                        <Plus className="w-4 h-4 md:w-5 h-5 group-hover:rotate-90 transition-transform" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </motion.div>
          </AnimatePresence>
            
            {filteredItems.length > 1 && (
              <button 
                onClick={scrollRight}
                className="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-12 h-12 md:w-16 h-16 bg-brand-red text-white rounded-full shadow-xl hidden md:flex items-center justify-center hover:scale-110 active:scale-95 transition-all -mr-2 md:-mr-8 border-4 border-white"
              >
                <ChevronRight className="w-6 h-6 md:w-8 h-8" />
              </button>
            )}
          </div>
        )}

        <div className="mt-8 md:mt-10 flex justify-center">
          <button 
            onClick={onShowFullMenu}
            className="group flex items-center gap-3 px-10 py-5 bg-brand-red text-white rounded-full shadow-xl shadow-brand-red/20 hover:scale-105 active:scale-95 transition-all duration-300"
          >
            <span className="text-lg md:text-xl font-black italic uppercase tracking-tighter">
              {t('SEE ALL MENU', 'TÜM MENÜYÜ GÖR')}
            </span>
            <ArrowRight className="w-5 h-5 md:w-6 h-6 group-hover:translate-x-1 transition-transform" />
          </button>
        </div>
      </div>

      <NutritionModal 
        item={selectedNutritionItem} 
        isOpen={!!selectedNutritionItem} 
        onClose={() => setSelectedNutritionItem(null)} 
      />

      <UpgradeModal 
        item={selectedUpgradeItem} 
        isOpen={!!selectedUpgradeItem} 
        onClose={() => setSelectedUpgradeItem(null)} 
        onShowNutrition={(item) => setSelectedNutritionItem(item)}
      />

      <ProductInfoModal 
        item={selectedProduct} 
        isOpen={!!selectedProduct} 
        onClose={() => setSelectedProduct(null)}
        onUpgrade={(item) => setSelectedUpgradeItem(item)}
      />
    </section>
  );
};

const Features = () => {
  const { t } = useTranslation();
  const features = [
    {
      title: t("Premium Beef", "Premium Dana Eti"),
      desc: t("100% grass-fed beef, never frozen, smashed fresh to order.", "%100 otla beslenen dana eti, asla dondurulmamış, sipariş üzerine taze basılmış."),
      icon: <Star className="w-8 h-8" />
    },
    {
      title: t("Fast Delivery", "Hızlı Teslimat"),
      desc: t("Hot and fresh to your door in under 30 minutes.", "30 dakikadan kısa sürede kapınızda sıcak ve taze."),
      icon: <Clock className="w-8 h-8" />
    },
    {
      title: t("Secret Sauce", "Gizli Sos"),
      desc: t("Our proprietary OMASH sauce recipe, perfected over 2 years.", "2 yılı aşkın sürede mükemmelleştirilen tescilli OMASH sos tarifimiz."),
      icon: <Info className="w-8 h-8" />
    }
  ];

  return (
    <section className="pt-20 pb-16 bg-brand-charcoal text-white overflow-hidden relative">
      <div className="absolute top-0 right-0 w-1/2 h-full bg-brand-red/5 -skew-x-12 translate-x-1/4" />
      
      <div className="container mx-auto px-4 md:px-8 relative z-10">
        <div className="grid md:grid-cols-3 gap-12">
          {features.map((f, i) => (
            <motion.div 
              key={`feature-${i}`}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.2 }}
              className="flex flex-col gap-6"
            >
              <div className="bg-brand-red w-16 h-16 rounded-2xl flex items-center justify-center shadow-lg shadow-brand-red/20">
                {f.icon}
              </div>
              <h3 className="text-2xl font-black italic">{f.title}</h3>
              <p className="text-white/60 font-medium leading-relaxed">
                {f.desc}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

const ChatModal = ({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) => {
  const { t } = useTranslation();
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState<{ text: string; isUser: boolean }[]>([
    { text: t('Hello! How can we help you today?', 'Merhaba! Size bugün nasıl yardımcı olabiliriz?'), isUser: false }
  ]);

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!message.trim()) return;

    setMessages(prev => [...prev, { text: message, isUser: true }]);
    setMessage('');

    // Mock response
    setTimeout(() => {
      setMessages(prev => [...prev, { 
        text: t('Thanks for your message! Our support team will get back to you shortly.', 'Mesajınız için teşekkürler! Destek ekibimiz kısa süre içinde size dönecektir.'), 
        isUser: false 
      }]);
    }, 1000);
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 z-[110]"
          />
          <motion.div 
            initial={{ opacity: 0, y: 100, scale: 0.9 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 100, scale: 0.9 }}
            className="fixed bottom-28 right-8 w-full max-w-[350px] bg-white rounded-[2rem] z-[120] shadow-2xl overflow-hidden border border-gray-100 flex flex-col h-[500px]"
          >
            <div className="bg-[#0084FF] p-6 text-white flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
                  <MessageSquare className="w-6 h-6 fill-white" />
                </div>
                <div>
                  <h3 className="font-black italic text-sm">OMASH SUPPORT</h3>
                  <p className="text-[10px] font-bold opacity-80 uppercase tracking-widest">{t('Online', 'Çevrimiçi')}</p>
                </div>
              </div>
              <button onClick={onClose} className="p-1 hover:bg-white/10 rounded-full transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-gray-50">
              {messages.map((msg, i) => (
                <div key={i} className={cn("flex", msg.isUser ? "justify-end" : "justify-start")}>
                  <div className={cn(
                    "max-w-[80%] p-4 rounded-2xl text-sm font-medium shadow-sm",
                    msg.isUser ? "bg-brand-red text-white rounded-tr-none" : "bg-white text-brand-charcoal rounded-tl-none"
                  )}>
                    {msg.text}
                  </div>
                </div>
              ))}
            </div>

            <form onSubmit={handleSend} className="p-4 bg-white border-t flex gap-2">
              <input 
                type="text" 
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder={t('Type a message...', 'Bir mesaj yazın...')}
                className="flex-1 bg-gray-100 border-none rounded-xl px-4 py-3 text-sm font-medium focus:ring-2 focus:ring-[#0084FF] transition-all"
              />
              <button type="submit" className="bg-[#0084FF] text-white p-3 rounded-xl hover:opacity-90 transition-opacity">
                <ArrowRight className="w-5 h-5" />
              </button>
            </form>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

const Footer = ({ setCurrentPage, onOpenLegal, onOpenLogin }: { 
  setCurrentPage: (page: any) => void;
  onOpenLegal: (type: string) => void;
  onOpenLogin: () => void;
}) => {
  const { t } = useTranslation();
  
  const footerLinks = {
    about: [
      { label: t('About Us', 'Hakkımızda'), onClick: () => {
        setCurrentPage('about');
        window.scrollTo({ top: 0, behavior: 'instant' as any });
      } },
      { label: t('Taste Guarantee', 'Omash Lezzet Garantisi'), onClick: () => {
        setCurrentPage('guarantee');
        window.scrollTo({ top: 0, behavior: 'instant' as any });
      } },
      { label: t('Full Menu', 'Tüm Menü'), onClick: () => { setCurrentPage('full-menu'); window.scrollTo({ top: 0, behavior: 'instant' as any }); } },
      { label: t('Deals', 'Fırsatlar'), onClick: () => { setCurrentPage('deals'); window.scrollTo({ top: 0, behavior: 'instant' as any }); } },
      { label: t('Franchising / Investment', 'Franchising / Yatırım'), onClick: () => { setCurrentPage('franchising'); window.scrollTo({ top: 0, behavior: 'instant' as any }); } },
    ],
    help: [
      { label: t('Our Locations', 'Şubelerimiz'), onClick: () => { setCurrentPage('locations'); window.scrollTo({ top: 0, behavior: 'instant' as any }); } },
      { label: t('Contact Us', 'Bize Ulaşın'), onClick: () => { setCurrentPage('locations'); window.scrollTo({ top: 0, behavior: 'instant' as any }); } },
      { label: t('Membership', 'Üyelik'), onClick: onOpenLogin },
      { label: t('Order Tracking', 'Sipariş Takip'), onClick: () => setCurrentPage('profile') },
      { label: t('Frequently Asked Questions', 'Sıkça Sorulan Sorular'), onClick: () => { setCurrentPage('about'); setTimeout(() => document.getElementById('faq')?.scrollIntoView({ behavior: 'smooth' }), 100); } },
      { label: t('Allergen List', 'Alerjen Listesi'), onClick: () => { 
        setCurrentPage('allergens'); 
        setTimeout(() => {
          window.scrollTo({ top: 0, behavior: 'instant' as any });
        }, 0);
      } },
    ],
    corporate: [
      { label: t('Personal Data Protection', 'Kişisel Verilerin Korunması'), onClick: () => onOpenLegal('kvkk') },
      { label: t('Privacy Policy', 'Gizlilik Politikası'), onClick: () => onOpenLegal('privacy') },
      { label: t('Terms of Service', 'Kullanım Koşulları'), onClick: () => onOpenLegal('terms') },
      { label: t('Information Society Services', 'Bilgi Toplum Hizmetleri'), onClick: () => onOpenLegal('society') },
      { label: t('Responsible Disclosure', 'Sorumlu Bildirim'), onClick: () => onOpenLegal('disclosure') },
      { label: t('Halal Certificates', 'Helal Belgeleri'), onClick: () => onOpenLegal('halal') },
      { label: t('Hygiene Policy', 'Hijyen Politikası'), onClick: () => onOpenLegal('hygiene') },
      { label: t('%100 Customer Satisfaction', '%100 Müşteri Memnuniyeti'), onClick: () => onOpenLegal('satisfaction') },
      { label: t('Contactless Delivery', 'Temassız Teslimat'), onClick: () => onOpenLegal('contactless') },
    ]
  };

  return (
    <footer className="bg-black text-white pt-12 pb-8 border-t border-white/5">
      <div className="container mx-auto px-4 md:px-8">
        {/* Middle Section: Links */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-12 mb-16">
          <div>
            <h4 className="text-lg font-black italic uppercase mb-8 tracking-tight">{t('OMASH', 'OMASH')}</h4>
            <ul className="space-y-4">
                  {footerLinks.about.map((link) => (
                <li key={link.label}>
                  <button onClick={link.onClick} className="text-sm font-bold text-white/60 hover:text-white transition-colors text-left">{link.label}</button>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="text-lg font-black italic uppercase mb-8 tracking-tight">{t('Help', 'Yardım')}</h4>
            <ul className="space-y-4">
              {footerLinks.help.map((link) => (
                <li key={link.label}>
                  <button onClick={link.onClick} className="text-sm font-bold text-white/60 hover:text-white transition-colors text-left">{link.label}</button>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="text-lg font-black italic uppercase mb-8 tracking-tight">{t('Corporate', 'Kurumsal')}</h4>
            <ul className="space-y-4">
              {footerLinks.corporate.map((link) => (
                <li key={link.label}>
                  <button onClick={link.onClick} className="text-sm font-bold text-white/60 hover:text-white transition-colors text-left">{link.label}</button>
                </li>
              ))}
            </ul>
          </div>

          <div className="space-y-4">
            <h4 className="text-lg font-black italic uppercase mb-4 tracking-tight">{t('Download App', 'Uygulamayı İndir')}</h4>
            <div className="flex flex-col gap-3">
              <a href="https://play.google.com" target="_blank" rel="noopener noreferrer" className="flex items-center gap-3 bg-black border border-white/20 rounded-xl px-4 py-2 hover:bg-white/5 transition-colors">
                <div className="w-8 h-8 flex items-center justify-center">
                  <svg viewBox="0 0 512 512" className="w-6 h-6">
                    <path d="M325.3 234.3L104.6 13l280.8 161.2-60.1 60.1z" fill="#48ff48"/>
                    <path d="M47 36c-1.3 2.7-2 5.9-2 9.3v421.4c0 3.3.7 6.4 2 9.1L273.9 256 47 36z" fill="#3bccff"/>
                    <path d="M325.4 277.7l60.1 60.1L104.6 499l220.8-221.3z" fill="#ff3333"/>
                    <path d="M347.3 256L405 198.3 467 234c11.7 6.8 11.7 18 0 24.8l-62 35.7L347.3 256z" fill="#ffd900"/>
                  </svg>
                </div>
                <div className="flex flex-col leading-none">
                  <span className="text-[10px] uppercase font-bold text-white/60">Google Play</span>
                  <span className="text-sm font-black italic tracking-tight uppercase">'DEN ALIN</span>
                </div>
              </a>
              <a href="https://apple.com/app-store" target="_blank" rel="noopener noreferrer" className="flex items-center gap-3 bg-black border border-white/20 rounded-xl px-4 py-2 hover:bg-white/5 transition-colors">
                <div className="w-8 h-8 flex items-center justify-center">
                  <svg viewBox="0 0 384 512" className="w-6 h-6 fill-white">
                    <path d="M318.7 268.7c-.2-36.7 16.4-64.4 50-84.8-18.8-26.9-47.2-41.7-84.7-44.6-35.5-2.8-74.3 20.7-88.5 20.7-15 0-49.4-19.7-76.4-19.7C63.3 141.2 4 184.8 4 273.5q0 39.3 14.4 81.2c12.8 36.7 59 126.7 107.2 125.2 25.2-.6 43-17.9 75.8-17.9 31.8 0 48.3 17.9 76.4 17.9 48.6-.7 90.4-82.5 102.6-119.3-65.2-30.7-61.7-90-61.7-91.9zm-56.6-164.2c27.3-32.4 24.8-61.9 24-72.5-24.1 1.4-52 16.4-67.9 34.9-17.5 19.8-27.8 44.3-25.6 71.9 26.1 2 49.9-11.4 69.5-34.3z"/>
                  </svg>
                </div>
                <div className="flex flex-col leading-none">
                  <span className="text-[10px] uppercase font-bold text-white/60">App Store'dan</span>
                  <span className="text-sm font-black italic tracking-tight uppercase">İNDİRİN</span>
                </div>
              </a>
            </div>
            <div className="pt-4">
              <a href="mailto:info@omashfood.com" className="text-sm font-bold text-white/60 hover:text-white transition-colors">info@omashfood.com</a>
            </div>
            <div className="pt-6 border-t border-white/10">
              <div className="flex items-center gap-6">
                <a href="https://facebook.com" target="_blank" rel="noopener noreferrer" className="text-white/60 hover:text-white transition-colors"><Facebook className="w-5 h-5 fill-current" /></a>
                <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" className="text-white/60 hover:text-white transition-colors"><Twitter className="w-5 h-5 fill-current" /></a>
                <a href="https://youtube.com" target="_blank" rel="noopener noreferrer" className="text-white/60 hover:text-white transition-colors"><Youtube className="w-5 h-5 fill-current" /></a>
                <a href="https://instagram.com" target="_blank" rel="noopener noreferrer" className="text-white/60 hover:text-white transition-colors"><Instagram className="w-5 h-5" /></a>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Section: Legal */}
        <div className="border-t border-white/10 pt-8 space-y-4">
          <p className="text-[11px] font-medium text-white/60 leading-relaxed">
            {t('Our prices are the recommended retail prices of OMASH.', 'Fiyatlarımız, OMASH\'ın tavsiye ettiği ürün satış fiyatlarıdır.')}
          </p>
          <p className="text-[11px] font-medium text-white/60 leading-relaxed">
            {t('The sausage, soujouk, cubed soujouk, pepperoni, ham and hamburger patties used in our products are produced from beef and/or turkey meat.', 'Ürünlerimizde kullandığımız sosis, sucuk, küp sucuk, pepperoni, jambon ve hamburger köftesi piliç ve/veya hindi etinden üretilmiştir.')}
          </p>
          <div className="pt-4 flex flex-col md:flex-row justify-between items-center gap-4 text-[10px] font-black uppercase tracking-widest text-white/60">
            <p>© 2026 OMASH FOOD GROUP. {t('ALL RIGHTS RESERVED.', 'TÜM HAKLARI SAKLIDIR.')}</p>
            <div className="flex gap-8">
              <button onClick={() => onOpenLegal('privacy')} className="hover:text-white transition-colors">{t('Privacy Policy', 'Gizlilik Politikası')}</button>
              <button onClick={() => onOpenLegal('terms')} className="hover:text-white transition-colors">{t('Terms of Service', 'Kullanım Koşulları')}</button>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

const ChatButton = ({ onClick }: { onClick: () => void }) => {
  return (
    <button 
      onClick={onClick}
      className="fixed bottom-8 right-8 z-[100] w-16 h-16 bg-[#0084FF] text-white rounded-full shadow-2xl flex items-center justify-center hover:scale-110 active:scale-95 transition-all group"
    >
      <MessageCircle className="w-8 h-8 fill-white group-hover:rotate-12 transition-transform" />
      <div className="absolute -top-1 -right-1 w-5 h-5 bg-brand-red rounded-full border-2 border-white flex items-center justify-center text-[10px] font-black">1</div>
    </button>
  );
};

const Locations = () => {
  const { lang, t } = useTranslation();
  const [selectedStore, setSelectedStore] = useState<string | null>(null);

  const stores = [
    { 
      name: "OMASH Güngören", 
      address: "Güngören, Bahçelievler, İstanbul", 
      status: t("Open until 11pm", "23:00'e kadar açık"),
      mapsUrl: "https://www.google.com/maps/dir/?api=1&destination=OMASH+Güngören+İstanbul"
    },
    { 
      name: "OMASH Şirinevler", 
      address: "Şirinevler, Bahçelievler, İstanbul", 
      status: t("COMING SOON", "ÇOK YAKINDA"), 
      isComingSoon: true,
      mapsUrl: "https://www.google.com/maps/dir/?api=1&destination=OMASH+Şirinevler+İstanbul"
    },
    { 
      name: "OMASH İncirli", 
      address: "İncirli, Bakırköy, İstanbul", 
      status: t("COMING SOON", "ÇOK YAKINDA"), 
      isComingSoon: true,
      mapsUrl: "https://www.google.com/maps/dir/?api=1&destination=OMASH+İncirli+İstanbul"
    },
  ];

  return (
    <section id="locations" className="pt-8 pb-12 bg-white scroll-mt-16">
      <div className="container mx-auto px-4 md:px-8">
        <div className="text-center mb-8">
          <h2 className="text-4xl md:text-6xl font-black tracking-tighter italic mb-4 uppercase">
            {lang === 'en' ? (
              <>FIND YOUR <span className="text-brand-red">NEAREST</span> LEVEL</>
            ) : (
              <>EN YAKIN <span className="text-brand-red">ŞUBEMİZİ</span> BULUN</>
            )}
          </h2>
          <p className="text-gray-500 font-medium max-w-2xl mx-auto">
            {t(
              "We're expanding fast. Find an OMASH location near you and experience the ultimate smash.",
              "Hızla büyüyoruz. Size en yakın OMASH şubesini bulun ve en üst düzey smash deneyimini yaşayın."
            )}
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-8">
          {stores.map((store, i) => (
            <div key={store.name} className="bg-brand-white border border-gray-100 p-8 rounded-3xl hover:shadow-xl transition-all group">
              <div className="bg-brand-red/10 w-12 h-12 rounded-xl flex items-center justify-center mb-6 group-hover:bg-brand-red group-hover:text-white transition-all">
                <MapPin className="w-6 h-6" />
              </div>
              <h3 className="text-2xl font-black italic mb-4">{store.name}</h3>
              <div className="space-y-3 font-medium text-sm">
                <p className="flex items-center gap-2 text-gray-500"><MapPin className="w-4 h-4 text-brand-red" /> {store.address}</p>
                <button 
                  onClick={() => !store.isComingSoon && setSelectedStore(store.name)}
                  className={cn(
                    "flex items-center gap-2 transition-all", 
                    store.isComingSoon ? "text-brand-red font-black" : "text-gray-500 hover:text-brand-red cursor-pointer underline decoration-dotted underline-offset-4"
                  )}
                >
                  <Clock className="w-4 h-4 text-brand-red" /> {store.status}
                </button>
              </div>
              <a 
                href={store.mapsUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full mt-8 border-2 border-brand-charcoal py-3 rounded-xl font-black hover:bg-brand-charcoal hover:text-white transition-all flex items-center justify-center"
              >
                {t('GET DIRECTIONS', 'YOL TARİFİ AL')}
              </a>
            </div>
          ))}
        </div>
      </div>

      <WorkingHoursModal 
        isOpen={!!selectedStore} 
        onClose={() => setSelectedStore(null)} 
        storeName={selectedStore || ''} 
      />
    </section>
  );
};

const FAQS = [
  { 
    id: 'faq-1',
    qEn: "What makes it a 'Smash' burger?", 
    qTr: "'Smash' burgeri özel yapan nedir?", 
    aEn: "We press our fresh beef patties onto a searing hot grill, creating a thin, crispy crust (the Maillard reaction) while keeping the center incredibly juicy.", 
    aTr: "Taze dana eti köftelerimizi çok sıcak bir ızgaraya bastırarak, merkezini inanılmaz derecede sulu tutarken ince, çıtır bir kabuk (Maillard reaksiyonu) oluşturuyoruz." 
  },
  { 
    id: 'faq-2',
    qEn: "Do you have vegan options?", 
    qTr: "Vegan seçenekleriniz var mı?", 
    aEn: "Yes! Any of our levels can be swapped with a premium plant-based patty. Just ask for the 'Green Mode' upgrade.", 
    aTr: "Evet! Herhangi bir seviyemiz premium bitki bazlı köfte ile değiştirilebilir. Sadece 'Green Mode' yükseltmesini isteyin." 
  },
  { 
    id: 'faq-3',
    qEn: "How fast is delivery?", 
    qTr: "Teslimat ne kadar hızlı?", 
    aEn: "We aim for under 30 minutes. Our kitchen is optimized for speed without sacrificing quality.", 
    aTr: "30 dakikanın altını hedefliyoruz. Mutfağımız, kaliteden ödün vermeden hız için optimize edilmiştir." 
  },
  { 
    id: 'faq-4',
    qEn: "Can I customize my burger?", 
    qTr: "Burgerimi kişiselleştirebilir miyim?", 
    aEn: "Absolutely. While our levels are pre-configured for maximum performance, you can add or remove any component in the ordering modal.", 
    aTr: "Kesinlikle. Seviyelerimiz maksimum performans için önceden yapılandırılmış olsa da, sipariş ekranında herhangi bir bileşeni ekleyebilir veya çıkarabilirsiniz." 
  },
];

const FAQ = () => {
  const { lang, t } = useTranslation();
  
  return (
    <section id="faq" className="py-24 bg-brand-white border-t border-gray-100">
      <div className="container mx-auto px-4 md:px-8 max-w-4xl">
        <h2 className="text-4xl font-black tracking-tighter italic mb-12 text-center uppercase">
          SYSTEM <span className="text-brand-red">INTEL</span> (FAQ)
        </h2>
        <div className="space-y-6">
          {FAQS.map((faq) => (
            <div key={faq.id} className="bg-white border border-gray-100 p-6 rounded-2xl">
              <h4 className="font-black text-lg mb-2 flex items-center gap-3 italic">
                <span className="text-brand-red">Q:</span> {lang === 'en' ? faq.qEn : faq.qTr}
              </h4>
              <p className="text-gray-500 font-medium leading-relaxed">
                {lang === 'en' ? faq.aEn : faq.aTr}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

const GlobalSearchResults = ({ setCurrentPage }: { setCurrentPage: (page: any) => void }) => {
  const { lang, t, formatPrice } = useTranslation();
  const { searchQuery, setSearchQuery } = useSearch();
  const { addToCart } = useCart();

  const query = searchQuery.toLowerCase();

  const menuResults = MENU_ITEMS.filter(item => 
    item.name.toLowerCase().includes(query) ||
    item.description.toLowerCase().includes(query) ||
    (item.descriptionTr && item.descriptionTr.toLowerCase().includes(query))
  );

  const locationResults = LOCATIONS.filter(loc => 
    loc.name.toLowerCase().includes(query) ||
    loc.address.toLowerCase().includes(query)
  );

  const faqResults = FAQS.filter(faq => 
    faq.qEn.toLowerCase().includes(query) ||
    faq.qTr.toLowerCase().includes(query) ||
    faq.aEn.toLowerCase().includes(query) ||
    faq.aTr.toLowerCase().includes(query)
  );

  const totalResults = menuResults.length + locationResults.length + faqResults.length;

  if (totalResults === 0) {
    return (
      <div className="pt-32 pb-24 bg-brand-white min-h-screen flex items-center justify-center">
        <div className="container mx-auto px-4 text-center">
          <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <Search className="w-10 h-10 text-gray-300" />
          </div>
          <h2 className="text-3xl font-black italic uppercase mb-2 tracking-tight">{t('No results found', 'Sonuç bulunamadı')}</h2>
          <p className="text-gray-500 font-medium mb-8 max-w-md mx-auto">
            {t(`We couldn't find anything matching "${searchQuery}". Try checking your spelling or using more general terms.`, `"${searchQuery}" ile eşleşen bir şey bulamadık. Yazımınızı kontrol etmeyi veya daha genel terimler kullanmayı deneyin.`)}
          </p>
          <button 
            onClick={() => setSearchQuery('')}
            className="bg-brand-red text-white px-10 py-4 rounded-2xl font-black text-lg hover:bg-brand-charcoal transition-all shadow-xl shadow-brand-red/20 active:scale-95"
          >
            {t('CLEAR SEARCH', 'ARAMAYI TEMİZLE')}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-32 pb-24 bg-brand-white min-h-screen">
      <div className="container mx-auto px-4 md:px-8">
        <div className="mb-12">
          <h1 className="text-4xl md:text-6xl font-black italic tracking-tighter uppercase mb-4">
            {t('SEARCH', 'ARAMA')} <span className="text-brand-red">{t('RESULTS', 'SONUÇLARI')}</span>
          </h1>
          <p className="text-gray-500 font-medium">
            {t(`Found ${totalResults} results for "${searchQuery}"`, `"${searchQuery}" için ${totalResults} sonuç bulundu`)}
          </p>
        </div>

        {menuResults.length > 0 && (
          <div className="mb-16">
            <h3 className="text-2xl font-black italic uppercase mb-8 flex items-center gap-3">
              <ShoppingBag className="w-6 h-6 text-brand-red" />
              {t('MENU ITEMS', 'MENÜ ÖĞELERİ')}
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {menuResults.map(item => (
                <div key={item.id} className="bg-white p-6 rounded-3xl border border-gray-100 flex gap-4 hover:shadow-xl transition-all group">
                  <div className="w-24 h-24 rounded-2xl overflow-hidden shrink-0">
                    <img src={item.image} alt={item.name} className="w-full h-full object-cover group-hover:scale-110 transition-transform" referrerPolicy="no-referrer" />
                  </div>
                  <div className="flex-1 min-w-0">
                    {(lang === 'en' ? item.tagline : item.taglineTr) && (
                      <span className="inline-block bg-brand-red text-white text-[8px] font-black italic uppercase tracking-tighter px-1.5 py-0.5 mb-1">
                        {lang === 'en' ? item.tagline : item.taglineTr}
                      </span>
                    )}
                    <h4 className="font-black italic uppercase text-lg mb-1 truncate">{formatItemName(item.name)}</h4>
                    <p className="text-gray-500 text-xs line-clamp-2 mb-3 font-medium">
                      {lang === 'en' ? item.description : (item.descriptionTr || item.description)}
                    </p>
                    <div className="flex items-center justify-between">
                      <span className="font-black text-brand-red">{formatPrice(item.price)}</span>
                      <button 
                        onClick={() => addToCart({ ...item, quantity: 1, selectedExtras: [] })}
                        className="text-[10px] font-black bg-brand-charcoal text-white px-3 py-1.5 rounded-lg hover:bg-brand-red transition-colors"
                      >
                        {t('ADD', 'EKLE')}
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {locationResults.length > 0 && (
          <div className="mb-16">
            <h3 className="text-2xl font-black italic uppercase mb-8 flex items-center gap-3">
              <MapPin className="w-6 h-6 text-brand-red" />
              {t('LOCATIONS', 'ŞUBELER')}
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {locationResults.map(loc => (
                <div key={loc.id} className="bg-white p-8 rounded-3xl border border-gray-100 hover:shadow-xl transition-all">
                  <h4 className="font-black italic uppercase text-xl mb-2">{loc.name}</h4>
                  <p className="text-gray-500 font-medium mb-6 flex items-start gap-2">
                    <MapPin className="w-4 h-4 text-brand-red shrink-0 mt-1" />
                    {loc.address}
                  </p>
                  <button 
                    onClick={() => { setCurrentPage('locations'); setSearchQuery(''); }}
                    className="text-xs font-black text-brand-red uppercase tracking-widest hover:underline"
                  >
                    {t('VIEW ON MAP', 'HARİTADA GÖR')}
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {faqResults.length > 0 && (
          <div>
            <h3 className="text-2xl font-black italic uppercase mb-8 flex items-center gap-3">
              <Info className="w-6 h-6 text-brand-red" />
              {t('HELP & FAQ', 'YARDIM & SSS')}
            </h3>
            <div className="space-y-4">
              {faqResults.map(faq => (
                <div key={faq.id} className="bg-white p-6 rounded-3xl border border-gray-100">
                  <h4 className="font-black text-lg mb-2 flex items-center gap-3 italic">
                    <span className="text-brand-red">Q:</span> {lang === 'en' ? faq.qEn : faq.qTr}
                  </h4>
                  <p className="text-gray-500 font-medium leading-relaxed">
                    {lang === 'en' ? faq.aEn : faq.aTr}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const ProductInfoModal = ({ item, isOpen, onClose, onUpgrade }: { item: MenuItem | null, isOpen: boolean, onClose: () => void, onUpgrade: (item: MenuItem) => void }) => {
  const { lang, t, formatPrice } = useTranslation();
  const { addToCart } = useCart();
  const [showNutrition, setShowNutrition] = useState(false);

  if (!item) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[110] flex items-center justify-center p-4">
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-brand-charcoal/60 backdrop-blur-sm"
          />
          <motion.div 
            initial={{ scale: 0.9, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.9, opacity: 0, y: 20 }}
            className="bg-white w-full max-w-md rounded-[2.5rem] overflow-hidden shadow-2xl relative z-10 max-h-[90vh] flex flex-col"
          >
            <div className="relative h-64 md:h-80 shrink-0">
              <img 
                src={item.image} 
                alt={item.name} 
                className="w-full h-full object-cover"
                referrerPolicy="no-referrer"
              />
              <button 
                onClick={onClose}
                className="absolute top-4 right-4 p-2 bg-white/20 backdrop-blur-md text-white rounded-full hover:bg-brand-red hover:text-white transition-colors z-20"
              >
                <X className="w-5 h-5" />
              </button>
              
              {item.isBestseller && (
                <div className="absolute top-4 left-4 bg-brand-red text-white py-1.5 px-4 rounded-xl flex items-center gap-2 shadow-lg border border-white/20">
                  <Star className="w-4 h-4 fill-white" />
                  <span className="font-black italic uppercase tracking-tighter text-xs">
                    {t('STAR OF THE WEEK', 'HAFTANIN YILDIZI')}
                  </span>
                </div>
              )}
            </div>

            <div className="p-6 md:p-8 overflow-y-auto">
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  {item.level && <span className="block text-brand-red text-3xl md:text-5xl font-black italic leading-none mb-2">{item.level}</span>}
                  {(lang === 'en' ? item.tagline : item.taglineTr) && (
                    <span className="inline-block bg-brand-red text-white text-[10px] md:text-sm font-black italic uppercase tracking-tighter px-2 py-0.5 mb-2">
                      {lang === 'en' ? item.tagline : item.taglineTr}
                    </span>
                  )}
                  <h3 className="text-xl md:text-3xl font-black italic uppercase leading-tight mb-2">
                    {formatItemName(item.name)}
                  </h3>
                </div>
                <span className="text-xl md:text-2xl font-black text-brand-red italic ml-4">{formatPrice(item.price)}</span>
              </div>

              <div className="mb-4">
                <ExpandableDescription 
                  text={lang === 'en' ? item.description : (item.descriptionTr || item.description)} 
                  id={`modal-${item.id}`} 
                  t={t} 
                />
              </div>

              <AllergenGridDisplay allergens={item.allergens} lang={lang} t={t} />

              <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                <div className="flex flex-col">
                  <button 
                    onClick={() => setShowNutrition(true)}
                    className="flex items-center gap-2 text-xs font-bold text-gray-400 uppercase tracking-wider hover:text-brand-red transition-colors group/cal"
                  >
                    <Info className="w-4 h-4 group-hover/cal:scale-110 transition-transform" />
                    {item.calories} {t('kcal', 'kalori')}
                  </button>
                  {(item.category === 'burgers' || item.category === 'menus' || item.category === 'doner') && item.level !== '128 BIT' && (
                    <span className="text-[10px] font-black text-brand-red uppercase mt-1">
                      {t('UPGRADE AVAILABLE', 'YÜKSELTME MEVCUT')}
                    </span>
                  )}
                </div>
                <button 
                  onClick={() => {
                    if ((item.category === 'burgers' || item.category === 'menus' || item.category === 'doner') && item.level !== '128 BIT') {
                      onUpgrade(item);
                      onClose();
                    } else {
                      addToCart(item);
                      onClose();
                    }
                  }}
                  className="bg-brand-charcoal text-white p-4 rounded-2xl shadow-xl hover:bg-brand-red transition-all active:scale-90"
                >
                  <Plus className="w-6 h-6" />
                </button>
              </div>
            </div>
          </motion.div>
        </div>
      )}
      <NutritionModal 
        item={item} 
        isOpen={showNutrition} 
        onClose={() => setShowNutrition(false)} 
      />
    </AnimatePresence>
  );
};

const FullMenuPage = () => {
  const { lang, t, formatPrice } = useTranslation();
  const { addToCart } = useCart();
  const [selectedProduct, setSelectedProduct] = useState<MenuItem | null>(null);
  const [selectedUpgradeItem, setSelectedUpgradeItem] = useState<MenuItem | null>(null);
  const [selectedNutritionItem, setSelectedNutritionItem] = useState<MenuItem | null>(null);
  const [activeFilter, setActiveFilter] = useState<'all' | 'high-protein' | 'low-carb' | 'low-cal' | 'mass-gainer'>('all');
  const [selectedCategoryView, setSelectedCategoryView] = useState<string | null>(null);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  const filteredItems = MENU_ITEMS.flatMap(item => {
    const items = [item];
    if (item.category === 'doner') {
      const isBox = item.id.includes('box');
      items.push({
        ...item,
        id: `${item.id}-menu`,
        name: `${item.name} MENU`, 
        price: isBox ? item.price + 60 : item.price + 110, 
        category: 'menus', 
        description: isBox ? item.description + ' + Drink' : item.description + ' + Fries + Drink', 
        descriptionTr: isBox 
          ? (item.descriptionTr || item.description) + ' + İçecek'
          : (item.descriptionTr || item.description) + ' + Patates + İçecek' 
      } );
    }
    return items;
  }).filter(item => {
    if (activeFilter === 'all') return true;
    if (activeFilter === 'high-protein') return item.nutrition && item.nutrition.protein >= 30;
    if (activeFilter === 'low-carb') return item.nutrition && item.nutrition.carbs <= 40;
    if (activeFilter === 'low-cal') return item.calories && item.calories <= 500;
    if (activeFilter === 'mass-gainer') return item.calories && item.calories >= 800;
    return true;
  });

  const filters = [
    { id: 'all', label: t('ALL', 'TÜMÜ'), icon: <MenuIcon className="w-5 h-5" /> },
    { id: 'high-protein', label: t('PROTEIN', 'PROTEİN'), icon: <Activity className="w-4 h-4" />, sub: '> 30G' },
    { id: 'low-carb', label: t('LOW CARB', 'DÜŞÜK KARB'), icon: <Wheat className="w-4 h-4" />, sub: '< 40G' },
    { id: 'low-cal', label: t('LOW CAL', 'DÜŞÜK KAL'), icon: <Zap className="w-4 h-4" />, sub: '< 500' },
    { id: 'mass-gainer', label: t('MASS', 'KÜTLE'), icon: <Flame className="w-4 h-4" />, sub: '> 800' },
  ];

  const categoriesList = ['burgers', 'doner', 'pizza', 'menus', 'chicken', 'breakfast', 'sides', 'kids', 'drinks', 'sauces', 'desserts'];
  
  const getCategoryName = (cat: string) => {
    return t(
      cat === 'kids' ? 'Kids Menu' : cat === 'desserts' ? 'Desserts' : cat === 'breakfast' ? 'Breakfast' : cat === 'sauces' ? 'Sauces' : cat.charAt(0).toUpperCase() + cat.slice(1), 
      cat === 'burgers' ? 'Burger' : 
      cat === 'menus' ? 'Menüler' : 
      cat === 'chicken' ? 'Tavuk' : 
      cat === 'doner' ? 'Döner' : 
      cat === 'pizza' ? 'Pizza' : 
      cat === 'sides' ? 'Yan Ürünler' : 
      cat === 'drinks' ? 'İçecekler' : 
      cat === 'kids' ? 'Mini Menü' : 
      cat === 'desserts' ? 'Tatlılar' : 
      cat === 'breakfast' ? 'Kahvaltı' : 
      cat === 'sauces' ? 'Soslar' : cat
    );
  };

  const groupedItems = categoriesList.reduce((acc, cat) => {
    const items = filteredItems.filter(item => item.category === cat);
    if (items.length > 0) acc[cat] = items;
    return acc;
  }, {} as Record<string, typeof filteredItems>);

  const renderCategoryGrid = () => (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
      {categoriesList.filter(cat => groupedItems[cat]).map(cat => {
        const firstItem = groupedItems[cat][0];
        return (
          <div 
            key={`cat-grid-${cat}`}
            onClick={() => {
              setSelectedCategoryView(cat);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            className="bg-white rounded-[2.5rem] overflow-hidden shadow-sm hover:shadow-2xl border border-gray-100 cursor-pointer group transition-all"
          >
            <div className="aspect-[4/3] bg-gray-100 relative overflow-hidden">
              <img 
                src={firstItem.image} 
                alt={getCategoryName(cat)} 
                className="absolute inset-0 w-full h-full object-cover group-hover:scale-110 group-active:scale-95 transition-transform duration-500" 
                referrerPolicy="no-referrer"
              />
            </div>
            <div className="p-8 text-center bg-white">
              <h3 className="text-3xl md:text-4xl font-black italic uppercase text-brand-charcoal group-hover:text-brand-red transition-colors">
                {getCategoryName(cat)}
              </h3>
            </div>
          </div>
        );
      })}
    </div>
  );

  return (
    <div className="pt-32 pb-24 bg-brand-white min-h-screen">
      <div className="container mx-auto px-4 md:px-8">
        <div className="text-center mb-12">
          {selectedCategoryView ? (
            <div className="flex flex-col items-center gap-4 mb-4">
              <button 
                onClick={() => setSelectedCategoryView(null)}
                className="text-gray-400 hover:text-brand-red font-bold uppercase tracking-wider text-sm flex items-center gap-2 transition-colors"
              >
                ← {t('BACK TO CATEGORIES', 'KATEGORİLERE DÖN')}
              </button>
              <h1 className="text-5xl md:text-8xl font-black italic tracking-tighter uppercase">
                {getCategoryName(selectedCategoryView)}
              </h1>
            </div>
          ) : (
            <>
              <h1 className="text-5xl md:text-8xl font-black italic tracking-tighter mb-4 uppercase">
                OMASH <span className="text-brand-red">{t('FLAVORS', 'LEZZETLERİ')}</span>
              </h1>
              <p className="text-gray-500 font-medium max-w-2xl mx-auto text-lg mb-12">
                {t('Explore our entire performance-boosted lineup.', 'Tüm performans artırıcı ürünlerimizi keşfedin.')}
              </p>
            </>
          )}

          {/* Category Slider */}
          <div className="flex gap-1 bg-white p-1.5 rounded-2xl overflow-x-auto no-scrollbar border border-gray-100 scroll-smooth mb-12 max-w-[1240px] mx-auto w-full shadow-sm">
            {(['burgers', 'doner', 'pizza', 'menus', 'chicken', 'breakfast', 'sides', 'kids', 'drinks', 'sauces', 'desserts'] as const).map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategoryView(selectedCategoryView === cat ? null : cat)}
                className={cn(
                  "px-8 py-3 rounded-xl font-bold text-sm uppercase tracking-wider transition-all whitespace-nowrap",
                  selectedCategoryView === cat 
                    ? "bg-brand-red text-white shadow-lg shadow-brand-red/20" 
                    : "text-gray-500 hover:text-brand-charcoal"
                )}
              >
                {t(cat === 'kids' ? 'Kids Menu' : cat === 'desserts' ? 'Desserts' : cat === 'breakfast' ? 'Breakfast' : cat === 'sauces' ? 'Sauces' : cat.charAt(0).toUpperCase() + cat.slice(1), 
                  cat === 'burgers' ? 'Burger' : 
                  cat === 'menus' ? 'Menüler' : 
                  cat === 'chicken' ? 'Tavuk' : 
                  cat === 'doner' ? 'Döner' : 
                  cat === 'pizza' ? 'Pizza' : 
                  cat === 'sides' ? 'Yan Ürünler' : 
                  cat === 'drinks' ? 'İçecekler' : 
                  cat === 'kids' ? 'Mini Menü' :
                  cat === 'breakfast' ? 'Kahvaltı' :
                  cat === 'sauces' ? 'Soslar' :
                  'Tatlılar'
                )}
              </button>
            ))}
          </div>
        </div>

        <AnimatePresence mode="wait">
          <motion.div 
            key={`${activeFilter}-${selectedCategoryView || 'grid'}`}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
          >
            {!selectedCategoryView ? (
              renderCategoryGrid()
            ) : (
              <div className="space-y-16">
                {groupedItems[selectedCategoryView] && (
                  <div key={`fullmenu-catGroup-${selectedCategoryView}`}>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                      {groupedItems[selectedCategoryView].map((item) => (
                        <div 
                          key={`fullmenu-item-${item.id}`} 
                          onClick={() => setSelectedProduct(item)}
                          className="bg-white p-6 rounded-[2.5rem] border border-gray-100 flex gap-4 hover:shadow-xl transition-all group cursor-pointer active:scale-[0.98]"
                        >
                          <div className="w-32 h-32 md:w-40 md:h-40 rounded-3xl overflow-hidden shrink-0 bg-gray-50">
                            <img 
                              src={item.image} 
                              alt={item.name} 
                              className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" 
                              referrerPolicy="no-referrer" 
                            />
                          </div>
                          <div className="flex flex-col justify-between flex-1 min-w-0">
                            <div>
                              {(lang === 'en' ? item.tagline : item.taglineTr) && (
                                <span className="inline-block bg-brand-red text-white text-[8px] md:text-[10px] font-black italic uppercase tracking-tighter px-1.5 py-0.5 mb-1">
                                  {lang === 'en' ? item.tagline : item.taglineTr}
                                </span>
                              )}
                              <h3 className="font-black italic uppercase text-lg md:text-xl mb-1 truncate leading-tight">
                                {formatItemName(item.name)}
                              </h3>
                              <p className="text-gray-500 text-xs md:text-sm font-medium line-clamp-2 mb-2">
                                {lang === 'en' ? item.description : (item.descriptionTr || item.description)}
                              </p>

                              {item.allergens && item.allergens.length > 0 && (
                                <div className="flex flex-wrap gap-1 mb-2">
                                  {item.allergens.map((allergen, i) => {
                                    const info = ALLERGEN_ICONS[allergen];
                                    if (!info) return null;
                                    return (
                                      <button 
                                        key={`full-allergen-${item.id}-${allergen}-${i}`}
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          setSelectedNutritionItem(item);
                                        }}
                                        className="w-5 h-5 rounded-md bg-gray-50 border border-gray-100 flex items-center justify-center text-[10px] hover:bg-brand-red/5 hover:border-brand-red/20 transition-all active:scale-90"
                                      >
                                        {info.icon}
                                      </button>
                                    );
                                  })}
                                </div>
                              )}
                            </div>
                            <div className="flex items-center justify-between mt-auto">
                              <span className="font-black text-brand-red text-lg md:text-xl italic">{formatPrice(item.price)}</span>
                              <button 
                                onClick={(e) => {
                                  e.stopPropagation();
                                  if ((item.category === 'burgers' || item.category === 'menus' || item.category === 'doner') && item.level !== '128 BIT') {
                                    setSelectedUpgradeItem(item);
                                  } else {
                                    addToCart({ ...item, quantity: 1, selectedExtras: [] });
                                  }
                                }}
                                className="bg-brand-charcoal text-white px-4 py-2 md:px-6 md:py-2.5 rounded-xl font-black text-xs md:text-sm hover:bg-brand-red transition-all active:scale-95 uppercase"
                              >
                                {t('ADD', 'EKLE')}
                              </button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
                
                {/* Show empty state if no items match filter */}
                {!groupedItems[selectedCategoryView] && (
                  <div className="text-center py-24">
                    <p className="text-gray-500 font-bold text-xl uppercase">
                      {t('No items found matching the selected filter.', 'Seçili filtreye uygun ürün bulunamadı.')}
                    </p>
                  </div>
                )}
              </div>
            )}
          </motion.div>
        </AnimatePresence>
      </div>

      <ProductInfoModal 
        item={selectedProduct} 
        isOpen={!!selectedProduct} 
        onClose={() => setSelectedProduct(null)}
        onUpgrade={(item) => setSelectedUpgradeItem(item)}
      />

      <UpgradeModal 
        item={selectedUpgradeItem} 
        isOpen={!!selectedUpgradeItem} 
        onClose={() => setSelectedUpgradeItem(null)} 
        onShowNutrition={(item) => setSelectedNutritionItem(item)}
      />

      <NutritionModal 
        item={selectedNutritionItem} 
        isOpen={!!selectedNutritionItem} 
        onClose={() => setSelectedNutritionItem(null)} 
      />
    </div>
  );
};

const DealsPage = () => {
  const { lang, t, formatPrice } = useTranslation();
  const { addToCart } = useCart();
  
  // Use 'menus' category as placeholder for deals/campaigns
  const deals = MENU_ITEMS.filter(item => item.category === 'menus');

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <div className="pt-32 pb-24 bg-brand-white min-h-screen">
      <div className="container mx-auto px-4 md:px-8">
        <div className="text-center mb-16">
          <h1 className="text-5xl md:text-8xl font-black italic tracking-tighter mb-4 uppercase">
            SYSTEM <span className="text-brand-red">DEALS</span>
          </h1>
          <p className="text-gray-500 font-medium max-w-2xl mx-auto text-lg">
            {t('Exclusive performance-boosted bundles and limited time offers.', 'Özel performans artırıcı paketler ve sınırlı süreli teklifler.')}
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {deals.map((item) => (
            <div key={item.id} className="bg-white rounded-[2.5rem] overflow-hidden border border-gray-100 shadow-xl hover:shadow-2xl transition-all group">
              <div className="relative h-64 overflow-hidden">
                <img 
                  src={item.image} 
                  alt={item.name} 
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                  referrerPolicy="no-referrer"
                />
                <div className="absolute top-6 left-6">
                  <span className="bg-brand-red text-white px-4 py-2 rounded-xl font-black text-xs uppercase tracking-widest shadow-lg">
                    {t('CAMPAIGN', 'KAMPANYA')}
                  </span>
                </div>
              </div>
              <div className="p-8">
                {(lang === 'en' ? item.tagline : item.taglineTr) && (
                  <span className="inline-block bg-brand-red text-white text-xs font-black italic uppercase tracking-tighter px-2 py-0.5 mb-2">
                    {lang === 'en' ? item.tagline : item.taglineTr}
                  </span>
                )}
                <h3 className="text-2xl font-black italic mb-2 uppercase tracking-tight">{formatItemName(item.name)}</h3>
                <p className="text-gray-500 text-sm font-medium mb-6 line-clamp-2">
                  {lang === 'en' ? item.description : item.descriptionTr}
                </p>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-black text-brand-red italic">{formatPrice(item.price)}</span>
                  <button 
                    onClick={() => addToCart({ ...item, quantity: 1, selectedExtras: [] })}
                    className="bg-brand-charcoal text-white px-6 py-3 rounded-xl font-black text-sm hover:bg-brand-red transition-all active:scale-95"
                  >
                    {t('ADD TO BAG', 'SEPETE EKLE')}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

const GuaranteePage = () => {
  const { t } = useTranslation();
  return (
    <div className="pt-24 bg-white min-h-screen">
      <section className="py-24 bg-brand-charcoal text-white relative overflow-hidden">
        <div className="absolute top-0 right-0 w-1/3 h-full opacity-10 bg-[url('https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&q=80')] bg-cover bg-center" />
        <div className="container mx-auto px-4 md:px-8 relative z-10">
          <div className="max-w-3xl">
            <span className="text-brand-red font-black uppercase tracking-widest text-sm mb-4 block">
              {t('OMASH Quality Standards', 'OMASH Kalite Standartları')}
            </span>
            <h2 className="text-5xl md:text-7xl font-black tracking-tighter italic mb-8 uppercase leading-none">
              OMASH <span className="text-brand-red">{t('TASTE', 'LEZZET')}</span><br />{t('GUARANTEE', 'GARANTİSİ')}
            </h2>
            <p className="text-gray-400 font-medium text-lg leading-relaxed mb-8">
              {t(
                "Because great performance requires the best fuel. We don't compromise on ingredients, hygiene, or our preparation methods. Every OMASH product is a result of meticulous quality control from farm to table.",
                "Çünkü yüksek performans en iyi yakıtı gerektirir. Malzemelerimizden, hijyen standartlarımızdan veya hazırlama yöntemlerimizden asla ödün vermeyiz. Her OMASH ürünü tarladan masaya titiz bir kalite kontrolünün eseridir."
              )}
            </p>
          </div>
        </div>
      </section>

      <section className="py-24">
        <div className="container mx-auto px-4 md:px-8">
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div className="bg-gray-50 border border-gray-100 p-8 rounded-3xl hover:border-brand-red transition-all group">
              <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-brand-red mb-6 shadow-sm group-hover:scale-110 transition-transform">
                <ChefHat className="w-8 h-8" />
              </div>
              <h3 className="text-2xl font-black italic uppercase tracking-tight mb-4">{t('100% Pure Beef', '%100 Gerçek Dana Eti')}</h3>
              <p className="text-gray-500 font-medium">
                {t(
                  "We only use 100% pure beef without absolutely any fillers, additives, or artificial preservatives. Grilled to perfection ensuring maximum juiciness and flavor.",
                  "Köftelerimizde hiçbir koruyucu, katkı maddesi veya dolgu malzemesi olmadan sadece %100 saf dana eti kullanıyoruz. Gerçek etin lezzetini mühürleyerek sunuyoruz."
                )}
              </p>
            </div>

            <div className="bg-gray-50 border border-gray-100 p-8 rounded-3xl hover:border-brand-red transition-all group">
              <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-brand-red mb-6 shadow-sm group-hover:scale-110 transition-transform">
                <Leaf className="w-8 h-8" />
              </div>
              <h3 className="text-2xl font-black italic uppercase tracking-tight mb-4">{t('Fresh Produce', 'Günlük Taze Sebzeler')}</h3>
              <p className="text-gray-500 font-medium">
                {t(
                  "Crisp lettuce, ripe tomatoes, and fresh onions sourced strictly from reliable local agricultural partners to guarantee that farm-fresh snap in every bite.",
                  "Çıtır marullar, olgun domatesler ve taze soğanlar. Her ısırıkta o tazeliği hissedebilmeniz için güvenilir yerli tarım ortaklarımızdan günlük olarak tedarik ediliyor."
                )}
              </p>
            </div>

            <div className="bg-gray-50 border border-gray-100 p-8 rounded-3xl hover:border-brand-red transition-all group">
              <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-brand-red mb-6 shadow-sm group-hover:scale-110 transition-transform">
                <ShieldCheck className="w-8 h-8" />
              </div>
              <h3 className="text-2xl font-black italic uppercase tracking-tight mb-4">{t('Ultimate Hygiene', 'Üstün Hijyen')}</h3>
              <p className="text-gray-500 font-medium">
                {t(
                  "Food safety is our uncompromisable priority. Our kitchens operate under draconian sanitation protocols exceeding international health board standards.",
                  "Gıda güvenliği taviz verilmez önceliğimizdir. Mutfaklarımız her gün düzenli olarak denetlenir ve uluslararası sağlık standartlarının ötesinde hijyen protokolleriyle çalışır."
                )}
              </p>
            </div>

            <div className="bg-gray-50 border border-gray-100 p-8 rounded-3xl hover:border-brand-red transition-all group">
              <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-brand-red mb-6 shadow-sm group-hover:scale-110 transition-transform">
                <Wheat className="w-8 h-8" />
              </div>
              <h3 className="text-2xl font-black italic uppercase tracking-tight mb-4">{t('Artisan Potato Buns', 'Özel Potato Bun')}</h3>
              <p className="text-gray-500 font-medium">
                {t(
                  "Our signature Potato Buns are baked fresh daily exclusively for OMASH. They deliver the perfect soft yet resilient texture that holds the smash securely without breaking.",
                  "İmza niteliğindeki Potato Bun ekmeklerimiz her gün OMASH için özel olarak taze pişirilir. İçinde bol malzemeyi tutarken dağılmayan, yumuşacık mükemmel bir doku sunar."
                )}
              </p>
            </div>

            <div className="bg-gray-50 border border-gray-100 p-8 rounded-3xl hover:border-brand-red transition-all group lg:col-span-2">
              <div className="flex flex-col md:flex-row gap-8 items-center">
                <div className="w-full md:w-1/3 aspect-square rounded-2xl bg-brand-charcoal overflow-hidden relative shadow-lg">
                   <img src="https://images.unsplash.com/photo-1594179047519-f347310d3322?auto=format&fit=crop&q=80&w=600" alt="Quality Standard" className="w-full h-full object-cover" />
                   <div className="absolute inset-0 bg-brand-red/20 mix-blend-multiply" />
                </div>
                <div className="flex-1">
                  <div className="w-12 h-12 bg-brand-red/10 rounded-xl flex items-center justify-center text-brand-red mb-4">
                    <HeartHandshake className="w-6 h-6" />
                  </div>
                  <h3 className="text-3xl font-black italic uppercase tracking-tight mb-4">{t('The OMASH Promise', 'OMASH Sözü')}</h3>
                  <p className="text-gray-500 font-medium text-lg leading-relaxed">
                    {t(
                      "If your meal isn't hot, fresh, and up to the OMASH standard, let us know immediately. We will replace it right there. Your satisfaction makes us who we are.",
                      "Eğer yemeğiniz sıcak, taze ve OMASH standartlarında değilse, bize anında bildirin. Tereddütsüz değiştireceğiz. Bizi biz yapan şey sizin memnuniyetinizdir."
                    )}
                  </p>
                </div>
              </div>
            </div>

          </div>
        </div>
      </section>
    </div>
  );
};

const AboutPage = () => {
  const { t } = useTranslation();
  
  const stats = [
    { value: '100%', label: t('Halal Certified', 'Helal Sertifikalı') },
    { value: '0', label: t('Compromises', 'Taviz') },
    { value: '+50', label: t('Locations Planned', 'Planlanan Şube') },
    { value: '3M+', label: t('Smash Burgers', 'Smash Burger (Satılan)') },
  ];

  const pillars = [
    {
      icon: <Target className="w-8 h-8" />,
      title: t('Our Mission', 'Misyonumuz'),
      desc: t(
        'To redefine the fast-casual dining experience by delivering consistently superior smash burgers crafted from premium ingredients, while pioneering operational efficiency and customer satisfaction.',
        'Birinci sınıf malzemelerden üretilen üstün smash burgerleri istikrarlı bir şekilde sunarken, operasyonel verimlilik ve müşteri memnuniyetinde öncü olarak hızlı tüketim (fast-casual) restoran deneyimini yeniden tanımlamak.'
      )
    },
    {
      icon: <Globe2 className="w-8 h-8" />,
      title: t('Our Vision', 'Vizyonumuz'),
      desc: t(
        'To become the undisputed global leader in the smash burger category, setting the industry benchmark for brand vibrancy, rapid expansion, and uncompromising food quality.',
        'Smash burger kategorisinde tartışmasız küresel lider olmak, marka canlılığı, hızlı büyüme ve tavizsiz yemek kalitesi konusunda sektör standartlarını belirlemek.'
      )
    },
    {
      icon: <Building2 className="w-8 h-8" />,
      title: t('Corporate Values', 'Kurumsal Değerlerimiz'),
      desc: t(
        'Transparency, Hygiene First, Culinary Innovation, Agile Growth, and 100% Commitment to our partners and guests forming the OMASH Food Group ecosystem.',
        'Şeffaflık, Önce Hijyen, Mutfak İnovasyonu, Çevik Büyüme ve OMASH Food Group ekosistemini oluşturan iş ortaklarımıza ve misafirlerimize %100 Bağlılık.'
      )
    }
  ];

  return (
    <div className="pt-32 pb-24 min-h-screen bg-brand-white">
      {/* Hero Section */}
      <section className="container mx-auto px-4 md:px-8 mb-24">
        <div className="flex flex-col md:flex-row gap-12 items-center">
          <div className="md:w-1/2">
            <span className="text-brand-red font-black uppercase tracking-widest text-sm mb-4 block">
              {t('OMASH Food Group', 'OMASH Gıda A.Ş.')}
            </span>
            <h1 className="text-5xl md:text-7xl lg:text-8xl font-black tracking-tighter italic mb-6 leading-none uppercase">
              {t('BORN TO', 'LİDERLİK İÇİN')} <br/><span className="text-brand-red">{t('LEAD.', 'DOĞDU.')}</span>
            </h1>
            <p className="text-gray-500 font-medium text-lg leading-relaxed">
              {t(
                "Founded on the principle of absolute culinary precision, OMASH isn't just a restaurant brand; it's an operational powerhouse redefining the premium burger segment. We combine scalable franchise architectures with an unforgettable flavor profile.",
                "Mutlak mutfak hassasiyeti prensibi üzerine kurulan OMASH, sadece bir restoran markası değil; premium burger segmentini yeniden tanımlayan bir operasyon gücüdür. Ölçeklenebilir franchise mimarisini, unutulmaz bir lezzet profiliyle birleştiriyoruz."
              )}
            </p>
          </div>
          <div className="md:w-1/2 grid grid-cols-2 gap-4">
            <div className="aspect-[4/5] rounded-3xl overflow-hidden shadow-xl mt-8">
              <img 
                src="https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&q=80&w=800" 
                alt="Corporate Restaurant" 
                className="w-full h-full object-cover grayscale hover:grayscale-0 transition-all duration-500" 
                referrerPolicy="no-referrer"
              />
            </div>
            <div className="aspect-[4/5] rounded-3xl overflow-hidden shadow-xl">
              <img 
                src="https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?auto=format&fit=crop&q=80&w=800" 
                alt="Smash Burger Quality" 
                className="w-full h-full object-cover" 
                referrerPolicy="no-referrer"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="bg-brand-red py-16 text-white mb-24">
        <div className="container mx-auto px-4 md:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 divide-x divide-white/20">
            {stats.map((stat, idx) => (
              <div key={idx} className="text-center px-4">
                <div className="text-4xl md:text-6xl font-black italic tracking-tighter mb-2">{stat.value}</div>
                <div className="text-sm font-bold uppercase tracking-widest text-white/80">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Vision, Mission, Values */}
      <section className="container mx-auto px-4 md:px-8 mb-24">
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-6xl font-black italic uppercase tracking-tighter text-brand-charcoal">
            {t('Corporate', 'Kurumsal')} <span className="text-brand-red">{t('Pillars', 'Temellerimiz')}</span>
          </h2>
        </div>
        <div className="grid md:grid-cols-3 gap-8">
          {pillars.map((pillar, idx) => (
            <div key={idx} className="bg-white p-10 rounded-[2rem] border border-gray-100 shadow-xl hover:-translate-y-2 transition-transform duration-300">
              <div className="bg-brand-charcoal text-white w-16 h-16 rounded-2xl flex items-center justify-center mb-8 shadow-lg shadow-black/10">
                {pillar.icon}
              </div>
              <h3 className="text-2xl font-black italic uppercase tracking-tight mb-4 text-brand-charcoal">{pillar.title}</h3>
              <p className="text-gray-500 font-medium leading-relaxed">
                {pillar.desc}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Existing DNA / Brand Identity Section */}
      <section className="bg-brand-charcoal py-24 text-white relative overflow-hidden mb-24 z-0">
        <div className="absolute top-0 right-0 w-1/3 h-full bg-brand-red/10 -skew-x-12 translate-x-1/2 -z-10" />
        <div className="container mx-auto px-4 md:px-8">
          <div className="grid md:grid-cols-2 gap-16 items-center">
            <motion.div
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
            >
              <span className="text-brand-red font-black uppercase tracking-widest text-sm mb-4 block">
                {t('Our Brand DNA', 'Marka DNA\'mız')}
              </span>
              <h2 className="text-4xl md:text-6xl lg:text-7xl font-black tracking-tighter italic mb-8 uppercase leading-none">
                VIBRANT. <span className="text-brand-red">BOLD.</span><br />UNCOMPROMISING.
              </h2>
              <p className="text-white/70 font-medium text-lg leading-relaxed mb-8">
                {t(
                  "OMASH isn't just a fast-food joint; it's a performance-driven brand ecosystem. We believe in the sheer power of the smash—the Maillard reaction that creates the ultimate flavor profile—delivered within a highly stylized retail environment.",
                  "OMASH sadece bir fast-food restoranı değil; performans odaklı bir marka ekosistemidir. Kusursuz lezzet profilini ortaya çıkaran Maillard reaksiyonunun, yani smash'in gücüne inanıyor ve bunu premium ve stilize bir perakende ortamında sunuyoruz."
                )}
              </p>
              <div className="grid grid-cols-2 gap-8">
                <div>
                  <h4 className="font-black text-brand-red text-2xl mb-2 italic">#E31837</h4>
                  <p className="text-xs font-bold text-white/50 uppercase tracking-widest">
                    {t('Vibrant Red Identity', 'Canlı Kırmızı Kimlik')}
                  </p>
                </div>
                <div>
                  <h4 className="font-black text-white text-2xl mb-2 italic">OUTFIT</h4>
                  <p className="text-xs font-bold text-white/50 uppercase tracking-widest">
                    {t('Modern Typography', 'Modern Tipografi')}
                  </p>
                </div>
              </div>
            </motion.div>
            <div className="relative">
              <div className="aspect-[4/3] rounded-[2rem] overflow-hidden shadow-2xl relative">
                <img 
                  src="https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&q=80&w=800" 
                  alt={t("Omash Signature Product", "Omash İmza Ürünü")} 
                  className="w-full h-full object-cover"
                  referrerPolicy="no-referrer"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      <FAQ />
    </div>
  );
};

const AllergensPage = () => {
  const { lang, t } = useTranslation();
  const [expandedAllergen, setExpandedAllergen] = useState<string | null>(null);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <div className="pt-32 pb-24 min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 md:px-8 max-w-6xl">
        {/* Corporate Header */}
        <div className="bg-white rounded-[2rem] p-12 shadow-sm border border-gray-100 mb-12">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-8">
            <div className="max-w-2xl">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-12 h-1 bg-brand-red rounded-full" />
                <span className="text-brand-red font-black uppercase tracking-[0.2em] text-xs">
                  {t('REGULATORY COMPLIANCE', 'MEVZUAT UYUMU')}
                </span>
              </div>
              <h1 className="text-4xl md:text-5xl font-black italic uppercase tracking-tight mb-6 leading-tight">
                {t('ALLERGEN', 'ALERJEN')} <span className="text-brand-red">{t('INFORMATION', 'BİLGİLENDİRMESİ')}</span>
              </h1>
              <p className="text-gray-500 font-medium text-lg leading-relaxed">
                {t(
                  'In accordance with the Turkish Food Codex Regulation on Food Labeling and Consumer Information, we provide detailed information regarding the 14 major allergens present in our products.',
                  'Türk Gıda Kodeksi Gıda Etiketleme ve Tüketicileri Bilgilendirme Yönetmeliği uyarınca, ürünlerimizde bulunan 14 temel alerjen hakkında detaylı bilgi sunmaktayız.'
                )}
              </p>
            </div>
            <div className="hidden lg:block">
              <div className="bg-brand-charcoal text-white p-8 rounded-3xl text-center">
                <div className="text-4xl font-black italic mb-1">14</div>
                <div className="text-[10px] font-black uppercase tracking-widest opacity-60">{t('MAJOR ALLERGENS', 'TEMEL ALERJEN')}</div>
              </div>
            </div>
          </div>
        </div>

        {/* Allergen Grid - Corporate Style */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-12">
          {Object.entries(ALLERGEN_ICONS).map(([key, data], index) => (
            <motion.div
              key={key}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.03 }}
              onClick={() => setExpandedAllergen(expandedAllergen === key ? null : key)}
              className={cn(
                "bg-white p-6 rounded-2xl border flex flex-col gap-4 cursor-pointer hover:shadow-md transition-all",
                expandedAllergen === key ? "border-brand-red shadow-lg" : "border-gray-100"
              )}
            >
              <div className="flex items-center gap-6">
                <div className="w-24 h-24 bg-gray-50 rounded-2xl flex items-center justify-center text-red-500 shrink-0">
                  {cloneElement(data.icon as React.ReactElement, { className: "w-12 h-12 text-brand-red" })}
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-[9px] font-black text-gray-300 uppercase tracking-widest">
                      REF: {String(index + 1).padStart(2, '0')}
                    </span>
                  </div>
                  <h3 className="text-lg font-black italic uppercase tracking-tight text-brand-charcoal">
                    {lang === 'en' ? data.nameEn : data.nameTr}
                  </h3>
                  <p className="text-[10px] font-bold text-gray-400 uppercase tracking-tight">
                    {lang === 'en' ? 'Food Allergen' : 'Gıda Alerjeni'}
                  </p>
                </div>
              </div>

              <AnimatePresence>
                {expandedAllergen === key && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    className="overflow-hidden"
                  >
                    <div className="pt-2 border-t border-gray-100 mt-2">
                      <p className="text-sm font-medium text-gray-500 leading-relaxed">
                        {lang === 'en' ? data.descEn : data.descTr}
                      </p>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          ))}
        </div>

        {/* Legal Footer Section */}
        <div className="grid md:grid-cols-3 gap-8">
          <div className="md:col-span-2 bg-white rounded-[2rem] p-10 border border-gray-100">
            <h3 className="text-xl font-black italic uppercase mb-6 flex items-center gap-3">
              <div className="w-2 h-6 bg-brand-red rounded-full" />
              {t('CONSUMER SAFETY NOTICE', 'TÜKETİCİ GÜVENLİĞİ BİLDİRİMİ')}
            </h3>
            <div className="space-y-4 text-gray-500 font-medium text-sm leading-relaxed">
              <p>
                {t(
                  'While we take extreme care to prevent cross-contamination, our products are prepared in a common kitchen environment. We cannot guarantee that any menu item is 100% free of allergens.',
                  'Çapraz bulaşmayı önlemek için azami özen göstermemize rağmen, ürünlerimiz ortak bir mutfak ortamında hazırlanmaktadır. Herhangi bir menü öğesinin %100 alerjen içermediğini garanti edemeyiz.'
                )}
              </p>
              <p className="p-4 bg-brand-red/5 rounded-xl border border-brand-red/10 text-brand-red font-bold">
                {t(
                  'IMPORTANT: Please inform our service staff about your specific allergies or dietary requirements before placing your order.',
                  'ÖNEMLİ: Lütfen siparişinizi vermeden önce özel alerjileriniz veya diyet gereksinimleriniz hakkında servis personelimizi bilgilendirin.'
                )}
              </p>
            </div>
          </div>
          
          <div className="bg-brand-charcoal rounded-[2rem] p-10 text-white flex flex-col justify-between">
            <div>
              <h4 className="text-xs font-black uppercase tracking-[0.2em] opacity-60 mb-4">{t('RESOURCES', 'KAYNAKLAR')}</h4>
              <p className="text-sm font-medium opacity-80 mb-8">
                {t('Access official documentation from the Ministry of Agriculture and Forestry.', 'Tarım ve Orman Bakanlığı\'nın resmi belgelerine erişin.')}
              </p>
            </div>
            <a 
              href="https://www.tarimorman.gov.tr/Konu/2023/Toplu_Tuketim_Yerlerinde_Alerjen_Bildirimi" 
              target="_blank" 
              rel="noopener noreferrer"
              className="w-full bg-white text-brand-charcoal py-4 rounded-xl font-black uppercase tracking-widest text-xs text-center hover:bg-brand-red hover:text-white transition-all"
            >
              {t('OFFICIAL PORTAL', 'RESMİ PORTAL')}
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

const FranchisingPage = () => {
  const { lang, t } = useTranslation();

  const advantages = [
    {
      icon: <Star className="w-8 h-8" />,
      title: t('Proven Business Model', 'Kanıtlanmış İş Modeli'),
      desc: t(
        'OMASH offers a highly profitable and scalable business model backed by extensive operational experience.',
        'OMASH, kapsamlı operasyonel deneyimle desteklenen, oldukça kârlı ve ölçeklenebilir bir iş modeli sunar.'
      )
    },
    {
      icon: <ShieldCheck className="w-8 h-8" />,
      title: t('Supply Chain Strength', 'Güçlü Tedarik Ağı'),
      desc: t(
        'Benefit from our centralized, reliable, and cost-effective supply chain infrastructure.',
        'Merkezi, güvenilir ve uygun maliyetli tedarik zinciri altyapımızdan yararlanın.'
      )
    },
    {
      icon: <Target className="w-8 h-8" />,
      title: t('Marketing & Training', 'Pazarlama ve Eğitim'),
      desc: t(
        'Comprehensive marketing support and rigorous staff training programs ensure you start strong.',
        'Kapsamlı pazarlama desteği ve zorlu personel eğitim programları, güçlü bir başlangıç yapmanızı sağlar.'
      )
    },
    {
      icon: <TrendingUp className="w-8 h-8" />,
      title: t('Continuous Support', 'Sürekli Operasyonel Destek'),
      desc: t(
        'Our expert field teams provide ongoing guidance to maximize your restaurant\'s performance.',
        'Uzman saha ekiplerimiz, restoranınızın performansını en üst düzeye çıkarmak için sürekli rehberlik sağlar.'
      )
    }
  ];

  const steps = [
    { num: '01', title: t('Application', 'Başvuru'), desc: t('Fill out the online application form.', 'Online başvuru formunu doldurun.') },
    { num: '02', title: t('Evaluation', 'Değerlendirme'), desc: t('Our team evaluates your profile and location.', 'Ekibimiz profilinizi ve lokasyonunuzu değerlendirir.') },
    { num: '03', title: t('Interview', 'Görüşme'), desc: t('Face-to-face meeting to discuss the details.', 'Detayları konuşmak için yüz yüze görüşme.') },
    { num: '04', title: t('Agreement & Training', 'Sözleşme & Eğitim'), desc: t('Sign the agreement and begin the management training.', 'Sözleşmeyi imzalayıp yönetim eğitimine başlayın.') },
    { num: '05', title: t('Grand Opening', 'Büyük Açılış'), desc: t('Launch your OMASH restaurant and start smashing.', 'OMASH restoranınızı açın ve smashlemeye başlayın.') }
  ];

  return (
    <div className="pt-32 pb-24 bg-brand-white min-h-screen">
      <section className="container mx-auto px-4 md:px-8 mb-24">
        <div className="flex flex-col md:flex-row gap-12 items-center">
          <div className="md:w-1/2">
            <h1 className="text-5xl md:text-8xl font-black italic tracking-tighter uppercase mb-6 leading-none">
              FRANCHISE <br/><span className="text-brand-red">{t('OPPORTUNITY', 'YATIRIMCILIĞI')}</span>
            </h1>
            <p className="text-gray-500 font-medium text-lg leading-relaxed mb-8">
              {t(
                'Become a part of the fastest-growing modern smash burger chain. By joining the OMASH family, you unlock advanced operational know-how, superior supply chain logistics, and a vibrant brand identity.',
                'En hızlı büyüyen modern smash burger zincirinin bir parçası olun. OMASH ailesine katılarak, gelişmiş operasyonel know-how, üstün tedarik zinciri lojistiği ve güçlü bir marka kimliği avantajlarından yararlanırsınız.'
              )}
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
               <button 
                 onClick={() => {
                   document.getElementById('franchise-form')?.scrollIntoView({ behavior: 'smooth' });
                 }}
                 className="bg-brand-red text-white px-8 py-4 rounded-xl font-black italic uppercase tracking-widest hover:bg-brand-charcoal transition-all text-center"
               >
                 {t('APPLY NOW', 'HEMEN BAŞVUR')}
               </button>
            </div>
          </div>
          <div className="md:w-1/2">
            <div className="aspect-[4/3] rounded-[2.5rem] overflow-hidden shadow-2xl relative group">
              <img 
                src="https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&q=80&w=1200" 
                alt="Restaurant Business" 
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                referrerPolicy="no-referrer"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-brand-charcoal/80 to-transparent flex items-end p-8">
                 <p className="text-white font-black italic text-2xl uppercase tracking-tighter">
                   {t('Build Your Empire', 'Kendi İmparatorluğunu Kur')}
                 </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="bg-brand-charcoal py-24 text-white relative overflow-hidden mb-24">
        <div className="absolute top-0 right-0 w-1/3 h-full bg-brand-red/10 -skew-x-12 translate-x-1/2" />
        <div className="container mx-auto px-4 md:px-8 relative z-10">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-6xl font-black italic uppercase tracking-tighter">
              {t('Franchise', 'Franchise')} <span className="text-brand-red">{t('Advantages', 'Avantajları')}</span>
            </h2>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {advantages.map((adv, i) => (
              <div key={`franchise-adv-${i}`} className="bg-white/5 border border-white/10 p-8 rounded-3xl hover:bg-white/10 transition-colors">
                <div className="bg-brand-red text-white w-16 h-16 rounded-2xl flex items-center justify-center mb-6 shadow-lg">
                  {adv.icon}
                </div>
                <h3 className="text-2xl font-black italic mb-4">{adv.title}</h3>
                <p className="text-white/60 font-medium leading-relaxed">
                  {adv.desc}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="container mx-auto px-4 md:px-8 mb-24">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-4xl md:text-6xl font-black italic uppercase tracking-tighter text-center mb-16">
            {t('Investment', 'Yatırım')} <span className="text-brand-red">{t('Process', 'Süreci')}</span>
          </h2>
          <div className="space-y-6">
            {steps.map((step, i) => (
              <div key={`franchise-step-${i}`} className="flex flex-col md:flex-row gap-6 items-start md:items-center bg-white p-8 rounded-3xl border border-gray-100 shadow-sm hover:shadow-xl transition-all">
                 <div className="text-5xl font-black text-brand-red italic opacity-20">
                   {step.num}
                 </div>
                 <div>
                   <h3 className="text-2xl font-black italic mb-2 uppercase">{step.title}</h3>
                   <p className="text-gray-500 font-medium">{step.desc}</p>
                 </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="franchise-form" className="container mx-auto px-4 md:px-8">
        <div className="max-w-3xl mx-auto bg-white p-8 md:p-12 rounded-[2.5rem] border border-gray-100 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-brand-red/10 rounded-bl-full -z-0" />
          
          <div className="relative z-10">
            <h2 className="text-3xl font-black italic tracking-tighter uppercase mb-2">
              {t('Franchising Application', 'Franchising Başvuru Formu')}
            </h2>
            <p className="text-gray-500 text-sm font-medium mb-8">
              {t('Please fill out the form entirely. Our investment team will contact you.', 'Lütfen formu eksiksiz doldurun. Yatırım ekibimiz sizinle iletişime geçecektir.')}
            </p>

            <form className="space-y-6" onSubmit={(e) => { e.preventDefault(); alert(t('Application submitted successfully! Our team will contact you shorty.', 'Başvurunuz başarıyla alındı! Ekibimiz kısa süre içinde sizinle iletişime geçecektir.')); }}>
              <div className="grid md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-xs font-black uppercase text-gray-400 tracking-widest mb-2">{t('Full Name', 'Ad Soyad')} *</label>
                  <input type="text" required className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 font-medium focus:ring-2 focus:ring-brand-red outline-none transition-all" />
                </div>
                <div>
                  <label className="block text-xs font-black uppercase text-gray-400 tracking-widest mb-2">{t('Phone Number', 'Telefon Numarası')} *</label>
                  <input type="tel" required className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 font-medium focus:ring-2 focus:ring-brand-red outline-none transition-all" />
                </div>
              </div>
              
              <div className="grid md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-xs font-black uppercase text-gray-400 tracking-widest mb-2">{t('Email Address', 'E-Posta Adresi')} *</label>
                  <input type="email" required className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 font-medium focus:ring-2 focus:ring-brand-red outline-none transition-all" />
                </div>
                <div>
                  <label className="block text-xs font-black uppercase text-gray-400 tracking-widest mb-2">{t('Target Location / City', 'Hedeflenen Lokasyon / Şehir')} *</label>
                  <input type="text" required className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 font-medium focus:ring-2 focus:ring-brand-red outline-none transition-all" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-black uppercase text-gray-400 tracking-widest mb-2">{t('Experience / Background', 'Deneyim / Özgeçmiş Özeti')}</label>
                <textarea rows={4} className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 font-medium focus:ring-2 focus:ring-brand-red outline-none transition-all resize-none"></textarea>
              </div>

              <div className="flex items-start gap-4 p-4 bg-gray-50 rounded-xl border border-gray-100">
                 <input type="checkbox" required id="kvkk-consent" className="mt-1 w-5 h-5 accent-brand-red" />
                 <label htmlFor="kvkk-consent" className="text-xs text-gray-500 font-medium leading-relaxed">
                   {t('I have read and accept the processing of my personal data in accordance with the', 'Kişisel verilerimin işlenmesini kabul ediyor ve onaylıyorum. İlgili metinler:')} 
                   <span className="text-brand-red font-bold hover:underline cursor-pointer"> {t('KVKK Terms', 'KVKK Aydınlatma Metni')}</span>.
                 </label>
              </div>

              <button type="submit" className="w-full bg-brand-charcoal text-white font-black italic uppercase tracking-widest py-4 rounded-xl hover:bg-brand-red transition-all shadow-xl shadow-brand-charcoal/20 active:scale-[0.98]">
                {t('SUBMIT APPLICATION', 'BAŞVURUYU GÖNDER')}
              </button>
            </form>
          </div>
        </div>
      </section>

    </div>
  );
};

const CorporatePage = ({ activeTab, setActiveTab }: { activeTab: string, setActiveTab: (t: string) => void }) => {
  const { lang } = useTranslation();
  
  const allowedKeys = ['kvkk', 'privacy', 'terms', 'society', 'disclosure', 'halal', 'hygiene', 'satisfaction', 'contactless'];
  const currentKey = allowedKeys.includes(activeTab) ? activeTab : 'kvkk';
  const content = INFO_CONTENT[currentKey];

  return (
    <div className="pt-32 pb-24 bg-brand-white min-h-screen">
      <div className="container mx-auto px-4 md:px-8">
        <div className="flex flex-col lg:flex-row gap-12">
          {/* Sidebar */}
          <div className="lg:w-1/4">
            <div className="bg-white rounded-[2rem] p-6 border border-gray-100 shadow-xl lg:sticky lg:top-32">
              <h3 className="text-xl font-black italic uppercase tracking-tighter mb-6 pb-4 border-b border-gray-100">
                {lang === 'en' ? 'CORPORATE' : 'KURUMSAL'}
              </h3>
              <ul className="space-y-2">
                {allowedKeys.map(key => {
                  const isActive = key === currentKey;
                  const title = lang === 'en' ? INFO_CONTENT[key].titleEn : INFO_CONTENT[key].titleTr;
                  return (
                    <li key={key}>
                      <button 
                        onClick={() => {
                          setActiveTab(key);
                          window.scrollTo({ top: 0, behavior: 'smooth' });
                        }}
                        className={cn(
                          "w-full text-left px-4 py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-between",
                          isActive 
                            ? "bg-brand-red text-white" 
                            : "text-gray-500 hover:bg-gray-50 hover:text-brand-charcoal"
                        )}
                      >
                        {title}
                        {isActive && <ChevronRight className="w-4 h-4" />}
                      </button>
                    </li>
                  );
                })}
              </ul>
            </div>
          </div>

          {/* Content */}
          <div className="lg:w-3/4">
            <div className="bg-white rounded-[2.5rem] p-8 md:p-12 border border-gray-100 shadow-xl">
               <div className="mb-12 border-b border-gray-100 pb-8">
                 <h1 className="text-3xl md:text-5xl font-black italic uppercase tracking-tighter text-brand-charcoal mb-4">
                   {lang === 'en' ? content.titleEn : content.titleTr}
                 </h1>
                 <div className="w-24 h-2 bg-brand-red/20 rounded-full overflow-hidden">
                   <div className="w-12 h-full bg-brand-red rounded-full" />
                 </div>
               </div>

               <div className="space-y-8">
                 {(lang === 'en' ? content.contentEn : content.contentTr).map((pStr, i) => (
                   <p key={`corp-p-${i}`} className="text-gray-600 font-medium leading-relaxed text-lg">
                     {pStr}
                   </p>
                 ))}
               </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const CheckoutPage = ({ setCurrentPage, isFirstOrder, discountAmount, finalPrice }: any) => {
  const { cart, clearCart, totalPrice } = useCart();
  const { user } = useAuth();
  const { t, formatPrice } = useTranslation();
  const [isProcessing, setIsProcessing] = useState(false);
  
  const [formData, setFormData] = useState({
    fullName: '',
    phone: '',
    address: '',
    paymentMethod: 'credit_card'
  });

  const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || cart.length === 0) return;

    setIsProcessing(true);
    try {
      const pointsEarned = Math.floor(finalPrice); 
      const orderData = {
        userId: user.uid,
        deliveryInfo: { ...formData },
        items: cart.map(item => ({
          id: item.id,
          name: item.name,
          price: item.price,
          quantity: item.quantity,
          level: item.level || null,
          extras: item.selectedExtras.map(e => ({ id: e.id, name: e.name, price: e.price }))
        })),
        totalPrice: finalPrice,
        discountApplied: discountAmount,
        pointsEarned: pointsEarned,
        createdAt: serverTimestamp(),
        status: 'pending'
      };

      await addDoc(collection(db, 'orders'), orderData);
      
      const userDocRef = doc(db, 'users', user.uid);
      await updateDoc(userDocRef, {
        points: increment(pointsEarned)
      });

      clearCart();
      alert(t('Order placed successfully! You earned ' + pointsEarned + ' BIT.', 'Siparişiniz başarıyla alındı! ' + pointsEarned + ' BIT kazandınız.'));
      setCurrentPage('profile');
    } catch (error) {
      console.error("Checkout Error:", error);
      alert(t('Something went wrong. Please try again.', 'Bir şeyler yanlış gitti. Lütfen tekrar deneyin.'));
    } finally {
      setIsProcessing(false);
    }
  };

  if (!user || cart.length === 0) {
    return (
      <div className="pt-48 pb-24 text-center">
        <h2 className="text-2xl font-black italic mb-4">{t('YOUR CART IS EMPTY', 'SEPETİNİZ BOŞ')}</h2>
        <button onClick={() => setCurrentPage('full-menu')} className="text-brand-red font-bold hover:underline">
          {t('GO TO MENU', 'MENÜYE GİT')}
        </button>
      </div>
    );
  }

  return (
    <div className="pt-32 pb-24 min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 md:px-8 max-w-6xl">
        <h1 className="text-4xl md:text-5xl font-black italic uppercase tracking-tighter mb-8">
          {t('CHECKOUT', 'ÖDEME SAYFASI')}
        </h1>
        
        <div className="grid lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white p-8 rounded-3xl border border-gray-100 shadow-sm">
              <h2 className="text-xl font-black italic uppercase mb-6 flex items-center gap-3">
                <div className="w-2 h-6 bg-brand-red rounded-full" />
                {t('DELIVERY DETAILS', 'TESLİMAT BİLGİLERİ')}
              </h2>
              
              <form id="checkout-form" onSubmit={handleCheckout} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('FULL NAME', 'AD SOYAD')} *</label>
                    <input 
                      required
                      type="text" 
                      value={formData.fullName}
                      onChange={e => setFormData({...formData, fullName: e.target.value})}
                      className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 font-bold text-sm focus:outline-none focus:border-brand-red"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('PHONE', 'TELEFON')} *</label>
                    <input 
                      required
                      type="tel" 
                      value={formData.phone}
                      onChange={e => setFormData({...formData, phone: e.target.value})}
                      className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 font-bold text-sm focus:outline-none focus:border-brand-red"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('FULL ADDRESS', 'AÇIK ADRES')} *</label>
                  <textarea 
                    required
                    value={formData.address}
                    onChange={e => setFormData({...formData, address: e.target.value})}
                    className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 font-bold text-sm focus:outline-none focus:border-brand-red h-24 resize-none"
                    placeholder={t('Enter your full delivery address', 'Açık adresinizi giriniz')}
                  />
                </div>
              </form>
            </div>
            
            <div className="bg-white p-8 rounded-3xl border border-gray-100 shadow-sm">
              <h2 className="text-xl font-black italic uppercase mb-6 flex items-center gap-3">
                <div className="w-2 h-6 bg-brand-red rounded-full" />
                {t('PAYMENT METHOD', 'ÖDEME YÖNTEMİ')}
              </h2>
              
              <div className="space-y-3">
                <label className={`flex items-center gap-4 p-4 rounded-xl border-2 cursor-pointer transition-all ${formData.paymentMethod === 'credit_card' ? 'border-brand-red bg-brand-red/5' : 'border-gray-100 hover:border-brand-red/30'}`}>
                  <input 
                    type="radio" 
                    name="paymentMethod" 
                    value="credit_card" 
                    checked={formData.paymentMethod === 'credit_card'}
                    onChange={e => setFormData({...formData, paymentMethod: e.target.value})}
                    className="w-5 h-5 accent-brand-red"
                  />
                  <div className="flex-1">
                    <p className="font-bold text-sm">{t('Credit Card (Iyzico / Stripe)', 'Kredi Kartı (Iyzico / Stripe)')}</p>
                    <p className="text-xs text-gray-500 font-medium">{t('Pay securely online', 'Güvenle online ödeyin')}</p>
                  </div>
                </label>
                <label className={`flex items-center gap-4 p-4 rounded-xl border-2 cursor-pointer transition-all ${formData.paymentMethod === 'cash_on_delivery' ? 'border-brand-red bg-brand-red/5' : 'border-gray-100 hover:border-brand-red/30'}`}>
                  <input 
                    type="radio" 
                    name="paymentMethod" 
                    value="cash_on_delivery" 
                    checked={formData.paymentMethod === 'cash_on_delivery'}
                    onChange={e => setFormData({...formData, paymentMethod: e.target.value})}
                    className="w-5 h-5 accent-brand-red"
                  />
                  <div className="flex-1">
                    <p className="font-bold text-sm">{t('Cash / Card on Delivery', 'Kapıda Nakit / Kart')}</p>
                    <p className="text-xs text-gray-500 font-medium">{t('Pay when you receive your order', 'Siparişinizi teslim alırken ödeyin')}</p>
                  </div>
                </label>
              </div>
            </div>
          </div>
          
          <div className="lg:col-span-1">
            <div className="bg-white p-8 rounded-3xl border border-gray-100 shadow-sm sticky top-32">
              <h2 className="text-xl font-black italic uppercase mb-6 flex items-center gap-3">
                <div className="w-2 h-6 bg-brand-charcoal rounded-full" />
                {t('ORDER SUMMARY', 'SİPARİŞ ÖZETİ')}
              </h2>
              
              <div className="space-y-4 mb-6 max-h-64 overflow-y-auto pr-2">
                {cart.map((item) => (
                  <div key={item.cartId} className="flex justify-between items-center text-sm font-bold border-b border-gray-100 pb-2">
                    <div className="flex items-center gap-2">
                      <span className="text-brand-red">{item.quantity}x</span>
                      <span>{item.name}</span>
                    </div>
                    <span>{formatPrice((item.price + item.selectedExtras.reduce((s, e) => s + e.price, 0)) * item.quantity)}</span>
                  </div>
                ))}
              </div>
              
              <div className="space-y-2 border-t border-gray-100 py-4">
                <div className="flex justify-between text-sm font-bold text-gray-500">
                  <span>{t('Subtotal', 'Ara Toplam')}</span>
                  <span>{formatPrice(totalPrice)}</span>
                </div>
                {isFirstOrder && (
                  <div className="flex justify-between text-sm font-black text-brand-red uppercase italic">
                    <span>{t('First Order Discount', 'İlk Sipariş İndirimi')}</span>
                    <span>-{formatPrice(discountAmount)}</span>
                  </div>
                )}
              </div>
              
              <div className="flex justify-between font-black text-2xl border-t border-gray-200 pt-4 mb-6">
                <span>{t('TOTAL', 'TOPLAM')}</span>
                <span>{formatPrice(finalPrice)}</span>
              </div>
              
              <button 
                form="checkout-form"
                type="submit"
                disabled={isProcessing}
                className="w-full bg-brand-red text-white py-5 rounded-2xl font-black text-lg flex items-center justify-center gap-2 hover:bg-brand-charcoal transition-all shadow-xl shadow-brand-red/20 active:scale-95 disabled:opacity-50"
              >
                {isProcessing ? <Loader2 className="w-6 h-6 animate-spin" /> : t('CONFIRM ORDER', 'SİPARİŞİ ONAYLA')}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const ProfilePage = () => {
  const { profile, updateProfile, user, logout } = useAuth();
  const { lang, t, formatPrice } = useTranslation();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    displayName: profile?.displayName || '',
    phoneNumber: profile?.phoneNumber || '',
    address: profile?.address || '',
    profilePictureUrl: profile?.profilePictureUrl || '',
  });
  const [orders, setOrders] = useState<Order[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (profile) {
      setFormData({
        displayName: profile.displayName || '',
        phoneNumber: profile.phoneNumber || '',
        address: profile.address || '',
        profilePictureUrl: profile.profilePictureUrl || '',
      });
    }
  }, [profile]);

  useEffect(() => {
    if (user) {
      const ordersRef = collection(db, 'orders');
      const q = query(
        ordersRef, 
        where('userId', '==', user.uid), 
        orderBy('createdAt', 'desc')
      );
      
      const unsubscribe = onSnapshot(q, (snapshot) => {
        const ordersData = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as Order[];
        setOrders(ordersData);
        setLoadingOrders(false);
      }, (error) => {
        console.error("Orders Snapshot Error:", error);
        setLoadingOrders(false);
      });

      return () => unsubscribe();
    }
  }, [user]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateProfile(formData);
      setIsEditing(false);
    } catch (error) {
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  if (!user) return (
    <div className="pt-48 pb-24 text-center">
      <h2 className="text-2xl font-black italic mb-4">{t('PLEASE LOGIN TO VIEW PROFILE', 'PROFİLİ GÖRMEK İÇİN LÜTFEN GİRİŞ YAPIN')}</h2>
    </div>
  );

  return (
    <div className="pt-32 pb-24 min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 md:px-8 max-w-6xl">
        <div className="grid lg:grid-cols-3 gap-8">
          
          {/* Sidebar: Profile Info */}
          <div className="lg:col-span-1 space-y-8">
            <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100">
              <div className="flex flex-col items-center text-center mb-8">
                <div className="w-24 h-24 rounded-full bg-brand-red/10 flex items-center justify-center border-4 border-white shadow-lg overflow-hidden mb-4">
                  {profile?.profilePictureUrl || user.photoURL ? (
                    <img src={profile?.profilePictureUrl || user.photoURL || ''} alt="Avatar" className="w-full h-full object-cover" />
                  ) : (
                    <User className="w-12 h-12 text-brand-red" />
                  )}
                </div>
                <h2 className="text-2xl font-black italic">{profile?.displayName || t('User', 'Kullanıcı')}</h2>
                <p className="text-gray-400 font-bold text-sm">{user.email}</p>
                
                <div className="mt-6 flex items-center gap-3 bg-brand-red/5 px-6 py-3 rounded-2xl border border-brand-red/10">
                  <Award className="w-6 h-6 text-brand-red" />
                  <div className="text-left">
                    <p className="text-[10px] font-black text-brand-red uppercase tracking-widest leading-none mb-1">{t('BIT POINTS', 'BIT PUANLARI')}</p>
                    <p className="text-xl font-black italic leading-none">{profile?.points || 0} BIT</p>
                  </div>
                </div>
              </div>

              {isEditing ? (
                <form onSubmit={handleSave} className="space-y-4">
                  <div>
                    <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('AVATAR URL', 'AVATAR URL')}</label>
                    <input 
                      type="url" 
                      value={formData.profilePictureUrl}
                      onChange={e => setFormData({...formData, profilePictureUrl: e.target.value})}
                      className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 font-bold text-sm focus:outline-none focus:border-brand-red"
                      placeholder="https://..."
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('FULL NAME', 'AD SOYAD')}</label>
                    <input 
                      type="text" 
                      value={formData.displayName}
                      onChange={e => setFormData({...formData, displayName: e.target.value})}
                      className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 font-bold text-sm focus:outline-none focus:border-brand-red"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('PHONE', 'TELEFON')}</label>
                    <input 
                      type="tel" 
                      value={formData.phoneNumber}
                      onChange={e => setFormData({...formData, phoneNumber: e.target.value})}
                      className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 font-bold text-sm focus:outline-none focus:border-brand-red"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">{t('ADDRESS', 'ADRES')}</label>
                    <textarea 
                      value={formData.address}
                      onChange={e => setFormData({...formData, address: e.target.value})}
                      className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 font-bold text-sm focus:outline-none focus:border-brand-red h-24 resize-none"
                    />
                  </div>
                  <div className="flex gap-2 pt-2">
                    <button 
                      type="submit"
                      disabled={saving}
                      className="flex-1 bg-brand-red text-white py-3 rounded-xl font-black text-sm flex items-center justify-center gap-2 hover:bg-brand-charcoal transition-all disabled:opacity-50"
                    >
                      {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                      {t('SAVE', 'KAYDET')}
                    </button>
                    <button 
                      type="button"
                      onClick={() => setIsEditing(false)}
                      className="flex-1 bg-gray-100 text-gray-500 py-3 rounded-xl font-black text-sm hover:bg-gray-200 transition-all"
                    >
                      {t('CANCEL', 'İPTAL')}
                    </button>
                  </div>
                </form>
              ) : (
                <div className="space-y-6">
                  <div className="space-y-4">
                    <div className="flex items-start gap-4">
                      <div className="bg-gray-50 p-2.5 rounded-lg">
                        <Phone className="w-4 h-4 text-gray-400" />
                      </div>
                      <div>
                        <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-0.5">{t('PHONE', 'TELEFON')}</p>
                        <p className="font-bold text-sm">{profile?.phoneNumber || t('Not set', 'Ayarlanmadı')}</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-4">
                      <div className="bg-gray-50 p-2.5 rounded-lg">
                        <MapPin className="w-4 h-4 text-gray-400" />
                      </div>
                      <div>
                        <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-0.5">{t('ADDRESS', 'ADRES')}</p>
                        <p className="font-bold text-sm leading-relaxed">{profile?.address || t('Not set', 'Ayarlanmadı')}</p>
                      </div>
                    </div>
                  </div>
                  <div className="space-y-3">
                    <button 
                      onClick={() => setIsEditing(true)}
                      className="w-full border-2 border-brand-charcoal py-3 rounded-xl font-black text-sm hover:bg-brand-charcoal hover:text-white transition-all"
                    >
                      {t('EDIT PROFILE', 'PROFİLİ DÜZENLE')}
                    </button>
                    <button 
                      onClick={logout}
                      className="w-full flex items-center justify-center gap-2 py-3 rounded-xl font-black text-sm text-brand-red hover:bg-brand-red/5 transition-all"
                    >
                      <LogOut className="w-4 h-4" />
                      {t('LOGOUT', 'ÇIKIŞ YAP')}
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Main Content: Order History */}
          <div className="lg:col-span-2 space-y-8">
            <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 min-h-[600px]">
              <div className="flex items-center gap-4 mb-8">
                <div className="bg-brand-charcoal text-white p-3 rounded-2xl">
                  <History className="w-6 h-6" />
                </div>
                <h2 className="text-3xl font-black italic uppercase tracking-tight">{t('ORDER HISTORY', 'SİPARİŞ GEÇMİŞİ')}</h2>
              </div>

              {loadingOrders ? (
                <div className="flex flex-col items-center justify-center py-24 text-gray-300">
                  <Loader2 className="w-12 h-12 animate-spin mb-4" />
                  <p className="font-bold">{t('Loading orders...', 'Siparişler yükleniyor...')}</p>
                </div>
              ) : orders.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-24 text-center">
                  <div className="bg-gray-50 p-8 rounded-full mb-6">
                    <ShoppingBag className="w-12 h-12 text-gray-200" />
                  </div>
                  <h3 className="text-xl font-bold mb-2">{t('No orders yet', 'Henüz sipariş yok')}</h3>
                  <p className="text-gray-500 font-medium max-w-xs">{t('Your performance history is empty. Time to upgrade your meal!', 'Performans geçmişiniz boş. Öğününüzü yükseltme zamanı!')}</p>
                </div>
              ) : (
                <div className="space-y-6">
                  {orders.map((order) => (
                    <div key={order.id} className="bg-gray-50 rounded-2xl p-6 border border-gray-100 hover:border-brand-red/20 transition-all group">
                      <div className="flex flex-wrap justify-between items-start gap-4 mb-4">
                        <div>
                          <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">
                            {order.createdAt?.toDate ? order.createdAt.toDate().toLocaleDateString(lang === 'tr' ? 'tr-TR' : 'en-US', { day: 'numeric', month: 'long', year: 'numeric' }) : ''}
                          </p>
                          <h4 className="font-black italic text-lg">ORDER #{order.id.slice(-6).toUpperCase()}</h4>
                        </div>
                        <div className="text-right">
                          <p className="text-xl font-black text-brand-red italic">{formatPrice(order.totalPrice)}</p>
                          <span className={cn(
                            "inline-block px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest mt-1",
                            order.status === 'completed' ? "bg-green-100 text-green-600" : "bg-orange-100 text-orange-600"
                          )}>
                            {order.status === 'completed' ? t('COMPLETED', 'TAMAMLANDI') : t('PENDING', 'BEKLİYOR')}
                          </span>
                        </div>
                      </div>
                      
                      <div className="flex flex-wrap gap-2 mb-4">
                        {order.items.map((item, idx) => (
                          <div key={idx} className="bg-white px-3 py-1.5 rounded-lg border border-gray-100 text-xs font-bold flex flex-col gap-1">
                            <div className="flex items-center gap-2">
                              <span className="text-brand-red">{item.quantity}x</span>
                              {item.name}
                            </div>
                            {item.extras && item.extras.length > 0 && (
                              <div className="text-[9px] text-gray-400 pl-6">
                                +{item.extras.map(e => e.name).join(', ')}
                              </div>
                            )}
                          </div>
                        ))}
                      </div>

                      <div className="flex items-center gap-2 text-[10px] font-black text-brand-red uppercase tracking-widest pt-4 border-t border-t-gray-200/50">
                        <Award className="w-3.5 h-3.5" />
                        {t('EARNED', 'KAZANILAN')}: {order.pointsEarned} BIT
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const WelcomeModal = ({ isOpen, onClose }: { isOpen: boolean, onClose: () => void }) => {
  const { t } = useTranslation();
  const { loginWithGoogle } = useAuth();
  const [view, setView] = useState<'welcome' | 'login' | 'signup'>('welcome');
  const [loginMethod, setLoginMethod] = useState<'phone' | 'email'>('phone');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (view === 'signup') {
        await createUserWithEmailAndPassword(auth, email, password);
      } else {
        await signInWithEmailAndPassword(auth, email, password);
      }
      onClose();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const resetModal = () => {
    setView('welcome');
    setError('');
    setEmail('');
    setPassword('');
    setPhone('');
  };

  useEffect(() => {
    if (!isOpen) {
      setTimeout(resetModal, 300);
    }
  }, [isOpen]);

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-brand-charcoal/60 backdrop-blur-sm z-[80]"
          />
          <motion.div 
            initial={{ opacity: 0, scale: 0.9, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.9, y: 20 }}
            className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-lg bg-white z-[90] shadow-2xl rounded-[2.5rem] overflow-hidden max-h-[95vh] flex flex-col"
          >
            <div className="p-8 md:p-12 overflow-y-auto custom-scrollbar relative">
              {view !== 'welcome' && (
                <button 
                  onClick={() => setView('welcome')}
                  className="absolute left-8 top-10 p-2 hover:bg-gray-100 rounded-full transition-colors z-10"
                >
                  <ChevronLeft className="w-6 h-6" />
                </button>
              )}
              
              <button 
                onClick={onClose} 
                className="absolute right-8 top-10 p-2 hover:bg-gray-100 rounded-full transition-colors z-10"
              >
                <X className="w-6 h-6" />
              </button>

              {view === 'welcome' && (
                <div className="flex flex-col items-center text-center">
                  <Logo className="text-brand-red mb-6 scale-125" />
                  <h2 className="text-3xl font-black italic mb-8 uppercase tracking-tighter">
                    {t('WELCOME TO THE SYSTEM', 'SİSTEME HOŞ GELDİNİZ')}
                  </h2>
                  
                  <div className="w-full aspect-[16/9] rounded-3xl overflow-hidden mb-8 relative group">
                    <img 
                      src="https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&q=80&w=800" 
                      alt="Promo" 
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-brand-charcoal/80 to-transparent flex flex-col justify-end p-6 text-left">
                      <p className="text-brand-red font-black text-xs uppercase tracking-widest mb-1">{t('FIRST ORDER SPECIAL', 'İLK SİPARİŞE ÖZEL')}</p>
                      <h3 className="text-white text-xl font-black italic leading-tight">
                        {t('FREE MEDIUM PIZZA ON YOUR FIRST DELIVERY ORDER!', 'EVE SERVİS İLK SİPARİŞİNE ORTA BOY PİZZA HEDİYE!')}
                      </h3>
                    </div>
                  </div>

                  <div className="w-full space-y-4">
                    <button 
                      onClick={() => setView('login')}
                      className="w-full bg-brand-red text-white py-5 rounded-2xl font-black text-xl hover:bg-brand-charcoal transition-all shadow-xl shadow-brand-red/20 active:scale-[0.98]"
                    >
                      {t('SIGN IN', 'GİRİŞ YAP')}
                    </button>
                    <button 
                      onClick={() => setView('signup')}
                      className="w-full bg-white text-brand-charcoal border-2 border-gray-100 py-5 rounded-2xl font-black text-xl hover:border-brand-red transition-all active:scale-[0.98]"
                    >
                      {t('CREATE ACCOUNT', 'ÜYE OL')}
                    </button>

                    <div className="relative py-4">
                      <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-gray-100"></div>
                      </div>
                      <div className="relative flex justify-center text-[10px] uppercase font-black tracking-widest">
                        <span className="bg-white px-4 text-gray-400">{t('OR', 'VEYA')}</span>
                      </div>
                    </div>

                    <button 
                      onClick={() => { loginWithGoogle(); onClose(); }}
                      className="w-full bg-white border-2 border-gray-100 text-brand-charcoal py-5 rounded-2xl font-black text-xl flex items-center justify-center gap-3 hover:border-brand-red transition-all active:scale-[0.98]"
                    >
                      <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="Google" className="w-6 h-6" />
                      {t('CONTINUE WITH GOOGLE', 'GOOGLE İLE DEVAM ET')}
                    </button>
                  </div>

                  <button 
                    onClick={onClose}
                    className="mt-8 text-gray-400 font-bold hover:text-brand-charcoal transition-colors text-sm uppercase tracking-widest"
                  >
                    {t('CONTINUE AS GUEST', 'ÜYE OLMADAN DEVAM ET')}
                  </button>
                </div>
              )}

              {view === 'login' && (
                <div className="flex flex-col">
                  <h2 className="text-4xl font-black italic mb-8 text-center uppercase tracking-tighter">
                    {t('SIGN IN', 'GİRİŞ YAP')}
                  </h2>

                  <div className="flex bg-gray-100 p-1.5 rounded-2xl mb-8">
                    <button 
                      onClick={() => setLoginMethod('phone')}
                      className={cn(
                        "flex-1 py-3 rounded-xl font-black text-sm transition-all",
                        loginMethod === 'phone' ? "bg-white text-brand-red shadow-sm" : "text-gray-400 hover:text-gray-600"
                      )}
                    >
                      {t('PHONE NUMBER', 'TELEFON NUMARASI')}
                    </button>
                    <button 
                      onClick={() => setLoginMethod('email')}
                      className={cn(
                        "flex-1 py-3 rounded-xl font-black text-sm transition-all",
                        loginMethod === 'email' ? "bg-white text-brand-red shadow-sm" : "text-gray-400 hover:text-gray-600"
                      )}
                    >
                      {t('E-MAIL', 'E-POSTA')}
                    </button>
                  </div>

                  <form onSubmit={handleAuth} className="space-y-6">
                    {loginMethod === 'phone' ? (
                      <div>
                        <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('PHONE NUMBER', 'TELEFON NUMARASI')}</label>
                        <div className="relative">
                          <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                          <input 
                            type="tel" 
                            required
                            value={phone}
                            onChange={(e) => setPhone(e.target.value)}
                            className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-5 pl-12 pr-4 font-bold focus:outline-none focus:border-brand-red transition-colors text-lg"
                            placeholder="5XX XXX XX XX"
                          />
                        </div>
                      </div>
                    ) : (
                      <>
                        <div>
                          <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('EMAIL ADDRESS', 'E-POSTA ADRESİ')}</label>
                          <div className="relative">
                            <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                            <input 
                              type="email" 
                              required
                              value={email}
                              onChange={(e) => setEmail(e.target.value)}
                              className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-5 pl-12 pr-4 font-bold focus:outline-none focus:border-brand-red transition-colors text-lg"
                              placeholder="name@example.com"
                            />
                          </div>
                        </div>
                        <div>
                          <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('PASSWORD', 'ŞİFRE')}</label>
                          <div className="relative">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                            <input 
                              type="password" 
                              required
                              value={password}
                              onChange={(e) => setPassword(e.target.value)}
                              className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-5 pl-12 pr-4 font-bold focus:outline-none focus:border-brand-red transition-colors text-lg"
                              placeholder="••••••••"
                            />
                          </div>
                        </div>
                      </>
                    )}

                    {error && <p className="text-brand-red text-xs font-bold text-center">{error}</p>}

                    <button 
                      type="submit"
                      disabled={loading}
                      className="w-full bg-brand-red text-white py-5 rounded-2xl font-black text-xl hover:bg-brand-charcoal transition-all shadow-xl shadow-brand-red/20 active:scale-[0.98] disabled:opacity-50"
                    >
                      {loading ? t('PROCESSING...', 'İŞLENİYOR...') : t('SIGN IN', 'GİRİŞ YAP')}
                    </button>

                    <div className="relative py-2">
                      <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-gray-100"></div>
                      </div>
                      <div className="relative flex justify-center text-[10px] uppercase font-black tracking-widest">
                        <span className="bg-white px-4 text-gray-400">{t('OR', 'VEYA')}</span>
                      </div>
                    </div>

                    <button 
                      type="button"
                      onClick={() => { loginWithGoogle(); onClose(); }}
                      className="w-full bg-white border-2 border-gray-100 text-brand-charcoal py-4 rounded-2xl font-black text-lg flex items-center justify-center gap-3 hover:border-brand-red transition-all active:scale-[0.98]"
                    >
                      <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="Google" className="w-6 h-6" />
                      {t('SIGN IN WITH GOOGLE', 'GOOGLE İLE GİRİŞ YAP')}
                    </button>
                  </form>

                  <div className="mt-8 pt-8 border-t border-gray-100 text-center">
                    <p className="text-gray-400 font-bold mb-4">{t("DON'T HAVE AN ACCOUNT?", "HESABINIZ YOK MU?")}</p>
                    <button 
                      onClick={() => setView('signup')}
                      className="text-brand-red font-black uppercase tracking-widest hover:underline"
                    >
                      {t('CREATE ACCOUNT', 'ÜYE OLUN')}
                    </button>
                  </div>
                </div>
              )}

              {view === 'signup' && (
                <div className="flex flex-col">
                  <h2 className="text-4xl font-black italic mb-8 text-center uppercase tracking-tighter">
                    {t('CREATE ACCOUNT', 'ÜYE OL')}
                  </h2>

                  <form onSubmit={handleAuth} className="space-y-5">
                    <div>
                      <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('PHONE NUMBER', 'TELEFON NUMARASI')}</label>
                      <div className="relative">
                        <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                        <input 
                          type="tel" 
                          required
                          value={phone}
                          onChange={(e) => setPhone(e.target.value)}
                          className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 pl-12 pr-4 font-bold focus:outline-none focus:border-brand-red transition-colors"
                          placeholder="5XX XXX XX XX"
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('FIRST NAME', 'AD')}</label>
                        <input 
                          type="text" 
                          required
                          value={firstName}
                          onChange={(e) => setFirstName(e.target.value)}
                          className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-4 font-bold focus:outline-none focus:border-brand-red transition-colors"
                          placeholder={t('First Name', 'Adınız')}
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('LAST NAME', 'SOYAD')}</label>
                        <input 
                          type="text" 
                          required
                          value={lastName}
                          onChange={(e) => setLastName(e.target.value)}
                          className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-4 font-bold focus:outline-none focus:border-brand-red transition-colors"
                          placeholder={t('Last Name', 'Soyadınız')}
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('EMAIL ADDRESS', 'E-POSTA ADRESİ')}</label>
                      <input 
                        type="email" 
                        required
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-4 font-bold focus:outline-none focus:border-brand-red transition-colors"
                        placeholder="name@example.com"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">{t('PASSWORD', 'ŞİFRE')}</label>
                      <input 
                        type="password" 
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-4 font-bold focus:outline-none focus:border-brand-red transition-colors"
                        placeholder="••••••••"
                      />
                    </div>

                    <div className="bg-gray-50/50 p-6 rounded-3xl space-y-4">
                      <p className="text-[11px] text-gray-500 leading-relaxed">
                        {t('You can access the clarification text regarding your personal data', 'Kişisel verilerinize dair aydınlatma metnine')} <button type="button" className="text-brand-red font-bold hover:underline">{t('here', 'buradan')}</button> {t('reach.', 'ulaşabilirsiniz.')}
                      </p>
                      <p className="text-[11px] text-gray-500 leading-relaxed">
                        {t('By clicking the Sign Up button, you accept the', 'Üye Ol butonuna tıklayarak')} <button type="button" className="text-brand-red font-bold hover:underline">{t('Membership Conditions', 'Üyelik Koşullarını')}</button> {t('accepting.', 'kabul etmektesiniz.')}
                      </p>
                      
                      <div className="space-y-4 pt-2">
                        <label className="flex items-start gap-3 cursor-pointer group">
                          <input type="checkbox" required className="mt-1 w-4 h-4 rounded border-gray-300 text-brand-red focus:ring-brand-red" />
                          <span className="text-[11px] font-medium text-gray-500 group-hover:text-gray-700 transition-colors leading-tight">
                            <span className="text-brand-red font-bold">{t('Explicit Consent Text', 'Açık Rıza Metni')}</span> {t('within the scope of processing and sharing of my personal data, to be informed about opportunities', 'kapsamında kişisel verilerimin işlenmesine ve paylaşılmasına, fırsatlardan haberdar olmak için')} <span className="text-brand-red font-bold">{t('Communication Permission', 'İletişim İznine')}</span> {t('I approve.', 'onay veriyorum.')}
                          </span>
                        </label>
                        
                        <label className="flex items-start gap-3 cursor-pointer group">
                          <input type="checkbox" required className="mt-1 w-4 h-4 rounded border-gray-300 text-brand-red focus:ring-brand-red" />
                          <span className="text-[11px] font-medium text-gray-500 group-hover:text-gray-700 transition-colors leading-tight">
                            {t('Earn 1 reward for every 2 slices', 'Her 2 dilimde 1 ödül kazandıran')} <span className="text-brand-red font-bold">{t("OMASH Ye-Kazan Loyalty Program Membership Conditions", "ayrıcalıklı OMASH Ye-Kazan Sadakat Programı Üyelik Koşullarını")}</span> {t('I have read and approve, to be informed about opportunities', 'okudum ve onaylıyorum, fırsatlardan haberdar olmak için')} <span className="text-brand-red font-bold">{t('Communication Permission', 'İletişim İznine')}</span> {t('I approve.', 'onay veriyorum.')}
                          </span>
                        </label>
                      </div>
                    </div>

                    {error && <p className="text-brand-red text-xs font-bold text-center">{error}</p>}

                    <button 
                      type="submit"
                      disabled={loading}
                      className="w-full bg-brand-red text-white py-5 rounded-2xl font-black text-xl hover:bg-brand-charcoal transition-all shadow-xl shadow-brand-red/20 active:scale-[0.98] disabled:opacity-50"
                    >
                      {loading ? t('PROCESSING...', 'İŞLENİYOR...') : t('CREATE ACCOUNT', 'ÜYE OL')}
                    </button>

                    <div className="relative py-2">
                      <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-gray-100"></div>
                      </div>
                      <div className="relative flex justify-center text-[10px] uppercase font-black tracking-widest">
                        <span className="bg-white px-4 text-gray-400">{t('OR', 'VEYA')}</span>
                      </div>
                    </div>

                    <button 
                      type="button"
                      onClick={() => { loginWithGoogle(); onClose(); }}
                      className="w-full bg-white border-2 border-gray-100 text-brand-charcoal py-4 rounded-2xl font-black text-lg flex items-center justify-center gap-3 hover:border-brand-red transition-all active:scale-[0.98]"
                    >
                      <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="Google" className="w-6 h-6" />
                      {t('SIGN UP WITH GOOGLE', 'GOOGLE İLE ÜYE OL')}
                    </button>
                  </form>

                  <div className="mt-8 pt-8 border-t border-gray-100 text-center">
                    <p className="text-gray-400 font-bold mb-4">{t("ALREADY HAVE AN ACCOUNT?", "HESABINIZ VAR MI?")}</p>
                    <button 
                      onClick={() => setView('login')}
                      className="text-brand-red font-black uppercase tracking-widest hover:underline"
                    >
                      {t('SIGN IN', 'ÜYE GİRİŞİ')}
                    </button>
                  </div>
                </div>
              )}
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

const LocationsPage = () => {
  const { t, lang } = useTranslation();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedLocation, setSelectedLocation] = useState(LOCATIONS[0]);
  const [isLocating, setIsLocating] = useState(false);

  const filteredLocations = LOCATIONS.filter(loc => 
    loc.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    loc.address.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleGPS = () => {
    setIsLocating(true);
    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition(() => {
        setTimeout(() => {
          setSelectedLocation(LOCATIONS[0]);
          setIsLocating(false);
        }, 1000);
      }, () => {
        setIsLocating(false);
      });
    } else {
      setIsLocating(false);
    }
  };

  return (
    <div className="pt-32 pb-20 bg-brand-white min-h-screen">
      <div className="container mx-auto px-4 md:px-8">
        <div className="mb-12">
          <h1 className="text-5xl md:text-7xl font-black italic tracking-tighter uppercase mb-4">
            {t('OUR', 'BİZİM')} <span className="text-brand-red">{t('LOCATIONS', 'ŞUBELERİMİZ')}</span>
          </h1>
          <p className="text-gray-500 font-bold uppercase tracking-widest">
            {t('Find the nearest OMASH store to you', 'Size en yakın OMASH şubesini bulun')}
          </p>
        </div>

        <div className="grid lg:grid-cols-3 gap-8">
          <div className="lg:col-span-1 space-y-6">
            <div className="relative">
              <input 
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder={t('Search by city or district...', 'Şehir veya ilçe ara...')}
                className="w-full bg-white border-2 border-gray-100 rounded-2xl px-12 py-4 font-bold focus:border-brand-red transition-all outline-none"
              />
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <button 
                onClick={handleGPS}
                className="absolute right-4 top-1/2 -translate-y-1/2 p-2 bg-brand-red text-white rounded-xl hover:bg-brand-charcoal transition-colors"
                title={t('Use my location', 'Konumumu kullan')}
              >
                {isLocating ? <Loader2 className="w-4 h-4 animate-spin" /> : <Navigation className="w-4 h-4" />}
              </button>
            </div>

            <div className="space-y-3 overflow-y-auto max-h-[600px] pr-2 custom-scrollbar">
              {filteredLocations.map((loc) => (
                <button
                  key={loc.id}
                  onClick={() => setSelectedLocation(loc)}
                  className={cn(
                    "w-full text-left p-6 rounded-3xl border-2 transition-all group",
                    selectedLocation.id === loc.id 
                      ? "border-brand-red bg-white shadow-xl shadow-brand-red/5" 
                      : "border-gray-100 bg-white hover:border-gray-200"
                  )}
                >
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-black italic text-xl uppercase tracking-tight group-hover:text-brand-red transition-colors">
                      {loc.name}
                    </h3>
                    <MapPin className={cn("w-5 h-5", selectedLocation.id === loc.id ? "text-brand-red" : "text-gray-300")} />
                  </div>
                  <p className="text-sm text-gray-500 font-medium leading-relaxed mb-4">
                    {loc.address}
                  </p>
                  <div className="flex items-center gap-4 text-[10px] font-black uppercase tracking-widest text-gray-400">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      11:00 - 23:00
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>

          <div className="lg:col-span-2 h-[400px] lg:h-[750px] rounded-[2.5rem] overflow-hidden border-4 border-white shadow-2xl relative">
            <iframe 
              src={`https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3010.0!2d${selectedLocation.lng}!3d${selectedLocation.lat}!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zNDHCsDAyJzM0LjEiTiAyOcKwMDAnMjcuMCJF!5e0!3m2!1sen!2str!4v1620000000000!5m2!1sen!2str`}
              width="100%" 
              height="100%" 
              style={{ border: 0 }} 
              allowFullScreen 
              loading="lazy"
              referrerPolicy="no-referrer-when-downgrade"
            ></iframe>
            
            <div className="absolute bottom-8 left-8 right-8 bg-white/90 backdrop-blur-md p-6 rounded-3xl shadow-2xl border border-white/20 flex flex-col md:flex-row justify-between items-center gap-4">
              <div>
                <h4 className="font-black italic text-2xl uppercase tracking-tight mb-1">{selectedLocation.name}</h4>
                <p className="text-sm font-bold text-gray-500">{selectedLocation.address}</p>
              </div>
              <a 
                href={`https://www.google.com/maps/dir/?api=1&destination=${selectedLocation.lat},${selectedLocation.lng}`}
                target="_blank"
                rel="noopener noreferrer"
                className="bg-brand-red text-white px-8 py-4 rounded-2xl font-black italic uppercase tracking-widest hover:bg-brand-charcoal transition-all flex items-center gap-2"
              >
                <Navigation className="w-5 h-5" />
                {t('GET DIRECTIONS', 'YOL TARİFİ AL')}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// --- Main App ---

export default function App() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isLoginOpen, setIsLoginOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState<'home' | 'about' | 'profile' | 'locations' | 'allergens' | 'deals' | 'full-menu' | 'guarantee' | 'franchising' | 'corporate'>('home');

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [currentPage]);

  return (
    <AuthProvider>
      <CartProvider>
        <LanguageProvider>
          <SearchProvider>
            <LanguageConsumer 
              isMenuOpen={isMenuOpen} 
              setIsMenuOpen={setIsMenuOpen} 
              isLoginOpen={isLoginOpen}
              setIsLoginOpen={setIsLoginOpen}
              isMobileMenuOpen={isMobileMenuOpen}
              setIsMobileMenuOpen={setIsMobileMenuOpen}
              currentPage={currentPage} 
              setCurrentPage={setCurrentPage} 
            />
          </SearchProvider>
        </LanguageProvider>
      </CartProvider>
    </AuthProvider>
  );
}

const LanguageConsumer = ({ isMenuOpen, setIsMenuOpen, isLoginOpen, setIsLoginOpen, isMobileMenuOpen, setIsMobileMenuOpen, currentPage, setCurrentPage }: any) => {
  const { lang, t, formatPrice } = useTranslation();
  const { cart, removeFromCart, updateQuantity, totalPrice, clearCart } = useCart();
  const { user, profile, loading } = useAuth();
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [hasPreviousOrders, setHasPreviousOrders] = useState<boolean | null>(null);
  const [isWelcomeShown, setIsWelcomeShown] = useState(false);
  const [activeCorporateTab, setActiveCorporateTab] = useState<string>('kvkk');
  const [isChatOpen, setIsChatOpen] = useState(false);

  const { searchQuery, setSearchQuery } = useSearch();

  useEffect(() => {
    if (!loading && !user && !isWelcomeShown) {
      const timer = setTimeout(() => {
        setIsLoginOpen(true);
        setIsWelcomeShown(true);
      }, 1500);
      return () => clearTimeout(timer);
    }
  }, [loading, user, isWelcomeShown]);

  useEffect(() => {
    if (user) {
      const q = query(collection(db, 'orders'), where('userId', '==', user.uid));
      getDocs(q).then(snap => {
        setHasPreviousOrders(!snap.empty);
      }).catch(err => {
        console.error("Error checking orders:", err);
        setHasPreviousOrders(true); // Default to true to be safe
      });
    } else {
      setHasPreviousOrders(null);
    }
  }, [user]);

  const isFirstOrder = user && hasPreviousOrders === false;
  const discountAmount = isFirstOrder ? totalPrice * 0.2 : 0;
  const finalPrice = totalPrice - discountAmount;

  const goToCheckout = () => {
    if (!user) {
      setIsLoginOpen(true);
      return;
    }
    if (cart.length === 0) return;
    
    setIsMenuOpen(false);
    setCurrentPage('checkout');
  };

  return (
    <div className="font-sans selection:bg-brand-red selection:text-white w-full overflow-x-hidden min-h-screen">
      <Navbar 
        onOpenMenu={() => setIsMenuOpen(true)} 
        onOpenLogin={() => setIsLoginOpen(true)} 
        onOpenMobileMenu={() => setIsMobileMenuOpen(true)}
        setCurrentPage={setCurrentPage} 
      />
      
      <MobileMenu 
        isOpen={isMobileMenuOpen} 
        onClose={() => setIsMobileMenuOpen(false)} 
        onOpenLogin={() => setIsLoginOpen(true)}
        setCurrentPage={setCurrentPage}
      />
      
      <main>
        {searchQuery !== '' ? (
          <GlobalSearchResults setCurrentPage={setCurrentPage} />
        ) : currentPage === 'home' ? (
          <>
            <Hero />
            <MenuSection onShowFullMenu={() => setCurrentPage('full-menu')} />
            <Locations />
          </>
        ) : currentPage === 'deals' ? (
          <DealsPage />
        ) : currentPage === 'full-menu' ? (
          <FullMenuPage />
        ) : currentPage === 'guarantee' ? (
          <GuaranteePage />
        ) : currentPage === 'about' ? (
          <AboutPage />
        ) : currentPage === 'locations' ? (
          <LocationsPage />
        ) : currentPage === 'allergens' ? (
          <AllergensPage />
        ) : currentPage === 'franchising' ? (
          <FranchisingPage />
        ) : currentPage === 'corporate' ? (
          <CorporatePage activeTab={activeCorporateTab} setActiveTab={setActiveCorporateTab} />
        ) : currentPage === 'checkout' ? (
          <CheckoutPage 
            setCurrentPage={setCurrentPage} 
            isFirstOrder={isFirstOrder} 
            discountAmount={discountAmount} 
            finalPrice={finalPrice} 
          />
        ) : (
          <ProfilePage />
        )}

        <div className="bg-brand-charcoal">
          {currentPage === 'home' && <Features />}
          {(currentPage !== 'profile' && currentPage !== 'locations') && <CTASection onOpenMenu={() => setIsMenuOpen(true)} />}
        </div>
      </main>

      <Footer 
        setCurrentPage={setCurrentPage} 
        onOpenLegal={(type) => {
          setActiveCorporateTab(type);
          setCurrentPage('corporate');
          setTimeout(() => window.scrollTo({ top: 0, behavior: 'instant' as any }), 0);
        }}
        onOpenLogin={() => setIsLoginOpen(true)}
      />
      <ChatButton onClick={() => setIsChatOpen(true)} />

      <ChatModal isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />

      <WelcomeModal isOpen={isLoginOpen} onClose={() => setIsLoginOpen(false)} />

      {/* Simple Cart Sidebar Overlay */}
      <AnimatePresence>
        {isMenuOpen && (
          <>
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsMenuOpen(false)}
              className="fixed inset-0 bg-brand-charcoal/60 backdrop-blur-sm z-[60]"
            />
            <motion.div 
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 200 }}
              className="fixed top-0 right-0 h-full w-full max-w-md bg-white z-[70] shadow-2xl flex flex-col"
            >
              <div className="p-6 border-b flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <h2 className="text-2xl font-black italic">{t('YOUR ORDER', 'SİPARİŞİNİZ')}</h2>
                  
                  {/* Mobile Account Button in Cart */}
                  <button 
                    onClick={() => { setCurrentPage('profile'); setIsMenuOpen(false); }}
                    className="md:hidden w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center hover:bg-brand-red/10 transition-colors"
                  >
                    {user ? (
                      user.photoURL ? (
                        <img src={user.photoURL} alt="Profile" className="w-full h-full rounded-full object-cover" />
                      ) : (
                        <User className="w-5 h-5 text-brand-red" />
                      )
                    ) : (
                      <LogIn className="w-5 h-5 text-gray-400" />
                    )}
                  </button>
                </div>
                <button onClick={() => setIsMenuOpen(false)} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
                  <X className="w-6 h-6" />
                </button>
              </div>
              
              <div className="flex-1 overflow-y-auto p-6">
                {cart.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center text-center">
                    <div className="bg-brand-white p-8 rounded-full mb-6">
                      <ShoppingBag className="w-12 h-12 text-gray-300" />
                    </div>
                    <h3 className="text-xl font-bold mb-2">{t('Your bag is empty', 'Sepetiniz boş')}</h3>
                    <p className="text-gray-500 font-medium mb-8">{t('Add some delicious smash burgers to get started!', 'Başlamak için lezzetli smash burgerlerimizden ekleyin!')}</p>
                    <button 
                      onClick={() => { setIsMenuOpen(false); setCurrentPage('full-menu'); }}
                      className="bg-brand-red text-white px-8 py-4 rounded-xl font-black hover:bg-brand-charcoal transition-all"
                    >
                      {t('BROWSE MENU', 'MENÜYE GÖZ AT')}
                    </button>
                  </div>
                ) : (
                  <div className="space-y-6">
                    {cart.map((item) => (
                      <div key={item.cartId} className="flex gap-4">
                        <div className="w-20 h-20 rounded-2xl overflow-hidden flex-shrink-0">
                          <img src={item.image} alt={item.name} className="w-full h-full object-cover" />
                        </div>
                        <div className="flex-1">
                          <div className="flex justify-between mb-1">
                            <div className="flex flex-col">
                              <h4 className="font-black italic text-sm">
                                {item.level && <span className="text-brand-red mr-1">{item.level}</span>}
                                {formatItemName(item.name)}
                              </h4>
                              {(lang === 'en' ? item.tagline : item.taglineTr) && (
                                <span className="inline-block bg-brand-red text-white text-[8px] font-black uppercase tracking-tighter px-1.5 py-0.5 mb-1 w-fit">
                                  {lang === 'en' ? item.tagline : item.taglineTr}
                                </span>
                              )}
                            </div>
                            <button onClick={() => removeFromCart(item.cartId)} className="text-gray-400 hover:text-brand-red">
                              <X className="w-4 h-4" />
                            </button>
                          </div>
                          {item.selectedExtras.length > 0 && (
                            <div className="mt-2 space-y-1 mb-3">
                              {item.selectedExtras.map((e, idx) => (
                                <div key={idx} className="flex justify-between items-center text-[11px] text-gray-600 font-bold bg-gray-50 px-2 py-1 rounded-lg border border-gray-100/50">
                                  <span className="flex items-center gap-1.5">
                                    <Plus className="w-2.5 h-2.5 text-brand-red" />
                                    {lang === 'en' ? e.name : e.nameTr}
                                  </span>
                                  <span className="text-brand-red/70">+{formatPrice(e.price)}</span>
                                </div>
                              ))}
                            </div>
                          )}
                          <div className="flex justify-between items-center mb-3">
                            <p className="text-brand-red font-black text-base whitespace-nowrap">
                              {formatPrice((item.price + item.selectedExtras.reduce((s, e) => s + e.price, 0)) * item.quantity)}
                            </p>
                            <div className="flex items-center gap-3 bg-gray-50 p-1 rounded-xl border border-gray-100">
                              <button 
                                onClick={() => updateQuantity(item.cartId, -1)}
                                className="w-8 h-8 rounded-lg bg-white shadow-sm flex items-center justify-center hover:bg-brand-red hover:text-white transition-all active:scale-90"
                              >
                                <Minus className="w-4 h-4" />
                              </button>
                              <span className="font-black w-6 text-center text-sm">{item.quantity}</span>
                              <button 
                                onClick={() => updateQuantity(item.cartId, 1)}
                                className="w-8 h-8 rounded-lg bg-white shadow-sm flex items-center justify-center hover:bg-brand-red hover:text-white transition-all active:scale-90"
                              >
                                <Plus className="w-4 h-4" />
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              
              <div className="p-6 border-t bg-brand-white">
                {isFirstOrder && (
                  <div className="flex justify-between mb-2 text-brand-red font-black text-sm uppercase italic">
                    <span>{t('First Order Discount (20%)', 'İlk Sipariş İndirimi (%20)')}</span>
                    <span>-{formatPrice(discountAmount)}</span>
                  </div>
                )}
                <div className="flex justify-between mb-4 font-bold text-lg">
                  <span>{t('Total', 'Toplam')}</span>
                  <span className="whitespace-nowrap">{formatPrice(finalPrice)}</span>
                </div>
                <button 
                  disabled={cart.length === 0}
                  onClick={goToCheckout}
                  className={cn(
                    "w-full py-5 rounded-2xl font-black text-lg transition-all flex items-center justify-center gap-2",
                    cart.length === 0
                      ? "bg-gray-300 text-white cursor-not-allowed" 
                      : "bg-brand-red text-white hover:bg-brand-charcoal shadow-xl shadow-brand-red/20 active:scale-95"
                  )}
                >
                  {t('CHECKOUT', 'ÖDEME YAP')}
                </button>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}

const CTASection = ({ onOpenMenu }: { onOpenMenu: () => void }) => {
  const { t } = useTranslation();
  return (
    <section className="py-16 md:py-24 bg-brand-red relative overflow-hidden [clip-path:polygon(0_15%,100%_0,100%_100%,0_100%)] -mt-8 md:-mt-12">
      <div className="container mx-auto px-4 md:px-8 relative z-10 text-center">
          <h2 className="text-5xl md:text-8xl font-black text-white italic mb-8 tracking-tighter leading-none">
            READY TO <span className="text-brand-charcoal">SMASH?</span>
          </h2>
          <p className="text-white/90 text-xl md:text-2xl font-bold mb-12 max-w-2xl mx-auto">
            {t('Order now and get 20% off your first order with code', 'Şimdi sipariş ver ve ilk siparişinde şu kodla %20 indirim kazan:')}{' '}
            <span className="bg-brand-charcoal px-4 py-1 rounded-lg">FIRSTSMASH20</span>
          </p>
          <button 
            onClick={onOpenMenu}
            className="bg-white text-brand-red px-12 py-6 rounded-2xl font-black text-2xl hover:bg-brand-charcoal hover:text-white transition-all shadow-2xl active:scale-95"
          >
            {t('START YOUR ORDER', 'SİPARİŞE BAŞLA')}
          </button>
        </div>
        
        {/* Decorative elements */}
        <div className="absolute top-0 left-0 w-64 h-64 bg-white/10 rounded-full -translate-x-1/2 -translate-y-1/2 blur-3xl" />
        <div className="absolute bottom-0 right-0 w-96 h-96 bg-brand-charcoal/10 rounded-full translate-x-1/3 translate-y-1/3 blur-3xl" />
    </section>
  );
};
