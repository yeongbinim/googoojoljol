package googoo.joljol.shopping_mall.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shopping_mall")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingMall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // 상호 (필수)

    @Column(name = "mall_name")
    private String mallName; // 쇼핑몰명

    @Column(name = "domain", nullable = false)
    private String domain; // 도메인명 (필수, 유니크)

    @Column(name = "phone", nullable = false)
    private String phone; // 전화번호 (필수)

    @Column(name = "operator_email", nullable = false)
    private String operatorEmail; // 운영자 이메일 (필수, 유니크)

    @Column(name = "business_number")
    private String businessNumber; // 통신판매번호 (not unique)

    @Column(name = "business_type")
    private String businessType; // 영업형태

    @Column(name = "first_report_date")
    private LocalDate firstReportDate; // 최초 신고일자

    @Column(name = "company_address")
    private String companyAddress; // 회사주소

    @Column(name = "business_status", nullable = false)
    private String businessStatus; // 업소 상태 (필수)

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating; // 전체평가 (0~3, 필수)

    @Column(name = "business_info_rating")
    private Integer businessInfoRating; // 사업자정보표시평가

    @Column(name = "cancellation_policy_rating")
    private Integer cancellationPolicyRating; // 청약철회평가

    @Column(name = "payment_method_rating")
    private Integer paymentMethodRating; // 결제방법평가

    @Column(name = "terms_of_service_rating")
    private Integer termsOfServiceRating; // 이용약관평가

    @Column(name = "privacy_security_rating")
    private Integer privacySecurityRating; // 개인정보보안평가

    @Column(name = "main_products")
    private String mainProducts; // 주요 취급 품목

    @Column(name = "cancellation_availability")
    private String cancellationAvailability; // 청약철회 가능 여부

    @Column(name = "required_display_items")
    private String requiredDisplayItems; // 초기화면 필수항목 표시 사항

    @Column(name = "payment_methods")
    private String paymentMethods; // 결제방법

    @Column(name = "terms_of_service_compliance")
    private String termsOfServiceCompliance; // 이용약관 준수 정도

    @Column(name = "privacy_policy")
    private String privacyPolicy; // 개인정보 취급방침

    @Column(name = "extra_privacy_info_request")
    private String extraPrivacyInfoRequest; // 표준약관 이상 개인정보 항목 요구

    @Column(name = "purchase_safety_service")
    private String purchaseSafetyService; // 구매 안전 서비스

    @Column(name = "security_server_installation")
    private String securityServerInstallation; // 보안 서버 설치

    @Column(name = "certification_mark")
    private String certificationMark; // 인증 마크

    @Column(name = "delivery_estimated_display")
    private String deliveryEstimatedDisplay; // 배송 예정일 표시

    @Column(name = "return_shipping_cost_policy")
    private String returnShippingCostPolicy; // 철회 시 배송비 부담 여부

    @Column(name = "customer_complaint_board")
    private String customerComplaintBoard; // 고객 불만 게시판 운영

    @Column(name = "membership_withdrawal")
    private String membershipWithdrawal; // 회원 탈퇴 방법

    @Column(name = "site_establishment_year")
    private String siteEstablishmentYear; // 사이트 개설 연도

    @Column(name = "monitoring_date", nullable = false)
    private LocalDate monitoringDate; // 모니터링 날짜 (필수)
}
