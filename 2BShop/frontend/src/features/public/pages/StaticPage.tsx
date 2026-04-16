import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useI18n } from "@/lib/i18n";

type StaticSection = {
  headingVi?: string;
  headingEn?: string;
  blocksVi: string[];
  blocksEn: string[];
};

type StaticContent = {
  titleVi: string;
  titleEn: string;
  descriptionVi: string;
  descriptionEn: string;
  sections: StaticSection[];
};

const contentMap: Record<string, StaticContent> = {
  about: {
    titleVi: "Về 2BShop | Cửa hàng đồng hồ cao cấp",
    titleEn: "About 2BShop | Premium watch retail",
    descriptionVi: "2BShop tập trung vào sản phẩm chính hãng và trải nghiệm mua sắm gọn gàng, hiện đại và dễ ra quyết định hơn.",
    descriptionEn: "2BShop curates authentic watches across major global brands and presents them through a cleaner, product-first storefront experience.",
    sections: [
      {
        headingVi: "Câu chuyện 2BShop",
        headingEn: "The 2BShop story",
        blocksVi: [
          "2BShop được xây dựng từ đam mê với đồng hồ và mong muốn trình bày sản phẩm theo một cách rõ ràng, hiện đại và cao cấp hơn.",
          "Một chiếc đồng hồ không chỉ để xem giờ. Đó còn là chất liệu, tỷ lệ, kỷ luật thiết kế và dấu ấn cá nhân của người đeo.",
        ],
        blocksEn: [
          "Built from a long-standing passion for timepieces, 2BShop focuses on presenting premium watches with sharper product storytelling and a calmer digital buying experience.",
          "A watch is more than a timekeeping tool. It is material, posture, precision and a personal signal of taste.",
        ],
      },
      {
        headingVi: "Tầm nhìn và sứ mệnh",
        headingEn: "Vision and mission",
        blocksVi: [
          "Tầm nhìn: trở thành điểm đến đáng tin cậy cho người mua đồng hồ chính hãng với trải nghiệm giao diện rõ ràng và gọn gàng.",
          "Sứ mệnh: giữ giao diện tối giản để sản phẩm được nổi bật, trong khi vẫn giữ quy trình mua hàng và hỗ trợ minh bạch.",
        ],
        blocksEn: [
          "Vision: become a trusted premium watch storefront with a modern editorial retail identity.",
          "Mission: keep the interface quiet, let authentic product quality lead, and make every transaction clear and dependable.",
        ],
      },
    ],
  },
  policy: {
    titleVi: "Chính sách | 2BShop",
    titleEn: "Policy | 2BShop",
    descriptionVi: "Tổng hợp chính sách giao hàng, bảo hành và mua sắm đang áp dụng tại 2BShop.",
    descriptionEn: "Key purchase, delivery and warranty policies for ordering watches through 2BShop.",
    sections: [
      {
        headingVi: "Chính sách giao hàng",
        headingEn: "Shipping policy",
        blocksVi: [
          "Đơn hàng từ 500.000đ được miễn phí giao hàng toàn quốc.",
          "Trạng thái giao nhận và theo dõi vận chuyển vẫn được lấy từ quy trình backend hiện tại.",
        ],
        blocksEn: [
          "Orders from 500,000 VND receive free nationwide shipping.",
          "Delivery states and fulfillment tracking still follow the existing backend workflow.",
        ],
      },
      {
        headingVi: "Chính sách bảo hành",
        headingEn: "Warranty policy",
        blocksVi: [
          "Mỗi sản phẩm tiếp tục theo đúng chính sách bảo hành được cấu hình trong hệ thống hiện tại.",
          "Những hao mòn tự nhiên hoặc lỗi sử dụng sai cách nằm ngoài phạm vi bảo hành chuẩn.",
        ],
        blocksEn: [
          "Each product follows the active warranty terms supported by the current backend rules and sales flow.",
          "Damage from improper use or natural wear remains outside standard warranty scope.",
        ],
      },
    ],
  },
  terms: {
    titleVi: "Điều khoản sử dụng | 2BShop",
    titleEn: "Terms of service | 2BShop",
    descriptionVi: "Các điều kiện giao dịch và sử dụng cơ bản trên hệ thống 2BShop.",
    descriptionEn: "Core usage terms and transaction conditions for the 2BShop storefront.",
    sections: [
      {
        headingVi: "Điều kiện chung",
        headingEn: "General terms",
        blocksVi: [
          "2BShop hiển thị thông tin sản phẩm, giá bán và hình ảnh dựa trên dữ liệu đang có trong hệ thống.",
          "Đơn hàng có thể bị từ chối hoặc hủy nếu xảy ra lỗi giá, gian lận hoặc sự cố bất khả kháng.",
        ],
        blocksEn: [
          "2BShop provides product information, pricing and imagery for items currently active on the platform.",
          "Orders may be rejected or canceled when fraud, pricing error or force majeure conditions are detected.",
        ],
      },
      {
        headingVi: "Bảo mật dữ liệu",
        headingEn: "Data protection",
        blocksVi: [
          "Dữ liệu cá nhân và thanh toán vẫn được bảo vệ bởi lớp bảo mật backend hiện tại.",
          "Thông tin khách hàng không được mua bán hay trao đổi cho bên thứ ba.",
        ],
        blocksEn: [
          "Personal and payment data continue to be protected by the existing backend security layer.",
          "Customer information is not sold or exchanged to third parties.",
        ],
      },
    ],
  },
  faq: {
    titleVi: "Câu hỏi thường gặp | 2BShop",
    titleEn: "FAQ | 2BShop",
    descriptionVi: "Những câu hỏi phổ biến về đặt hàng, tài khoản, bảo hành và xác thực sản phẩm.",
    descriptionEn: "Quick answers for ordering, account usage, warranty and watch authenticity at 2BShop.",
    sections: [
      {
        headingVi: "Có cần tài khoản để đặt hàng không?",
        headingEn: "Do I need an account to order?",
        blocksVi: [
          "Có. Tài khoản giúp gắn checkout, theo dõi đơn hàng, cập nhật hồ sơ và lịch sử hỗ trợ vào đúng phiên đăng nhập của bạn.",
        ],
        blocksEn: [
          "Yes. An account keeps checkout, order tracking, profile updates and support history tied to your active session and account state.",
        ],
      },
      {
        headingVi: "Làm sao biết sản phẩm là chính hãng?",
        headingEn: "How do I know the watch is authentic?",
        blocksVi: [
          "Sản phẩm được quản lý qua hệ thống catalog, đơn hàng và quản trị hiện tại, giữ đúng quy trình duyệt và cập nhật sản phẩm đang vận hành.",
        ],
        blocksEn: [
          "Products are presented through the existing catalog and order flow backed by the current admin management system and data rules.",
        ],
      },
    ],
  },
};

export default function StaticPage() {
  const { slug = "about" } = useParams();
  const { tx } = useI18n();
  const content = contentMap[slug] ?? contentMap.about;
  const title = tx(content.titleVi, content.titleEn);
  const description = tx(content.descriptionVi, content.descriptionEn);

  useEffect(() => {
    document.title = title;

    let metaDescription = document.querySelector('meta[name="description"]');
    if (!metaDescription) {
      metaDescription = document.createElement("meta");
      metaDescription.setAttribute("name", "description");
      document.head.appendChild(metaDescription);
    }
    metaDescription.setAttribute("content", description);
  }, [description, title]);

  return (
    <article className="panel editorial-page">
      <header className="editorial-page__header">
        <span className="eyebrow">{tx("Cẩm nang 2BShop", "2BShop guide")}</span>
        <h1>{title.split("|")[0].trim()}</h1>
        <p className="muted-copy">{description}</p>
      </header>

      <main style={{ display: "grid", gap: "24px" }}>
        {content.sections.map((section, index) => (
          <section key={index} className="editorial-page__section">
            {section.headingVi || section.headingEn ? <h2>{tx(section.headingVi || "", section.headingEn || "")}</h2> : null}
            {(tx(section.blocksVi.join("\n"), section.blocksEn.join("\n")).split("\n")).map((block, blockIndex) => (
              <p key={blockIndex} className="muted-copy" style={{ lineHeight: 1.8 }}>
                {block}
              </p>
            ))}
          </section>
        ))}
      </main>
    </article>
  );
}
