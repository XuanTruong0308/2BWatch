import { useParams } from "react-router-dom";

const contentMap: Record<string, { title: string; description: string; blocks: string[] }> = {
  about: {
    title: "Về 2BShop",
    description: "Một giao diện mới nhưng vẫn giữ đúng tinh thần thương hiệu navy - gold quen thuộc.",
    blocks: [
      "2BShop được xây dựng như một không gian chọn đồng hồ cao cấp: tập trung vào sự tin cậy, độ rõ ràng và cảm giác mua sắm tinh tế.",
      "Ở phiên bản React mới, toàn bộ storefront được tái tổ chức để sản phẩm nổi bật hơn, phần điều hướng rõ hơn và thao tác mua hàng mượt hơn.",
    ],
  },
  policy: {
    title: "Chính sách",
    description: "Các nguyên tắc mua sắm, đổi trả và chăm sóc khách hàng được trình bày rõ ràng hơn.",
    blocks: [
      "Đơn hàng từ 500.000đ được miễn phí vận chuyển theo logic hiện hành của hệ thống.",
      "Sản phẩm được bảo hành theo chính sách của từng thương hiệu và điều kiện mua hàng hiện có trên backend.",
    ],
  },
  terms: {
    title: "Điều khoản sử dụng",
    description: "Mọi giao dịch trên 2BShop đều tuân theo điều khoản minh bạch, dễ tra cứu.",
    blocks: [
      "Thông tin giá, khuyến mãi và trạng thái tồn kho được lấy trực tiếp từ hệ thống backend hiện tại.",
      "Việc thanh toán, xác thực và phân quyền tài khoản tiếp tục dùng logic bảo mật Spring Security hiện hữu.",
    ],
  },
  faq: {
    title: "Câu hỏi thường gặp",
    description: "Một vài câu hỏi phổ biến để người dùng mới không bị chững lại khi mua hàng.",
    blocks: [
      "Tôi có cần đăng nhập để thêm giỏ hàng không? Có, hệ thống hiện tại yêu cầu đăng nhập để quản lý giỏ hàng cá nhân.",
      "Khi nào được miễn phí giao hàng? Khi tổng giá trị đơn đã chọn từ 500.000đ trở lên.",
    ],
  },
};

export default function StaticPage() {
  const { slug = "about" } = useParams();
  const content = contentMap[slug] ?? contentMap.about;

  return (
    <div className="panel">
      <span className="eyebrow">2BShop Journal</span>
      <h1>{content.title}</h1>
      <p className="muted-copy">{content.description}</p>
      <div className="panel" style={{ marginTop: 20 }}>
        {content.blocks.map((block) => (
          <p key={block}>{block}</p>
        ))}
      </div>
    </div>
  );
}
