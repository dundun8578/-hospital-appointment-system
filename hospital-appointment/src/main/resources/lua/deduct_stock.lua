-- ===================================================
-- Redis库存扣减Lua脚本
-- 功能：原子性地检查库存并扣减
-- 保证Redis操作的原子性，防止超卖
-- ===================================================

-- KEYS[1]: 库存Key，格式：hospital:stock:{scheduleId}
-- ARGV[1]: 要扣减的数量，固定为1

local stockKey = KEYS[1]
local deductCount = tonumber(ARGV[1])

-- 1. 查询当前库存
local currentStock = redis.call('GET', stockKey)

-- 2. 如果库存不存在，返回-1
if currentStock == false then
    return -1
end

currentStock = tonumber(currentStock)

-- 3. 如果库存不足，返回0
if currentStock <= 0 then
    return 0
end

-- 4. 扣减库存
local newStock = currentStock - deductCount

-- 5. 更新库存到Redis
redis.call('SET', stockKey, tostring(newStock))

-- 6. 返回扣减后的库存
return 1
