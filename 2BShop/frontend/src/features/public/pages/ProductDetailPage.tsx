import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useParams } from "react-router-dom";
import { getJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";
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
  const { tx } = useI18n();

  const query = useQuery({
    queryKey: ["product", id],
    queryFn: async () => {
      const response = await getJson<ApiResponse<ProductDetail>>(`/api/v1/public/watches/${id}`);
      return response.data;
    },
  });

  if (query.isLoading) {
    return <LoadingScreen label={tx("Đang tải chi tiết sản phẩm...", "Loading product details...")} />;
  }

  if (query.isError || !query.data) {
    return <ErrorState message={tx("Không thể tải sản phẩm nay.", "We could not load this product.")} />;
  }

  const product = query.data;
  const image = product.images[activeImage]?.url || product.images[0]?.url;
  const productId = Number(id);
  const isOutOfStock = (product.stockQuantity ?? 0) <= 0;

  return (
    <div className="split-grid">
      <ScrollReveal animation="slide-right" className="panel">
        <div className="product-cover" style={{ aspectRatio: "4 / 5", marginBottom: 18 }}>
          <img alt={product.watchName} src={image} />
        </div>

        <div className="product-detail-gallery" role="list" aria-label={tx("Bộ sưu tập ảnh sản phẩm", "Product image gallery")}>
          {product.images.map((entry, index) => (
            <button
              key={entry.id}
              type="button"
              className={`product-detail-gallery__thumb ${activeImage === index ? "is-active" : ""}`}
              onClick={() => setActiveImage(index)}
              aria-pressed={activeImage === index}
              aria-label={tx("Xem ảnh", "View image") + ` ${index + 1}`}
            >
              <img alt={`${product.watchName} ${index + 1}`} src={entry.url} />
            </button>
          ))}
        </div>
      </ScrollReveal>

      <ScrollReveal animation="slide-left" delay={140} className="panel">
        <span className="eyebrow">{product.brandName}</span>
        <h1 style={{ marginTop: 10 }}>{product.watchName}</h1>

        <div className="price-stack" style={{ marginTop: 16 }}>
          <span className="price-main">{formatCurrency(product.priceAfterDiscount)}</span>
          {product.discountPercent ? <span className="price-old">{formatCurrency(product.price)}</span> : null}
        </div>

        <div className="header-actions" style={{ justifyContent: "flex-start", flexWrap: "wrap", marginTop: 18 }}>
          <Badge label={isOutOfStock ? tx("Hết hàng", "Sold out") : tx("Còn hàng", "In stock")} />
          <Badge label={`${tx("Da ban", "Sold")} ${product.soldCount ?? 0}`} tone="info" />
          {product.discountPercent ? <Badge label={`${product.discountPercent}% ${tx("giam", "off")}`} tone="warning" /> : null}
        </div>

        <p className="muted-copy" style={{ marginTop: 18, lineHeight: 1.8 }}>
          {product.description}
        </p>

        <div className="info-grid" style={{ margin: "24px 0" }}>
          <div className="metric-card">
            <span className="eyebrow">{tx("Thương hiệu", "Brand")}</span>
            <strong>{product.brandName}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">{tx("Danh muc", "Category")}</span>
            <strong>{product.categoryName}</strong>
          </div>
        </div>

        <div className="quantity-stepper" style={{ justifyContent: "flex-start", marginBottom: 20 }}>
          <button className="button button-subtle" onClick={() => setQuantity((value) => Math.max(1, value - 1))} type="button">
            -
          </button>
          <span style={{ minWidth: 20, textAlign: "center", fontWeight: 700 }}>{quantity}</span>
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
          {addToCart.isAdding(productId)
            ? tx("Đang xử lý...", "Processing...")
            : isOutOfStock
              ? tx("Hết hàng", "Sold out")
              : tx("Thêm vào giỏ", "Add to cart")}
        </button>

        {addToCart.feedback ? (
          <div className={`inline-alert inline-alert-${addToCart.feedback.tone}`} style={{ marginTop: 16 }}>
            {addToCart.feedback.message}
          </div>
        ) : null}
      </ScrollReveal>

      <div className="panel" style={{ gridColumn: "1 / -1" }}>
        <ScrollReveal animation="fade-down">
          <div className="section-heading" style={{ marginBottom: 20 }}>
            <div>
              <span className="eyebrow">{tx("Sản phẩm lien quan", "Related edit")}</span>
              <h2>{tx("Sản phẩm lien quan", "Related products")}</h2>
            </div>
          </div>
        </ScrollReveal>

        <div className="product-grid">
          {product.relatedProducts.map((entry, idx) => (
            <ScrollReveal key={entry.watchId} animation="fade-up" delay={40 * (idx % 6)}>
              <ProductTile product={entry} />
            </ScrollReveal>
          ))}
        </div>
      </div>
    </div>
  );
}
