import { Link } from "react-router-dom";
import type { ProductCard } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { formatCurrency } from "@/lib/utils/format";
import { Badge } from "@/components/ui/Badge";

type ProductTileProps = {
  product: ProductCard;
  action?: React.ReactNode;
};

export function ProductTile({ product, action }: ProductTileProps) {
  const { tx } = useI18n();

  return (
    <article className="product-card">
      <Link className="product-cover" to={`/watches/${product.watchId}`}>
        <img
          alt={product.watchName}
          loading="lazy"
          src={
            product.imageUrl ||
            "https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=900&q=80"
          }
        />
      </Link>

      <div className="product-card__body">
        <div className="product-card__meta">
          <span className="eyebrow">{product.brandName || "2BShop"}</span>
          {product.discountPercent ? <Badge label={`-${product.discountPercent}%`} tone="warning" /> : null}
        </div>

        <div className="product-card__copy">
          <h3>{product.watchName}</h3>
          <p className="muted-copy">
            {product.description?.slice(0, 82) ||
              tx("Thiet ke đồng hồ premium cho phong cach hiện đại.", "Premium watch design for a modern lifestyle.")}
          </p>
        </div>

        <div className="price-stack">
          <span className="price-main">{formatCurrency(product.priceAfterDiscount)}</span>
          {product.discountPercent ? <span className="price-old">{formatCurrency(product.price)}</span> : null}
        </div>

        <div className="header-actions" style={{ justifyContent: "space-between" }}>
          <Badge label={product.stockQuantity && product.stockQuantity > 0 ? tx("Còn hàng", "In stock") : tx("Hết hàng", "Sold out")} />
        </div>

        {action ? <div className="product-card__action">{action}</div> : null}
      </div>
    </article>
  );
}
