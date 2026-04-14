import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useParams } from "react-router-dom";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, ProductDetail } from "@/lib/api/types";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { ErrorState } from "@/components/ui/ErrorState";
import { ProductTile } from "@/components/ui/ProductTile";
import { Badge } from "@/components/ui/Badge";
import { formatCurrency } from "@/lib/utils/format";
import { ScrollReveal } from "@/components/ui/ScrollReveal";
import { useAddToCart } from "@/hooks/useAddToCart";

export default function ProductDetailPage() {
  const { id } = useParams();
  const [quantity, setQuantity] = useState(1);
  const [activeImage, setActiveImage] = useState(0);
  const addToCart = useAddToCart();

  const query = useQuery({
    queryKey: ["product", id],
    queryFn: async () => {
      const response = await getJson<ApiResponse<ProductDetail>>(`/api/v1/public/watches/${id}`);
      return response.data;
    },
  });

  if (query.isLoading) {
    return <LoadingScreen label="Dang tai chi tiet san pham..." />;
  }

  if (query.isError || !query.data) {
    return <ErrorState message="Khong the tai thong tin san pham." />;
  }

  const product = query.data;
  const image = product.images[activeImage]?.url || product.images[0]?.url;
  const productId = Number(id);
  const isOutOfStock = (product.stockQuantity ?? 0) <= 0;

  return (
    <div className="split-grid">
      <ScrollReveal animation="slide-right" className="panel">
        <div className="product-cover" style={{ marginBottom: 18 }}>
          <img alt={product.watchName} src={image} />
        </div>
        <div className="product-grid" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(120px, 1fr))" }}>
          {product.images.map((entry, index) => (
            <button
              key={entry.id}
              className={`button ${activeImage === index ? "button-primary" : "button-subtle"}`}
              onClick={() => setActiveImage(index)}
              type="button"
            >
              Ảnh {index + 1}
            </button>
          ))}
        </div>
      </ScrollReveal>

      <ScrollReveal animation="slide-left" delay={200} className="panel">
        <div className="eyebrow">{product.brandName}</div>
        <h1>{product.watchName}</h1>
        <div className="price-stack">
          <span className="price-main">{formatCurrency(product.priceAfterDiscount)}</span>
          {product.discountPercent ? <span className="price-old">{formatCurrency(product.price)}</span> : null}
        </div>
        <div className="header-actions" style={{ justifyContent: "flex-start", marginBottom: 16 }}>
          <Badge label={isOutOfStock ? "ết hàng" : "Còn hàng"} />
          <Badge label={`Đã bán ${product.soldCount ?? 0}`} tone="info" />
        </div>
        <p className="muted-copy">{product.description}</p>
        <div className="info-grid" style={{ margin: "24px 0" }}>
          <div className="metric-card">
            <span className="eyebrow">Thương hiệu</span>
            <strong>{product.brandName}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">Danh mục</span>
            <strong>{product.categoryName}</strong>
          </div>
        </div>
        <div className="header-actions" style={{ justifyContent: "flex-start", marginBottom: 18 }}>
          <button className="button button-subtle" onClick={() => setQuantity((value) => Math.max(1, value - 1))} type="button">
            -
          </button>
          <span>{quantity}</span>
          <button className="button button-subtle" onClick={() => setQuantity((value) => value + 1)} type="button">
            +
          </button>
        </div>
        <button
          className="button button-primary"
          disabled={addToCart.isAdding(productId) || isOutOfStock}
          onClick={() => addToCart.addToCart(productId, quantity)}
          type="button"
        >
          {addToCart.isAdding(productId) ? "Dang xu ly..." : isOutOfStock ? "Hết hàng" : "Thêm vào giỏ hàng"}
        </button>
        {addToCart.feedback ? (
          <div className={`inline-alert inline-alert-${addToCart.feedback.tone}`} style={{ marginTop: 16 }}>
            {addToCart.feedback.message}
          </div>
        ) : null}
      </ScrollReveal>

      <div className="panel" style={{ gridColumn: "1 / -1" }}>
        <ScrollReveal animation="fade-down">
          <h2>Sản phẩm liên quan</h2>
        </ScrollReveal>
        <div className="product-grid">
          {product.relatedProducts.map((entry, idx) => (
            <ScrollReveal key={entry.watchId} animation="fade-up" delay={50 * (idx % 6)}>
              <ProductTile product={entry} />
            </ScrollReveal>
          ))}
        </div>
      </div>
    </div>
  );
}
