import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postFormData } from "@/lib/api/client";
import type { ApiResponse, ProductDetail, WatchOptionsPayload } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage } from "@/lib/utils/format";

type WatchValues = {
  watchName: string;
  description: string;
  price: string;
  discountPercent: string;
  stockQuantity: string;
  brandId: string;
  categoryId: string;
  isActive: boolean;
  mainImage: FileList;
  galleryImages: FileList;
};

export default function WatchFormPage() {
  const { tx } = useI18n();
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const form = useForm<WatchValues>({
    defaultValues: {
      watchName: "",
      description: "",
      price: "",
      discountPercent: "0",
      stockQuantity: "0",
      brandId: "",
      categoryId: "",
      isActive: true,
    },
  });

  const optionsQuery = useQuery({
    queryKey: ["admin", "watch-options"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<WatchOptionsPayload>>("/api/v1/admin/watches/options");
      return response.data;
    },
  });

  const detailQuery = useQuery({
    queryKey: ["admin", "watch", id],
    enabled: editing,
    queryFn: async () => {
      const response = await getJson<ApiResponse<ProductDetail>>(`/api/v1/admin/watches/${id}`);
      return response.data;
    },
  });

  useEffect(() => {
    if (!detailQuery.data) {
      return;
    }
    form.reset({
      watchName: detailQuery.data.watchName,
      description: detailQuery.data.description || "",
      price: String(detailQuery.data.price),
      discountPercent: String(detailQuery.data.discountPercent || 0),
      stockQuantity: String(detailQuery.data.stockQuantity || 0),
      brandId: String(detailQuery.data.brandId || ""),
      categoryId: String(detailQuery.data.categoryId || ""),
      isActive: Boolean(detailQuery.data.active),
    });
  }, [detailQuery.data, form]);

  const mutation = useMutation({
    mutationFn: async (values: WatchValues) => {
      const formData = new FormData();
      formData.append("watchName", values.watchName);
      formData.append("description", values.description);
      formData.append("price", values.price);
      formData.append("discountPercent", values.discountPercent);
      formData.append("stockQuantity", values.stockQuantity);
      formData.append("brandId", values.brandId);
      formData.append("categoryId", values.categoryId);
      formData.append("isActive", String(values.isActive));
      if (values.mainImage?.[0]) {
        formData.append("mainImage", values.mainImage[0]);
      }
      Array.from(values.galleryImages || []).forEach((file) => {
        formData.append("galleryImages", file);
      });
      return postFormData(editing ? `/api/v1/admin/watches/${id}` : "/api/v1/admin/watches", formData);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "watches"] });
      navigate("/admin/watches");
    },
  });

  if (optionsQuery.isLoading || detailQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải form sản phẩm...", "Loading product form...")} />;
  }

  if (optionsQuery.isError || !optionsQuery.data || detailQuery.isError) {
    return <ErrorState message={tx("Không thể tải dữ liệu form sản phẩm.", "Could not load product form data.")} />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Form sản phẩm", "Watch form")}</span>
          <h2>{editing ? tx("Cập nhật sản phẩm", "Update product") : tx("Tạo sản phẩm mới", "Create new product")}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/watches">
          {tx("Quay lại danh sách", "Back to list")}
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="watchName">{tx("Tên sản phẩm", "Product name")}</label>
          <input className="field" id="watchName" {...form.register("watchName", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="price">{tx("Giá gốc", "Base price")}</label>
          <input className="field" id="price" type="number" {...form.register("price", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="discountPercent">{tx("Giảm giá %", "Discount %")}</label>
          <input className="field" id="discountPercent" type="number" {...form.register("discountPercent")} />
        </div>
        <div className="field-group">
          <label htmlFor="stockQuantity">{tx("Tồn kho", "Stock quantity")}</label>
          <input className="field" id="stockQuantity" type="number" {...form.register("stockQuantity", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="brandId">{tx("Thương hiệu", "Brand")}</label>
          <select className="select" id="brandId" {...form.register("brandId", { required: true })}>
            <option value="">{tx("Chọn thương hiệu", "Select brand")}</option>
            {optionsQuery.data.brands.map((brand) => (
              <option key={brand.brandId} value={brand.brandId}>
                {brand.brandName}
              </option>
            ))}
          </select>
        </div>
        <div className="field-group">
          <label htmlFor="categoryId">{tx("Danh mục", "Category")}</label>
          <select className="select" id="categoryId" {...form.register("categoryId", { required: true })}>
            <option value="">{tx("Chọn danh mục", "Select category")}</option>
            {optionsQuery.data.categories.map((category) => (
              <option key={category.categoryId} value={category.categoryId}>
                {category.categoryName}
              </option>
            ))}
          </select>
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label htmlFor="description">{tx("Mô tả", "Description")}</label>
          <textarea className="textarea" id="description" rows={5} {...form.register("description")} />
        </div>
        <div className="field-group">
          <label htmlFor="mainImage">{tx("Ảnh chính", "Main image")}</label>
          <input className="field" id="mainImage" type="file" {...form.register("mainImage")} />
        </div>
        <div className="field-group">
          <label htmlFor="galleryImages">{tx("Bộ sưu tập ảnh", "Gallery images")}</label>
          <input className="field" id="galleryImages" multiple type="file" {...form.register("galleryImages")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("isActive")} />
            <span>{tx("Sản phẩm đang hoạt động", "Product is active")}</span>
          </label>
        </div>

        {detailQuery.data?.images?.length ? (
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label>{tx("Ảnh hiện tại", "Current images")}</label>
            <div className="mini-gallery">
              {detailQuery.data.images.map((image) => (
                <img alt={detailQuery.data.watchName} key={image.id} src={image.url} />
              ))}
            </div>
          </div>
        ) : null}

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending
              ? tx("Đang lưu...", "Saving...")
              : editing
                ? tx("Cập nhật sản phẩm", "Update product")
                : tx("Tạo sản phẩm", "Create product")}
          </button>
        </div>
      </form>
    </div>
  );
}
