import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ProductTile } from "@/components/ui/ProductTile";
import { Pagination } from "@/components/ui/Pagination";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { ErrorState } from "@/components/ui/ErrorState";
import { ScrollReveal } from "@/components/ui/ScrollReveal";
import { getJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";
import type { ApiResponse, HomePageData, PaginatedResponse, ProductCard } from "@/lib/api/types";
import { useAddToCart } from "@/hooks/useAddToCart";

type CatalogPageProps = {
  variant?: "all" | "newest" | "discount";
};

export default function CatalogPage({ variant = "all" }: CatalogPageProps) {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const page = Number(searchParams.get("page") || 0);
  const addToCart = useAddToCart();
  const { tx } = useI18n();

  const optionsQuery = useQuery({
    queryKey: ["public", "options"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<HomePageData>>("/api/v1/public/home").catch(() => null);
      return response?.data;
    },
  });

  const productsQuery = useQuery({
    queryKey: ["catalog", variant, searchParams.toString()],
    queryFn: async () => {
      const endpoint =
        variant === "newest"
          ? "/api/v1/public/watches/newest"
          : variant === "discount"
            ? "/api/v1/public/watches/discount"
            : "/api/v1/public/watches";
      const response = await getJson<PaginatedResponse<ProductCard>>(`${endpoint}?${searchParams.toString()}`);
      return response;
    },
  });

  const overview = useMemo(() => {
    if (variant === "newest") {
      return {
        eyebrow: tx("Hàng mới", "New arrivals"),
        title: tx("Sản phẩm mới nhất", "Latest watch drops"),
        desc: tx(
          "Những mẫu vừa lên kệ, được sắp xếp gọn gàng để người dùng quét nhanh và so sánh dễ hơn.",
          "New pieces surfaced through a sharp editorial grid with product imagery as the only source of color.",
        ),
      };
    }

    if (variant === "discount") {
      return {
        eyebrow: tx("Bộ sưu tập giảm giá", "Sale edit"),
        title: tx("Những sản phẩm đang ưu đãi", "Discount collection"),
        desc: tx(
          "Các mẫu đang có ưu đãi nhưng vẫn giữ trải nghiệm mua sắm sạch, rõ và dễ ra quyết định.",
          "High-impact offers presented through a flatter, cleaner buying experience.",
        ),
      };
    }

    return {
      eyebrow: tx("Tất cả bộ sưu tập", "Full collection"),
      title: tx("Khám phá tất cả đồng hồ", "Explore every watch"),
      desc: tx(
        "Danh mục được trình bày theo mật độ gọn hơn, dễ lọc nhanh theo thương hiệu, giá và tình trạng sản phẩm.",
        "A dense, product-first catalog with restrained UI and faster scanning across brand, category and price.",
      ),
    };
  }, [tx, variant]);

  const updateParams = (updater: (next: URLSearchParams) => void) => {
    const next = new URLSearchParams(searchParams);
    updater(next);
    setSearchParams(next);
  };

  const changePage = (nextPage: number) => {
    updateParams((next) => {
      next.set("page", String(nextPage));
    });
  };

  if (productsQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải danh muc...", "Loading catalog...")} />;
  }

  if (productsQuery.isError || !productsQuery.data) {
    return <ErrorState message={tx("Không thể tải danh muc lúc này.", "We could not load the collection right now.")} />;
  }

  return (
    <main className="catalog-page-container">
      <ScrollReveal animation="fade-down" className="catalog-header">
        <span className="catalog-eyebrow">{overview.eyebrow}</span>
        <h1 className="catalog-title">{overview.title}</h1>
        <p className="catalog-description">{overview.desc}</p>
      </ScrollReveal>

      {addToCart.feedback ? (
        <div className={`inline-alert inline-alert-${addToCart.feedback.tone}`} style={{ marginBottom: 24 }}>
          {addToCart.feedback.message}
        </div>
      ) : null}

      {variant === "all" ? (
        <ScrollReveal animation="fade-in" delay={180} className="catalog-filters">
          <div className="catalog-filter-group">
            <label className="catalog-filter-label" htmlFor="search">
              {tx("Tìm kiếm", "Search")}
            </label>
            <input
              className="catalog-input"
              defaultValue={searchParams.get("search") || ""}
              id="search"
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  updateParams((next) => {
                    next.set("search", (event.target as HTMLInputElement).value);
                    next.set("page", "0");
                  });
                }
              }}
              placeholder={tx("Tên đồng hồ hoặc bộ sưu tập...", "Watch name or collection...")}
            />
          </div>

          <div className="catalog-filter-group">
            <label className="catalog-filter-label" htmlFor="brand">
              {tx("Thương hiệu", "Brand")}
            </label>
            <select
              className="catalog-select"
              id="brand"
              value={searchParams.get("brand") || ""}
              onChange={(event) => {
                updateParams((next) => {
                  if (event.target.value) {
                    next.set("brand", event.target.value);
                  } else {
                    next.delete("brand");
                  }
                  next.set("page", "0");
                });
              }}
            >
              <option value="">{tx("Tất cả thương hiệu", "All brands")}</option>
              {optionsQuery.data?.brands?.map((brand) => (
                <option key={brand.id} value={brand.value}>
                  {brand.label}
                </option>
              ))}
            </select>
          </div>

          <div className="catalog-filter-group">
            <label className="catalog-filter-label" htmlFor="sortBy">
              {tx("Sắp xếp", "Sort")}
            </label>
            <select
              className="catalog-select"
              id="sortBy"
              value={searchParams.get("sortBy") || ""}
              onChange={(event) => {
                updateParams((next) => {
                  if (event.target.value) {
                    next.set("sortBy", event.target.value);
                  } else {
                    next.delete("sortBy");
                  }
                });
              }}
            >
              <option value="">{tx("Đề xuất", "Recommended")}</option>
              <option value="price-asc">{tx("Giá tăng dần", "Price low to high")}</option>
              <option value="price-desc">{tx("Giá giảm dần", "Price high to low")}</option>
              <option value="newest">{tx("Mới nhất", "Newest")}</option>
            </select>
          </div>

          <div className="catalog-filter-group">
            <label className="catalog-filter-label" htmlFor="pageState">
              {tx("Trang", "Page")}
            </label>
            <input
              className="catalog-input"
              id="pageState"
              readOnly
              value={`${tx("Trang", "Page")} ${page + 1} / ${Math.max(productsQuery.data.totalPages, 1)}`}
            />
          </div>
        </ScrollReveal>
      ) : null}

      {productsQuery.data.items.length === 0 ? (
        <ScrollReveal animation="zoom-in" className="catalog-empty">
          <h2 className="catalog-empty-title">{tx("Không có sản phẩm phù hợp", "No matching products")}</h2>
          <p className="catalog-empty-text">{tx("Thử điều chỉnh bộ lọc hoặc quay về trang chủ để tiếp tục xem sản phẩm.", "Try a broader filter or return to the storefront to continue browsing.")}</p>
          <button className="catalog-btn-primary" onClick={() => navigate("/")} type="button">
            {tx("Quay về trang chủ", "Return home")}
          </button>
        </ScrollReveal>
      ) : (
        <>
          <div className="catalog-product-grid">
            {productsQuery.data.items.map((product, idx) => (
              <ScrollReveal key={product.watchId} animation="fade-up" delay={40 * (idx % 8)} threshold={0}>
                <ProductTile
                  product={product}
                  action={
                    <button
                      className="catalog-btn-add"
                      disabled={addToCart.isAdding(product.watchId) || (product.stockQuantity ?? 0) <= 0}
                      onClick={() => addToCart.addToCart(product.watchId)}
                      type="button"
                    >
                      {addToCart.isAdding(product.watchId)
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

          {productsQuery.data.totalPages > 1 ? (
            <ScrollReveal animation="fade-up" className="catalog-pagination">
              <Pagination currentPage={page} onPageChange={changePage} totalPages={productsQuery.data.totalPages} />
            </ScrollReveal>
          ) : null}
        </>
      )}
    </main>
  );
}
