#!/bin/bash
# Redis Session Key 迁移脚本
# 用途: sa-token 上线后清空旧 Spring Session 数据,确保所有用户重新登录
#
# 环境变量:
#   REDIS_HOST  Redis 主机 (默认 127.0.0.1)
#   REDIS_PORT  Redis 端口 (默认 6379)
#   REDIS_PASS  Redis 密码 (默认空)
#
# 用法示例:
#   bash scripts/migrate-redis-session.sh
#   REDIS_HOST=8.137.86.224 REDIS_PASS=169832 bash scripts/migrate-redis-session.sh

set -e

REDIS_HOST=${REDIS_HOST:-127.0.0.1}
REDIS_PORT=${REDIS_PORT:-6379}
REDIS_PASS=${REDIS_PASS:-}

AUTH_PARAM=""
if [ -n "$REDIS_PASS" ]; then
    AUTH_PARAM="-a $REDIS_PASS --no-auth-warning"
fi

REDIS_CLI="redis-cli -h $REDIS_HOST -p $REDIS_PORT $AUTH_PARAM"

echo "==> 目标 Redis: $REDIS_HOST:$REDIS_PORT"

# 1. 备份旧 Session Key
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
BACKUP_FILE="backup-spring-session-${TIMESTAMP}.txt"
echo "==> 备份旧 Session Key 到 $BACKUP_FILE ..."
$REDIS_CLI --scan --pattern "renzs-blog:session:*" > "$BACKUP_FILE" || true
OLD_KEY_COUNT=$(wc -l < "$BACKUP_FILE" | tr -d ' ')
echo "    旧 Session Key 数量: $OLD_KEY_COUNT"

# 2. 删除旧 Session Key
if [ "$OLD_KEY_COUNT" -gt 0 ]; then
    echo "==> 删除旧 Session Key ..."
    $REDIS_CLI --scan --pattern "renzs-blog:session:*" | xargs -L 100 $REDIS_CLI DEL > /dev/null
else
    echo "==> 没有旧 Session Key 需要删除"
fi

# 3. 验证清理结果
REMAINING=$($REDIS_CLI --scan --pattern "renzs-blog:session:*" | wc -l | tr -d ' ')
echo "==> 剩余旧 Session Key: $REMAINING (预期 0)"

# 4. 查看当前 sa-token Key 状态
SATOKEN_COUNT=$($REDIS_CLI --scan --pattern "satoken:*" | wc -l | tr -d ' ')
echo "==> 当前 sa-token Key 数量: $SATOKEN_COUNT (用户登录后会逐步增加)"

if [ "$REMAINING" -eq 0 ]; then
    echo ""
    echo "✓ 迁移完成。所有用户需要重新登录。"
    echo "  备份文件: $BACKUP_FILE"
else
    echo ""
    echo "✗ 警告: 仍有 $REMAINING 个旧 Key 未删除,请检查权限或重试"
    exit 1
fi
