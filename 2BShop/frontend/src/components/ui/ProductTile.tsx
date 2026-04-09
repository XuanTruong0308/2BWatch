import { Link } from "react-router-dom";
import type { ProductCard } from "@/lib/api/types";
import { formatCurrency } from "@/lib/utils/format";
import { Badge } from "@/components/ui/Badge";

type ProductTileProps = {
  product: ProductCard;
  action?: React.ReactNode;
};

export function ProductTile({ product, action }: ProductTileProps) {
  return (
    <article className="product-card">
      <Link className="product-cover" to={`/watches/${product.watchId}`}>
        <img
          alt={product.watchName}
          src={product.imageUrl || "https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=900&q=80"}
        />
      </Link>
      <div className="eyebrow">{product.brandName || "2BShop"}</div>
      <div>
        <h3>{product.watchName}</h3>
        <p className="muted-copy">{product.description?.slice(0, 82) || "Thiết kế chuẩn premium cho mọi dịp."}</p>
      </div>
      <div className="price-stack">
        <span className="price-main">{formatCurrency(product.priceAfterDiscount)}</span>
        {product.discountPercent ? <span className="price-old">{formatCurrency(product.price)}</span> : null}
      </div>
      <div className="header-actions" style={{ justifyContent: "space-between" }}>
        <Badge label={product.stockQuantity && product.stockQuantity > 0 ? "Còn hàng" : "Hết hàng"} />
        {product.discountPercent ? <Badge label={`-${product.discountPercent}%`} tone="warning" /> : null}
      </div>
      {action}
    </article>
  );
}
