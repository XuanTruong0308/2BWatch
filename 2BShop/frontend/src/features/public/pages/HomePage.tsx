import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { ProductTile } from "@/components/ui/ProductTile";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { ErrorState } from "@/components/ui/ErrorState";
import { ScrollReveal } from "@/components/ui/ScrollReveal";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, HomePageData, ProductCard } from "@/lib/api/types";
import { useAddToCart } from "@/hooks/useAddToCart";

function HeroSection() {
  return (
    <section className="home-hero-section">
      <video className="home-hero-video" autoPlay muted loop playsInline>
        <source src="/video/hero-section.mp4" type="video/mp4" />
        Trinh duyet cua ban khong ho tro video.
      </video>
      <div className="home-hero-overlay" />
      <div className="home-hero-content">
        <ScrollReveal animation="fade-down" delay={200}>
          <h1 className="home-hero-title">WELCOME TO 2BSHOP</h1>
        </ScrollReveal>
        <ScrollReveal animation="fade-up" delay={500}>
          <p className="home-hero-subtitle">Khám phá ngay bộ sưu tập đồng hồ cao cấp, sang trọng và đẳng cấp.</p>
        </ScrollReveal>
        <ScrollReveal animation="zoom-in" delay={800}>
          <div className="home-hero-actions">
            <a href="#bestsellers" className="home-btn home-btn-primary">
              Khám phá ngay
            </a>
            <Link to="/watches/newest" className="home-btn home-btn-outline">
              Sản phẩm mới
            </Link>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
}

interface ProductSectionProps {
  id?: string;
  eyebrow: string;
  title: string;
  description: string;
  products: ProductCard[];
  actionLink?: string;
  actionText?: string;
  onAddToCart: (id: number) => void;
  isAddingToCart: (id: number) => boolean;
  backgroundColor?: "white" | "gray";
}

function ProductSection({
  id,
  eyebrow,
  title,
  description,
  products,
  actionLink,
  actionText,
  onAddToCart,
  isAddingToCart,
  backgroundColor = "white",
}: ProductSectionProps) {
  if (!products || products.length === 0) {
    return null;
  }

  return (
    <section id={id} className={`home-product-section bg-${backgroundColor}`}>
      <div className="home-section-header">
        <ScrollReveal animation="fade-down" delay={100}>
          <p className="home-section-eyebrow">{eyebrow}</p>
        </ScrollReveal>
        <ScrollReveal animation="fade-in" delay={200}>
          <h2 className="home-section-title">{title}</h2>
        </ScrollReveal>
        <ScrollReveal animation="fade-up" delay={300}>
          <p className="home-section-description">{description}</p>
        </ScrollReveal>
      </div>

      <div className="home-product-grid">
        {products.map((product, idx) => (
          <ScrollReveal key={product.watchId} animation="fade-up" delay={150 * (idx + 1)} threshold={0}>
            <ProductTile
              product={product}
              action={
                <button
                  className="home-btn-cart"
                  disabled={isAddingToCart(product.watchId) || (product.stockQuantity ?? 0) <= 0}
                  onClick={() => onAddToCart(product.watchId)}
                  type="button"
                >
                  {isAddingToCart(product.watchId)
                    ? "Đang xử lý..."
                    : (product.stockQuantity ?? 0) <= 0
                      ? "Hết hàng"
                      : "Thêm vào giỏ"}
                </button>
              }
            />
          </ScrollReveal>
        ))}
      </div>

      {actionLink && actionText ? (
        <ScrollReveal animation="zoom-in" delay={400} className="home-section-footer">
          <Link to={actionLink} className="home-btn home-btn-outline-dark">
            {actionText}
          </Link>
        </ScrollReveal>
      ) : null}
    </section>
  );
}

export default function HomePage() {
  const homeQuery = useQuery({
    queryKey: ["public", "home"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<HomePageData>>("/api/v1/public/home");
      return response.data;
    },
  });

  const addToCart = useAddToCart();

  if (homeQuery.isLoading) {
    return <LoadingScreen label="Dang tai storefront..." />;
  }

  if (homeQuery.isError || !homeQuery.data) {
    return <ErrorState message="Khong the tai du lieu trang chu." />;
  }

  const { bestSellers, newestProducts, biggestDiscounts } = homeQuery.data;

  return (
    <main className="home-page-container">
      <HeroSection />

      {addToCart.feedback ? (
        <div className={`inline-alert inline-alert-${addToCart.feedback.tone}`} style={{ margin: "0 auto 28px", maxWidth: 1280 }}>
          {addToCart.feedback.message}
        </div>
      ) : null}

      <ProductSection
        id="bestsellers"
        eyebrow="Đẳng cấp và sang trọng"
        title="Bán Chạy Nhất"
        description="Bộ sưu tập những mẫu đồng hồ cao cấp được săn đón nhiều nhất."
        products={bestSellers}
        onAddToCart={addToCart.addToCart}
        isAddingToCart={addToCart.isAdding}
        backgroundColor="white"
      />

      <ProductSection
        eyebrow="Tuyệt tác mới"
        title="SẢN PHẨM MỚI NHẤT"
        description="Khám phá ngay những thiết kế vừa ra mắt."
        products={newestProducts}
        onAddToCart={addToCart.addToCart}
        isAddingToCart={addToCart.isAdding}
        actionLink="/watches/newest"
        actionText="Xem toàn bộ sản phẩm mới"
        backgroundColor="gray"
      />

      <ProductSection
        eyebrow="Đặc quyền giới hạn"
        title="GIẢM GIÁ SÂU NHẤT"
        description="Sở hữu tuyệt tác đồng hồ với mức giá ưu đãi chưa từng có."
        products={biggestDiscounts}
        onAddToCart={addToCart.addToCart}
        isAddingToCart={addToCart.isAdding}
        actionLink="/watches/discount"
        actionText="Xem tất cả ưu đãi"
        backgroundColor="white"
      />
    </main>
  );
}
