# 08. 庫存整合與高併發

---

## 8.1 庫存整合架構

### 8.1.1 軟連結設計

```
shop_product_sku.inv_item_id  ──────►  inv_item.item_id
                                             │
                                             ▼
                                       inv_stock.item_id
```

**設計原則**：
- SKU 可選擇關聯庫存物品（`inv_item_id`）
- 未關聯時使用 SKU 自身的 `stock_quantity`
- 關聯後扣庫存時同步扣減 `inv_stock`

### 8.1.2 庫存查詢邏輯

```java
public int getAvailableStock(ShopProductSku sku) {
    if (sku.getInvItemId() != null) {
        // 關聯庫存：查詢 inv_stock
        InvStock stock = invStockMapper.selectByItemId(sku.getInvItemId());
        return stock != null ? stock.getAvailableQuantity() : 0;
    } else {
        // 獨立庫存：使用 SKU 自身的庫存
        return sku.getStockQuantity();
    }
}
```

---

## 8.2 高併發庫存扣減

### 8.2.1 問題場景

```
用戶A: 查詢庫存 = 10
用戶B: 查詢庫存 = 10
用戶A: 扣減 5，庫存變為 5
用戶B: 扣減 8，期望庫存 = 2，實際超賣
```

### 8.2.2 解決方案：Redis Lua Script

**原子性庫存預扣**

```java
@Component
@RequiredArgsConstructor
public class StockRedisService {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String STOCK_KEY_PREFIX = "shop:stock:";
    private static final String DEDUCT_STOCK_LUA = """
        local key = KEYS[1]
        local quantity = tonumber(ARGV[1])
        local stock = tonumber(redis.call('GET', key) or '0')
        if stock >= quantity then
            redis.call('DECRBY', key, quantity)
            return 1
        else
            return 0
        end
        """;
    
    private final DefaultRedisScript<Long> deductScript;
    
    @PostConstruct
    public void init() {
        deductScript = new DefaultRedisScript<>();
        deductScript.setScriptText(DEDUCT_STOCK_LUA);
        deductScript.setResultType(Long.class);
    }
    
    /**
     * 預扣庫存
     */
    public boolean deductStock(Long skuId, int quantity) {
        String key = STOCK_KEY_PREFIX + skuId;
        Long result = redisTemplate.execute(deductScript, List.of(key), String.valueOf(quantity));
        return result != null && result == 1;
    }
    
    /**
     * 回滾庫存
     */
    public void rollbackStock(Long skuId, int quantity) {
        String key = STOCK_KEY_PREFIX + skuId;
        redisTemplate.opsForValue().increment(key, quantity);
    }
    
    /**
     * 同步庫存到 Redis
     */
    public void syncStock(Long skuId, int quantity) {
        String key = STOCK_KEY_PREFIX + skuId;
        redisTemplate.opsForValue().set(key, String.valueOf(quantity));
    }
}
```

### 8.2.3 下單流程

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopOrderServiceImpl implements IShopOrderService {
    
    private final StockRedisService stockRedisService;
    private final ShopProductSkuMapper skuMapper;
    private final InvStockMapper invStockMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResult createOrder(OrderCreateDTO dto) {
        List<OrderItem> items = dto.getItems();
        
        // 1. Redis 預扣庫存（原子操作）
        List<Long> deductedSkuIds = new ArrayList<>();
        try {
            for (OrderItem item : items) {
                boolean success = stockRedisService.deductStock(item.getSkuId(), item.getQuantity());
                if (!success) {
                    throw new ServiceException("商品「" + item.getProductName() + "」庫存不足");
                }
                deductedSkuIds.add(item.getSkuId());
            }
            
            // 2. 建立訂單
            ShopOrder order = buildOrder(dto);
            orderMapper.insert(order);
            
            // 3. 建立訂單明細
            saveOrderItems(order, items);
            
            // 4. 扣減資料庫庫存
            for (OrderItem item : items) {
                deductDbStock(item.getSkuId(), item.getQuantity());
            }
            
            // 5. 清空購物車
            cartMapper.deleteByMemberAndSkuIds(dto.getMemberId(), 
                items.stream().map(OrderItem::getSkuId).collect(Collectors.toList()));
            
            return OrderCreateResult.success(order);
            
        } catch (Exception e) {
            // 回滾 Redis 庫存
            for (Long skuId : deductedSkuIds) {
                OrderItem item = findItemBySkuId(items, skuId);
                stockRedisService.rollbackStock(skuId, item.getQuantity());
            }
            throw e;
        }
    }
    
    private void deductDbStock(Long skuId, int quantity) {
        ShopProductSku sku = skuMapper.selectById(skuId);
        
        if (sku.getInvItemId() != null) {
            // 扣減關聯的 inv_stock
            int result = invStockMapper.decreaseAvailableQuantity(sku.getInvItemId(), quantity);
            if (result == 0) {
                throw new ServiceException("庫存扣減失敗");
            }
        } else {
            // 扣減 SKU 獨立庫存
            int result = skuMapper.decreaseStock(skuId, quantity);
            if (result == 0) {
                throw new ServiceException("庫存扣減失敗");
            }
        }
    }
}
```

### 8.2.4 庫存同步機制

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class StockSyncTask {
    
    private final ShopProductSkuMapper skuMapper;
    private final InvStockMapper invStockMapper;
    private final StockRedisService stockRedisService;
    
    /**
     * 定時同步庫存到 Redis
     * 每 5 分鐘執行一次
     */
    @Scheduled(fixedRate = 300000)
    public void syncStockToRedis() {
        log.info("開始同步庫存到 Redis...");
        
        List<ShopProductSku> skuList = skuMapper.selectAllEnabled();
        
        for (ShopProductSku sku : skuList) {
            int stock;
            if (sku.getInvItemId() != null) {
                InvStock invStock = invStockMapper.selectByItemId(sku.getInvItemId());
                stock = invStock != null ? invStock.getAvailableQuantity() : 0;
            } else {
                stock = sku.getStockQuantity();
            }
            stockRedisService.syncStock(sku.getSkuId(), stock);
        }
        
        log.info("庫存同步完成，共同步 {} 個 SKU", skuList.size());
    }
    
    /**
     * 商品上架時同步庫存
     */
    public void syncOnProductOnSale(Long productId) {
        List<ShopProductSku> skuList = skuMapper.selectByProductId(productId);
        for (ShopProductSku sku : skuList) {
            int stock = getAvailableStock(sku);
            stockRedisService.syncStock(sku.getSkuId(), stock);
        }
    }
}
```

---

## 8.3 庫存資料庫扣減（防超賣）

### 8.3.1 inv_stock 表扣減

```xml
<!-- InvStockMapper.xml -->
<update id="decreaseAvailableQuantity">
    UPDATE inv_stock
    SET available_quantity = available_quantity - #{quantity},
        update_time = NOW()
    WHERE item_id = #{itemId}
      AND available_quantity >= #{quantity}
</update>
```

### 8.3.2 shop_product_sku 表扣減

```xml
<!-- ShopProductSkuMapper.xml -->
<update id="decreaseStock">
    UPDATE shop_product_sku
    SET stock_quantity = stock_quantity - #{quantity},
        update_time = NOW()
    WHERE sku_id = #{skuId}
      AND stock_quantity >= #{quantity}
</update>
```

**關鍵**：`WHERE available_quantity >= #{quantity}` 確保不會超賣

---

## 8.4 訂單超時取消

### 8.4.1 延遲佇列方案

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutTask {
    
    private final ShopOrderMapper orderMapper;
    private final StockRedisService stockRedisService;
    private final ShopProductSkuMapper skuMapper;
    private final InvStockMapper invStockMapper;
    
    /**
     * 掃描超時訂單（每分鐘執行）
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        // 查詢超過 30 分鐘未付款的訂單
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<ShopOrder> timeoutOrders = orderMapper.selectPendingOrdersBefore(timeout);
        
        for (ShopOrder order : timeoutOrders) {
            try {
                cancelOrder(order.getOrderId(), "訂單超時自動取消");
            } catch (Exception e) {
                log.error("取消超時訂單失敗，orderId={}", order.getOrderId(), e);
            }
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        ShopOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getStatusEnum() != OrderStatus.PENDING) {
            return;
        }
        
        // 1. 更新訂單狀態
        order.setStatusEnum(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 2. 回滾庫存
        List<ShopOrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (ShopOrderItem item : items) {
            // 回滾 Redis
            stockRedisService.rollbackStock(item.getSkuId(), item.getQuantity());
            
            // 回滾資料庫
            ShopProductSku sku = skuMapper.selectById(item.getSkuId());
            if (sku.getInvItemId() != null) {
                invStockMapper.increaseAvailableQuantity(sku.getInvItemId(), item.getQuantity());
            } else {
                skuMapper.increaseStock(item.getSkuId(), item.getQuantity());
            }
        }
        
        log.info("訂單已取消，orderId={}, reason={}", orderId, reason);
    }
}
```

---

## 8.5 庫存整合服務

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryIntegrationService {
    
    private final InvItemMapper invItemMapper;
    private final InvStockMapper invStockMapper;
    
    /**
     * 取得可關聯的庫存物品列表
     */
    public List<InvItemVO> getAvailableInvItems(String keyword) {
        return invItemMapper.selectAvailableItems(keyword);
    }
    
    /**
     * 檢查庫存物品是否存在
     */
    public boolean checkInvItemExists(Long invItemId) {
        return invItemMapper.existsById(invItemId);
    }
    
    /**
     * 取得庫存物品的可用數量
     */
    public int getInvItemAvailableQuantity(Long invItemId) {
        InvStock stock = invStockMapper.selectByItemId(invItemId);
        return stock != null ? stock.getAvailableQuantity() : 0;
    }
    
    /**
     * 扣減庫存（供訂單服務調用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductInvStock(Long invItemId, int quantity, String orderNo) {
        int result = invStockMapper.decreaseAvailableQuantity(invItemId, quantity);
        if (result == 0) {
            InvItem item = invItemMapper.selectById(invItemId);
            throw new ServiceException("庫存物品「" + item.getItemName() + "」庫存不足");
        }
        log.info("扣減庫存成功，invItemId={}, quantity={}, orderNo={}", invItemId, quantity, orderNo);
    }
    
    /**
     * 回滾庫存（供訂單取消調用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void rollbackInvStock(Long invItemId, int quantity, String orderNo) {
        invStockMapper.increaseAvailableQuantity(invItemId, quantity);
        log.info("回滾庫存成功，invItemId={}, quantity={}, orderNo={}", invItemId, quantity, orderNo);
    }
}
```

---

## 8.6 流程圖

### 下單流程

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  前端   │───►│  Redis  │───►│  建立   │───►│  扣減   │───►│  完成   │
│  下單   │    │  預扣   │    │  訂單   │    │  DB庫存 │    │  訂單   │
└─────────┘    └────┬────┘    └─────────┘    └─────────┘    └─────────┘
                    │
              ┌─────▼─────┐
              │  失敗回滾  │
              │  Redis   │
              └───────────┘
```

### 取消訂單流程

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  取消   │───►│  更新   │───►│  回滾   │───►│  回滾   │
│  請求   │    │  狀態   │    │  Redis  │    │  DB庫存 │
└─────────┘    └─────────┘    └─────────┘    └─────────┘
```
