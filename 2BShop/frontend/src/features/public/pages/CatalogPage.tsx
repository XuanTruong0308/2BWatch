import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ProductTile } from "@/components/ui/ProductTile";
import { Pagination } from "@/components/ui/Pagination";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { ErrorState } from "@/components/ui/ErrorState";
import { ScrollReveal } from "@/components/ui/ScrollReveal";
import { getJson } from "@/lib/api/client";
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
        title: "Tuyet Tac Moi",
        desc: "Kham pha ngay nhung thiet ke vua ra mat mang hoi tho thoi dai.",
      };
    }
    if (variant === "discount") {
      return {
        title: "Dac Quyen Gioi Han",
        desc: "So huu tuyet tac dong ho voi muc gia uu dai chua tung co.",
      };
    }
    return {
      title: "Bo Suu Tap Dong Ho",
      desc: "Tinh hoa cua thoi gian hoi tu trong tung thiet ke sang trong va dang cap.",
    };
  }, [variant]);

  const changePage = (nextPage: number) => {
    searchParams.set("page", String(nextPage));
    setSearchParams(searchParams);
  };

  if (productsQuery.isLoading) {
    return <LoadingScreen label="Dang tai danh muc..." />;
  }

  if (productsQuery.isError || !productsQuery.data) {
    return <ErrorState message="Khong the tai danh sach san pham." />;
  }

  return (
    <main className="catalog-page-container">
      <ScrollReveal animation="fade-down" className="catalog-header">
        <span className="catalog-eyebrow">2BSHOP COLLECTION</span>
        <h1 className="catalog-title">{overview.title}</h1>
        <p className="catalog-description">{overview.desc}</p>
      </ScrollReveal>

      {addToCart.feedback ? (
        <div className={`inline-alert inline-alert-${addToCart.feedback.tone}`} style={{ marginBottom: 24 }}>
          {addToCart.feedback.message}
        </div>
      ) : null}

      {variant === "all" ? (
        <ScrollReveal animation="fade-in" delay={200} className="catalog-filters">
          <div className="catalog-filter-group">
            <label className="catalog-filter-label" htmlFor="search">Tim kiem</label>
            <input
              className="catalog-input"
              defaultValue={searchParams.get("search") || ""}
              id="search"
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  searchParams.set("search", (event.target as HTMLInputElement).value);
                  searchParams.set("page", "0");
                  setSearchParams(searchParams);
                }
              }}
              placeholder="Ten hoac dong san pham..."
            />
          </div>

          <div className="catalog-filter-group">
            <label className="catalog-filter-label" htmlFor="brand">Thuong hieu</label>
            <select
              className="catalog-select"
              id="brand"
              value={searchParams.get("brand") || ""}
              onChange={(event) => {
                if (event.target.value) {
                  searchParams.set("brand", event.target.value);
                } else {
                  searchParams.delete("brand");
                }
                searchParams.set("page", "0");
                setSearchParams(searchParams);
              }}
            >
              <option value="">Tat ca thuong hieu</option>
              {optionsQuery.data?.brands?.map((brand) => (
                <option key={brand.id} value={brand.value}>
                  {brand.label}
                </option>
              ))}
            </select>
          </div>

          <div className="catalog-filter-group">
            <label className="catalog-filter-label" htmlFor="sortBy">Sap xep theo</label>
            <select
              className="catalog-select"
              id="sortBy"
              value={searchParams.get("sortBy") || ""}
              onChange={(event) => {
                if (event.target.value) {
                  searchParams.set("sortBy", event.target.value);
                } else {
                  searchParams.delete("sortBy");
                }
                setSearchParams(searchParams);
              }}
            >
              <option value="">Goi y</option>
              <option value="price-asc">Gia: thap den cao</option>
              <option value="price-desc">Gia: cao xuong thap</option>
              <option value="newest">Moi nhat</option>
            </select>
          </div>
        </ScrollReveal>
      ) : null}

      {productsQuery.data.items.length === 0 ? (
        <ScrollReveal animation="zoom-in" className="catalog-empty">
          <h2 className="catalog-empty-title">Khong Tim Thay San Pham</h2>
          <p className="catalog-empty-text">Hien tai khong co san pham nao phu hop voi tieu chi cua ban.</p>
          <button className="catalog-btn-primary" onClick={() => navigate("/")} type="button">
            Quay ve trang chu
          </button>
        </ScrollReveal>
      ) : (
        <>
          <div className="catalog-product-grid">
            {productsQuery.data.items.map((product, idx) => (
              <ScrollReveal key={product.watchId} animation="fade-up" delay={50 * (idx % 8)} threshold={0}>
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
                        ? "Dang xu ly..."
                        : (product.stockQuantity ?? 0) <= 0
                          ? "Het hang"
                          : "Them vao gio"}
                    </button>
                  }
                />
              </ScrollReveal>
            ))}
          </div>

          {productsQuery.data.totalPages > 1 ? (
            <ScrollReveal animation="fade-up" className="catalog-pagination">
              <Pagination
                currentPage={page}
                onPageChange={changePage}
                totalPages={productsQuery.data.totalPages}
              />
            </ScrollReveal>
          ) : null}
        </>
      )}
    </main>
  );
}
