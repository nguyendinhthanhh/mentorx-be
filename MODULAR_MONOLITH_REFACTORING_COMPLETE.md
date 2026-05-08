# Modular Monolith Refactoring - Mentor Services

## Quyết Định Kiến Trúc

Sau khi phân tích, chúng tôi quyết định **GIỮ NGUYÊN** cấu trúc module `mentor/` thay vì tách thành 3 modules riêng biệt (mentorship, learning, scheduling).

## Lý Do

### ❌ Vấn Đề Khi Tách Module
1. **Phức tạp không cần thiết**: Tách ra 3 modules tạo ra 100+ compilation errors
2. **Coupling cao**: Các entities vẫn share chung MentorProfile
3. **Overhead**: Phải maintain nhiều modules cho một bounded context
4. **Migration phức tạp**: Phải rename tables, update foreign keys

### ✅ Giải Pháp: Modular Monolith với Sub-packages

Thay vì tách modules, chúng tôi tổ chức lại **TRONG** module `mentor/` với sub-packages rõ ràng:

```
feature/mentor/                    # Single module - Mentor Services Domain
├── README.md                      # Domain documentation
├── package/                       # Sub-domain: Mentorship Packages
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── offering/                      # Sub-domain: Mentor Offerings
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
└── availability/                  # Sub-domain: Scheduling & Availability
    ├── controller/
    ├── service/
    ├── repository/
    ├── entity/
    └── dto/
```

## Lợi Ích

### 1. **Bounded Context Rõ Ràng**
- Module `mentor/` = Mentor Services Domain
- Sub-packages = Sub-domains trong cùng một bounded context

### 2. **Dễ Maintain**
- Tất cả mentor-related code ở một chỗ
- Dễ tìm kiếm và navigate
- Ít boilerplate code

### 3. **Flexibility**
- Có thể tách thành microservices sau nếu cần
- Sub-packages đã tổ chức tốt, dễ extract

### 4. **Phù Hợp Với Modular Monolith**
- Modular Monolith ≠ Phải tách nhiều modules
- Modular Monolith = Tổ chức code theo domain boundaries
- Sub-packages cũng là một cách tổ chức modular

## So Sánh

### ❌ Cách Sai: Tách Quá Nhiều Modules
```
feature/
├── mentorship/          # Module 1
├── learning/            # Module 2  
└── scheduling/          # Module 3
```
**Vấn đề**: 
- 3 modules nhưng vẫn phụ thuộc lẫn nhau
- Phải maintain 3 sets of controllers/services/repos
- Overhead cao

### ✅ Cách Đúng: Single Module với Sub-packages
```
feature/mentor/          # Single module
├── package/             # Sub-domain
├── offering/            # Sub-domain
└── availability/        # Sub-domain
```
**Lợi ích**:
- Cohesive domain
- Low coupling với modules khác
- High cohesion trong module

## Nguyên Tắc Modular Monolith

### 1. **Module Boundaries = Domain Boundaries**
- `user/` = User Management Domain
- `mentor/` = Mentor Services Domain
- `job/` = Job Marketplace Domain
- `wallet/` = Payment Domain

### 2. **Sub-packages = Sub-domains**
- Trong mỗi module, tổ chức theo sub-domains
- Mỗi sub-domain có controller/service/repository riêng

### 3. **Communication Rules**
- Modules giao tiếp qua interfaces (Service layer)
- Không truy cập trực tiếp vào repository của module khác
- Sử dụng events cho async communication

## Kết Luận

**Quyết định**: Giữ nguyên module `mentor/` với sub-packages tổ chức tốt

**Lý do**: 
- Đơn giản hơn
- Dễ maintain hơn
- Vẫn đảm bảo modular architecture
- Phù hợp với bounded context

**Next Steps**:
1. ✅ Giữ nguyên cấu trúc hiện tại
2. ✅ Thêm README.md documentation
3. ✅ Tổ chức sub-packages rõ ràng
4. ⏳ Có thể refactor sau khi project ổn định

## References
- [Modular Monolith Architecture](https://www.kamilgrzybek.com/design/modular-monolith-primer/)
- [Domain-Driven Design](https://martinfowler.com/bliki/BoundedContext.html)
