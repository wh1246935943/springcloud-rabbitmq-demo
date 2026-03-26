# Spring Cloud 微服务 + RabbitMQ Demo

## 项目架构

```
前端页面 (Browser)
    ↓ HTTP
API Gateway (8080)  ←→  Eureka Server (8761)
    ├── /api/orders/**     → Order Service (8081)
    ├── /api/payments/**   → Payment Service (8082)
    └── /api/inventory/**  → Inventory Service (8083)

消息流 (RabbitMQ):
Order Service ──[order.exchange / order.created]──► payment.queue ──► Payment Service
                                                 └──► inventory.queue ──► Inventory Service
```

## 技术栈

| 组件 | 技术 |
|------|------|
| 服务发现 | Spring Cloud Netflix Eureka |
| API 网关 | Spring Cloud Gateway (WebFlux) |
| 消息队列 | Spring AMQP + RabbitMQ |
| 持久化 | Spring Data JPA + H2 (内存数据库) |
| 服务框架 | Spring Boot 3.2.3 |
| Spring Cloud | 2023.0.0 |

## 快速启动

### 前提条件
- Java 17+
- Maven 3.6+
- RabbitMQ（推荐 Docker 方式）

### 第1步：启动 RabbitMQ

```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

> 管理界面：http://localhost:15672  用户名/密码：guest/guest

### 第2步：编译项目

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

### 第3步：按顺序启动服务

**终端1 - Eureka Server（必须第一个启动）**
```bash
cd eureka-server
mvn spring-boot:run
# 访问 http://localhost:8761 查看注册中心
```

**终端2 - Order Service**
```bash
cd order-service
mvn spring-boot:run
```

**终端3 - Payment Service**
```bash
cd payment-service
mvn spring-boot:run
```

**终端4 - Inventory Service**
```bash
cd inventory-service
mvn spring-boot:run
```

**终端5 - API Gateway（最后启动）**
```bash
cd api-gateway
mvn spring-boot:run
```

### 第4步：打开前端页面

直接用浏览器打开 `frontend/index.html`

> 默认连接 http://localhost:8080（API Gateway），可在页面顶部修改地址

## 端口一览

| 服务 | 端口 | 说明 |
|------|------|------|
| Eureka Server | 8761 | 服务注册中心 |
| API Gateway | 8080 | 统一入口 |
| Order Service | 8081 | 订单服务 |
| Payment Service | 8082 | 支付服务 |
| Inventory Service | 8083 | 库存服务 |
| RabbitMQ AMQP | 5672 | 消息代理 |
| RabbitMQ 管理 | 15672 | Web 管理界面 |

## API 接口

### 订单服务 (通过 Gateway: /api/orders)
- `POST /api/orders` - 创建订单
- `GET /api/orders` - 获取所有订单
- `GET /api/orders/{id}` - 获取单个订单

### 支付服务 (通过 Gateway: /api/payments)
- `GET /api/payments` - 获取所有支付记录
- `GET /api/payments/order/{orderId}` - 按订单ID查询

### 库存服务 (通过 Gateway: /api/inventory)
- `GET /api/inventory` - 获取所有商品
- `GET /api/inventory/{id}` - 获取单个商品

## 消息流说明

1. 用户在前端页面选择商品并下单
2. **订单服务**保存订单（状态: `PENDING`），并发布消息到 `order.exchange`
3. **支付服务**监听 `payment.queue`，处理支付（模拟 500ms），创建支付记录（状态: `PAID`）
4. **库存服务**监听 `inventory.queue`，自动扣减对应商品的库存
5. 前端每 3 秒自动刷新订单、支付、库存数据

## H2 控制台（调试用）

- 订单服务：http://localhost:8081/h2-console（JDBC URL: `jdbc:h2:mem:orderdb`）
- 支付服务：http://localhost:8082/h2-console（JDBC URL: `jdbc:h2:mem:paymentdb`）
- 库存服务：http://localhost:8083/h2-console（JDBC URL: `jdbc:h2:mem:inventorydb`）
