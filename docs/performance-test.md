# 性能测试记录

项目提供 `jmeter/scripts/appointment_test_plan.jmx`。简历中的并发数、P95、缓存命中率和 Lua 耗时必须以实测数据为准。

## 测试前提

1. 启动 MySQL、Redis、RabbitMQ 和全部后端服务。
2. 准备足量排班号源与测试用户，避免业务库存不足影响吞吐统计。
3. 预热医院、医生、排班查询接口后再开始正式采样。
4. 记录机器配置、JDK 参数、数据库连接池配置和测试日期。

## 建议执行

```bash
jmeter -n -t jmeter/scripts/appointment_test_plan.jmx \
  -l jmeter/results/appointment.jtl \
  -e -o jmeter/results/report
```

## 结果表

| 场景 | 并发用户 | 吞吐量 | P95 | 错误率 | 备注 |
|---|---:|---:|---:|---:|---|
| 优化前 | 待测试 | 待测试 | 待测试 | 待测试 | 保留基线提交 |
| 优化后 | 待测试 | 待测试 | 待测试 | 待测试 | Lua、缓存、Outbox版本 |

Redis 缓存命中率可结合 `INFO stats` 中的 `keyspace_hits` 与 `keyspace_misses` 计算；Lua 耗时应通过应用指标或压测采样记录，不应凭代码推断。
