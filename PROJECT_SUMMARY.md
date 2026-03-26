# 项目概要（快速上手与问题记录）

## 一句话说明
这是一个用于学习 Spring Cloud 微服务架构的示例项目，包含：Eureka 服务注册发现、Spring Cloud Gateway、若干业务服务（Order/Payment/Inventory）、RabbitMQ 消息总线与一个静态前端页面。

## 架构图（概念）
- 前端 (Browser) -> API Gateway (8080) -> 各业务服务（Order:8081, Payment:8082, Inventory:8083）
- 服务注册 / 发现：Eureka Server (8761)
- 异步消息：RabbitMQ (exchange `order.exchange`, routing key `order.created`)

## 为什么创建这个架构
- 学习微服务核心概念：服务注册与发现、网关路由、异步消息通信、服务间解耦与容错设计。
- 练习 Spring Cloud 生态（Eureka、Gateway、Spring Boot、Spring Data、Spring AMQP 等）的配置和调试流程。

## 主要组件与位置
- `eureka-server`：服务注册中心（`@EnableEurekaServer`）, 配置在 `eureka-server/src/main/resources/application.yml`。
- `api-gateway`：Spring Cloud Gateway 配置路由（`spring.cloud.gateway.routes` 在 `application.yml`），跨域在 `api-gateway/src/main/java/com/demo/gateway/config/CorsConfig.java`。
- 业务服务：`order-service`、`payment-service`、`inventory-service`（各自为 Spring Boot 应用，作为 Eureka client 注册）。
- `frontend/index.html`：静态前端，默认 `apiBase` 指向 `http://localhost:8080`（即网关）；前端也可直接指向单个服务（例如 `http://localhost:8081`）。

## 关键工作流说明
- 服务注册：各业务服务启动后通过 `eureka.client.service-url.defaultZone` 注册到 Eureka；Gateway 使用 `lb://<service-name>` 通过 DiscoveryClient 查找实例并转发请求。
- 网关转发：不需要自定义 controller；Spring Cloud Gateway 根据 `application.yml` 中的 routes 在启动时自动创建路由并代理请求。
- 异步消息：订单创建后由订单服务发布消息到 RabbitMQ，支付与库存服务按队列消费处理。

## 已遇到的典型问题（与解决办法）
- 500 错误 -> 原因：RabbitMQ 上已存在 `order.exchange`，类型为 `fanout`，而应用期望是 `topic`，声明时触发 `PRECONDITION_FAILED` 导致异常。  
  解决：删除 Broker 上的旧 exchange（或在代码中将 exchange 类型与 Broker 保持一致，或设置 `RabbitAdmin#setIgnoreDeclarationExceptions(true)`）。
- 看似“前端直接通过 8080 静态文件访问”，但这并不说明 Gateway 无用：前端的 `apiBase` 决定请求会走网关还是直连后端。
- Gateway 没有显式代码转发逻辑，因为转发由 Spring Cloud Gateway 的自动装配和 `application.yml` 路由配置完成。
- Eureka Server 只有一个启动类是正常的：Eureka 的注册/发现功能由 Spring Cloud 实现，服务器端只需 `@EnableEurekaServer` 与少量配置。

## 遇到的迷惑点（为后续阅读者列出）
- 为什么 `api-gateway` 没有看到显式的转发代码？（答：路由配置 + Gateway 自动装配）
- 如果前端可以直接访问后端，网关还有何作用？（答：统一入口、跨域、认证、路由规则、熔断限流等；本 demo 使用网关做路由与 CORS）
- Eureka 没有额外代码为什么能工作？（答：服务注册/发现的逻辑已由 Spring Cloud 提供；Eureka Server 启动即提供 registry）

## 给后续 AI / 开发者的快速检查清单
- 启动顺序建议：RabbitMQ -> Eureka Server -> 各业务服务 (order/payment/inventory) -> API Gateway -> 前端。  
- 若出现 500 且堆栈指向 RabbitTemplate / exchange，先检查 RabbitMQ 上是否已有相同名称但类型不同的 exchange。  
- 确认前端 `apiBase`（`frontend/index.html`）指向的是网关还是某个服务。  
- 在 Gateway 中查看 `application.yml` 的 routes 来了解哪些路径被转发。

## 后续建议（短）
- 在生产或长期实验环境，避免自动在 Broker 上声明与已有资源不一致的交换器；使用版本化名称或运维管控变更。  
- 增加更严格的输入校验与错误处理（示例已在 `order-service` 中临时加入），并考虑将消息发送转为异步/重试策略与断路器保护。  
