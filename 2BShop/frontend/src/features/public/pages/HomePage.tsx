import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { ProductTile } from "@/components/ui/ProductTile";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { ErrorState } from "@/components/ui/ErrorState";
import { ScrollReveal } from "@/components/ui/ScrollReveal";
import { getJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";
import type { ApiResponse, HomePageData, ProductCard } from "@/lib/api/types";
import { useAddToCart } from "@/hooks/useAddToCart";

function HeroSection() {
  const { tx, language } = useI18n();
  const heroTitleLines =
    language === "vi" ? ["SỞ HỮU", "THỜI GIAN", "THEO CÁCH", "CỦA BẠN."] : ["OWN TIME.", "WEAR IT", "WITH INTENT."];

  return (
    <section className="home-hero-section">
      <video className="home-hero-video" autoPlay muted loop playsInline>
        <source src="/video/hero-section.mp4" type="video/mp4" />
        Your browser does not support background video.
      </video>
      <div className="home-hero-overlay" />

      <div className="home-hero-content">
        <ScrollReveal animation="fade-down" delay={120}>
          <span className="eyebrow" style={{ color: "rgba(255,255,255,0.72)" }}>
            {tx("Không gian mua sắm hiện đại", "Editorial watch retail")}
          </span>
        </ScrollReveal>

        <ScrollReveal animation="fade-down" delay={220}>
          <h1 className="home-hero-title">
            {heroTitleLines.map((line) => (
              <span className="home-hero-title-line" key={line}>
                {line}
              </span>
            ))}
          </h1>
        </ScrollReveal>

        <div className="home-hero-copy">
          <ScrollReveal animation="fade-up" delay={360}>
            <p className="home-hero-subtitle">
              {tx(
                "Khám phá bộ sưu tập đồng hồ với giao diện đơn sắc, nổi bật chất liệu, mặt số và đường nét của từng sản phẩm.",
                "Discover a sharper watch storefront where monochrome UI steps back and every case, dial and material takes the lead.",
              )}
            </p>
          </ScrollReveal>

          <ScrollReveal animation="zoom-in" delay={520}>
            <div className="home-hero-actions">
              <a href="#bestsellers" className="home-btn home-btn-primary">
                {tx("Khám phá ngay", "Explore now")}
              </a>
              <Link to="/watches/newest" className="home-btn home-btn-outline">
                {tx("Hàng mới", "New arrivals")}
              </Link>
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
}

interface ProductSectionProps {
  id?: string;
  eyebrowVi: string;
  eyebrowEn: string;
  titleVi: string;
  titleEn: string;
  descriptionVi: string;
  descriptionEn: string;
  products: ProductCard[];
  actionLink?: string;
  actionTextVi?: string;
  actionTextEn?: string;
  onAddToCart: (id: number) => void;
  isAddingToCart: (id: number) => boolean;
  backgroundColor?: "white" | "gray";
}

function ProductSection({
  id,
  eyebrowVi,
  eyebrowEn,
  titleVi,
  titleEn,
  descriptionVi,
  descriptionEn,
  products,
  actionLink,
  actionTextVi,
  actionTextEn,
  onAddToCart,
  isAddingToCart,
  backgroundColor = "white",
}: ProductSectionProps) {
  const { tx } = useI18n();

  if (!products || products.length === 0) {
    return null;
  }

  return (
    <section id={id} className={`home-product-section bg-${backgroundColor}`}>
      <div className="home-section-header">
        <ScrollReveal animation="fade-down" delay={80}>
          <p className="home-section-eyebrow">{tx(eyebrowVi, eyebrowEn)}</p>
        </ScrollReveal>

        <ScrollReveal animation="fade-in" delay={140}>
          <h2 className="home-section-title">{tx(titleVi, titleEn)}</h2>
        </ScrollReveal>

        <ScrollReveal animation="fade-up" delay={220}>
          <p className="home-section-description">{tx(descriptionVi, descriptionEn)}</p>
        </ScrollReveal>
      </div>

      <div className="home-product-grid">
        {products.map((product, idx) => (
          <ScrollReveal key={product.watchId} animation="fade-up" delay={60 * (idx + 1)} threshold={0}>
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
                    ? tx("Đang xử lý...", "Processing...")
                    : (product.stockQuantity ?? 0) <= 0
                      ? tx("Hết hàng", "Sold out")
                      : tx("Thêm vào giỏ", "Add to cart")}
                </button>
              }
            />
          </ScrollReveal>
        ))}
      </div>

      {actionLink && actionTextVi && actionTextEn ? (
        <ScrollReveal animation="zoom-in" delay={320} className="home-section-footer">
          <Link to={actionLink} className="home-btn home-btn-outline-dark">
            {tx(actionTextVi, actionTextEn)}
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
  const { tx } = useI18n();

  if (homeQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải trang chủ...", "Loading storefront...")} />;
  }

  if (homeQuery.isError || !homeQuery.data) {
    return <ErrorState message={tx("Không thể tải trang chủ lúc này.", "We could not load the storefront right now.")} />;
  }

  const { bestSellers, newestProducts, biggestDiscounts } = homeQuery.data;

  return (
    <main className="home-page-container">
      <HeroSection />

      {addToCart.feedback ? (
        <div className={`inline-alert inline-alert-${addToCart.feedback.tone}`} style={{ margin: "24px auto 0", maxWidth: 1440 }}>
          {addToCart.feedback.message}
        </div>
      ) : null}

      <ProductSection
        id="bestsellers"
        eyebrowVi="Bán chạy nhất"
        eyebrowEn="Best sellers"
        titleVi="Những mẫu được chọn nhiều nhất."
        titleEn="Built for everyday prestige."
        descriptionVi="Những thiết kế được khách hàng lựa chọn nhiều nhất, nổi bật về chất liệu, độ hoàn thiện và tỷ lệ mặt số."
        descriptionEn="The strongest performers in the collection, selected by demand and shaped by premium detailing."
        products={bestSellers}
        onAddToCart={addToCart.addToCart}
        isAddingToCart={addToCart.isAdding}
        backgroundColor="white"
      />

      <ProductSection
        eyebrowVi="Hàng mới"
        eyebrowEn="Latest drops"
        titleVi="Sản phẩm mới nhất"
        titleEn="Newest arrivals"
        descriptionVi="Những mẫu vừa lên kệ, được trình bày theo nhịp điệu gọn gàng và dễ quét nhanh hơn."
        descriptionEn="Fresh launches presented with a tighter editorial rhythm and a cleaner product-first layout."
        products={newestProducts}
        onAddToCart={addToCart.addToCart}
        isAddingToCart={addToCart.isAdding}
        actionLink="/watches/newest"
        actionTextVi="Xem tất cả hàng mới"
        actionTextEn="View all new arrivals"
        backgroundColor="gray"
      />

      <ProductSection
        eyebrowVi="Ưu đãi giới hạn"
        eyebrowEn="Limited offers"
        titleVi="Bộ sưu tập giảm giá"
        titleEn="The sale edit"
        descriptionVi="Những sản phẩm đang có ưu đãi, vẫn giữ nguyên cảm giác cao cấp trong cách trình bày và lựa chọn."
        descriptionEn="Reduced pieces with the same premium treatment, surfaced through a restrained monochrome storefront."
        products={biggestDiscounts}
        onAddToCart={addToCart.addToCart}
        isAddingToCart={addToCart.isAdding}
        actionLink="/watches/discount"
        actionTextVi="Xem tất cả ưu đãi"
        actionTextEn="View all sale pieces"
        backgroundColor="white"
      />
    </main>
  );
}
