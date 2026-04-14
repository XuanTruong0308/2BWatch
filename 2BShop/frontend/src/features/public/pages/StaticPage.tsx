import { useEffect } from "react";
import { useParams } from "react-router-dom";

type StaticContent = {
  title: string;
  description: string;
  sections: {
    heading?: string;
    blocks: string[];
  }[];
};

const contentMap: Record<string, StaticContent> = {
  about: {
    title: "Giới thiệu về 2BShop | Hệ thống bán lẻ đồng hồ chính hãng cao cấp",
    description: "2BShop tự hào là đơn vị phân phối các thương hiệu đồng hồ nổi tiếng thế giới: Rolex, Omega, Seiko, Casio chính hãng 100%, bảo hành chuẩn quốc tế, uy tín hàng đầu.",
    sections: [
      {
        heading: "Câu chuyện thương hiệu 2BShop",
        blocks: [
          "Được thành lập với niềm đam mê bất tận dành cho những cỗ máy thời gian, 2BShop đã không ngừng vươn lên thành một trong những điểm đến tin cậy nhất cho những tín đồ yêu thích đồng hồ cao cấp tại Việt Nam.",
          "Chúng tôi hiểu rằng, một chiếc đồng hồ không chỉ là công cụ để xem giờ mà còn là biểu tượng của sự thành đạt, phong cách sống lịch lãm và dấu ấn cá nhân của mỗi người."
        ]
      },
      {
        heading: "Tầm nhìn và Sứ mệnh",
        blocks: [
          "Tầm nhìn: Trở thành chuỗi bán lẻ đồng hồ chính hãng cao cấp số 1 Việt Nam, mang đến trải nghiệm mua sắm sang trọng, tiện lợi và chuẩn mực.",
          "Sứ mệnh: Nói KHÔNG với hàng giả, hàng nhái. 2BShop cam kết 100% sản phẩm phân phối đều là đồng hồ chính hãng (Authentic), nguyên hộp, sổ thẻ cùng chính sách bảo hành chuẩn quốc tế."
        ]
      },
      {
        heading: "Tại sao bạn nên chọn mua đồng hồ tại 2BShop?",
        blocks: [
          "1. Cam kết chính hãng: Hoàn tiền và bồi thường gấp 10 lần nếu phát hiện hàng giả, hàng kém chất lượng.",
          "2. Đa dạng thương hiệu: Từ phân khúc phổ thông đến xa xỉ, hội tụ các thương hiệu đỉnh cao như Rolex, Omega, Orient, Seiko, Casio...",
          "3. Chế độ hậu mãi và bảo hành: Hỗ trợ bảo hành lau dầu, thay pin, kiểm định từ 2 đến 5 năm với quy trình chuyên nghiệp.",
          "4. Mua sắm dễ dàng - Giao hàng nhanh toàn quốc: Miễn phí vận chuyển cho tất cả các đơn hàng từ 500.000đ trở lên, cùng chính sách đổi trả minh bạch."
        ]
      }
    ],
  },
  policy: {
    title: "Chính sách mua hàng & Bảo hành | 2BShop",
    description: "Xem chi tiết chính sách mua hàng, giao nhận, đổi trả và bảo hành đồng hồ chính hãng tại 2BShop. Uy tín, minh bạch và bảo vệ quyền lợi khách hàng tuyệt đối.",
    sections: [
      {
        heading: "1. Chính sách giao hàng",
        blocks: [
          "Tất cả đơn hàng có giá trị từ 500.000đ trở lên sẽ được 2BShop miễn phí vận chuyển toàn quốc.",
          "Đơn hàng được bàn giao qua các đơn vị chuyển phát uy tín, cho phép bạn được quyền kiểm tra hàng trước khi thanh toán (COD)."
        ]
      },
      {
        heading: "2. Chính sách bảo hành",
        blocks: [
          "Bảo hành đầy đủ theo chuẩn của hãng sản xuất và chính sách hậu mãi cộng thêm tại hệ thống 2BShop.",
          "Lưu ý: Không bảo hành cho các trường hợp hao mòn tự nhiên (hư dây da, xước kính) hoặc hư hỏng do lỗi sử dụng sai cách (vào nước bất chính, rơi vỡ mạnh)."
        ]
      }
    ],
  },
  terms: {
    title: "Điều khoản sử dụng dịch vụ | 2BShop",
    description: "Các điều khoản và quy định rõ ràng khi sử dụng các dịch vụ đặt hàng và mua sắm đồng hồ trực tuyến trên website của 2BShop.",
    sections: [
      {
        heading: "Điều kiện giao dịch chung",
        blocks: [
          "Website 2BShop cung cấp thông tin, giá cả, hình ảnh minh họa cho các sản phẩm đồng hồ hiện đang có sẵn trên hệ thống.",
          "Chúng tôi có quyền từ chối hoặc hủy đơn hàng nếu phát hiện có gian lận, sai sót hệ thống về giá cả hoặc các sự cố bất khả kháng."
        ]
      },
      {
        heading: "Bảo mật thông tin",
        blocks: [
          "Mọi thông tin thanh toán, mật khẩu và dữ liệu cá nhân của người dùng được mã hóa qua chuẩn Spring Security cao nhất ở phía máy chủ.",
          "Chúng tôi cam kết tuyệt đối không mua bán, trao đổi dữ liệu cá nhân khách hàng cho bên thứ ba."
        ]
      }
    ],
  },
  faq: {
    title: "Hỏi đáp (FAQ) - Các câu hỏi thường gặp | 2BShop",
    description: "Giải đáp nhanh chóng thắc mắc thường gặp của khách hàng mua đồng hồ tại 2BShop: cách đặt hàng, bảo hành nhanh chóng, đổi trả dễ dàng, thanh toán an toàn.",
    sections: [
      {
        heading: "Tôi có cần tạo tài khoản để mua hàng không?",
        blocks: [
          "Có, hệ thống 2BShop hiện yêu cầu bạn nên tạo một tài khoản để tiện theo dõi tình trạng đơn hàng, bảo hành điện tử và tích lũy ưu đãi riêng."
        ]
      },
      {
        heading: "Làm thế nào để biết đồng hồ tôi mua là chính hãng?",
        blocks: [
          "Tất cả sản phẩm bán ra đều gửi kèm hộp, sổ thẻ bảo hành quốc tế từ nhà sản xuất và thẻ chứng nhận của 2BShop. Bạn có thể tự mình kiểm chứng tại bất kỳ trung tâm thẩm định uy tín nào."
        ]
      }
    ],
  },
};

export default function StaticPage() {
  const { slug = "about" } = useParams();
  const content = contentMap[slug] ?? contentMap.about;

  useEffect(() => {
    // Đẩy thông tin Title SEO động dựa theo Component (Tab trình duyệt)
    document.title = content.title;
    
    // Khởi tạo và cập nhật thẻ Meta Description linh hoạt cho Bot Google đọc (SEO Onpage)
    let metaDescription = document.querySelector('meta[name="description"]');
    if (!metaDescription) {
      metaDescription = document.createElement('meta');
      metaDescription.setAttribute('name', 'description');
      document.head.appendChild(metaDescription);
    }
    metaDescription.setAttribute('content', content.description);
  }, [content]);

  return (
    <article className="panel" style={{ maxWidth: 960, margin: "2rem auto", padding: "2rem", border: "1px solid rgba(0,0,0,0.08)" }}>
      <header style={{ marginBottom: "3rem", textAlign: "center", fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif" }}>
        <span style={{ color: "var(--gold)", fontSize: "0.95rem", fontWeight: 600, letterSpacing: "1.5px", textTransform: "uppercase" }}>Cẩm Nang 2BShop</span>
        <h1 style={{ fontSize: "2.2rem", marginTop: "0.8rem", color: "var(--navy)", fontWeight: 700 }}>{content.title.split("|")[0].trim()}</h1>
        <p className="muted-copy" style={{ fontSize: "1.1rem", marginTop: "1rem", lineHeight: 1.6 }}>{content.description}</p>
      </header>

      <main style={{ display: "flex", flexDirection: "column", gap: "2.5rem" }}>
        {content.sections.map((section, index) => (
          <section key={index} style={{ padding: "0" }}>
            {section.heading && (
              <h2 style={{ fontSize: "1.4rem", marginBottom: "1rem", color: "var(--navy)", paddingBottom: "0.5rem", borderBottom: "1px solid rgba(0,0,0,0.08)" }}>
                {section.heading}
              </h2>
            )}
            <div style={{ display: "flex", flexDirection: "column", gap: "0.8rem" }}>
              {section.blocks.map((block, bIdx) => (
                <p key={bIdx} style={{ lineHeight: 1.7, color: "var(--gray)", fontSize: "1.05rem" }}>{block}</p>
              ))}
            </div>
          </section>
        ))}
      </main>
    </article>
  );
}
