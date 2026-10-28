-- KEYS[1]: 限流 ZSet key
-- ARGV[1]: 当前时间戳（毫秒）
-- ARGV[2]: 窗口大小（毫秒）
-- ARGV[3]: 最大请求数
-- ARGV[4]: 本次请求唯一标识
local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local maxRequests = tonumber(ARGV[3])

redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
local count = redis.call('ZCARD', key)
if count >= maxRequests then
    return 0
end

redis.call('ZADD', key, now, ARGV[4])
redis.call('PEXPIRE', key, window)
return 1
