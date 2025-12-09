#!/usr/bin/env bash
# scripts/run-micro-demo.sh
# Build and run the micro-demo (order-service, user-service, gateway)
# Usage: ./scripts/run-micro-demo.sh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "Building micro-demo jars..."
./gradlew :jai-router-examples:micro-demo:order-service:bootJar \
          :jai-router-examples:micro-demo:user-service:bootJar \
          :jai-router-examples:micro-demo:gateway:bootJar --no-daemon

ORDER_JAR="${ROOT_DIR}/jai-router-examples/micro-demo/order-service/build/libs/order-service-0.0.1-SNAPSHOT.jar"
USER_JAR="${ROOT_DIR}/jai-router-examples/micro-demo/user-service/build/libs/user-service-0.0.1-SNAPSHOT.jar"
GATEWAY_JAR="${ROOT_DIR}/jai-router-examples/micro-demo/gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar"

LOG_ORDER="/tmp/jai_order_service.log"
LOG_USER="/tmp/jai_user_service.log"
LOG_GATEWAY="/tmp/jai_gateway.log"

# Stop any previous instances started by this script (best-effort)
pkill -f "order-service-0.0.1-SNAPSHOT.jar" || true
pkill -f "user-service-0.0.1-SNAPSHOT.jar" || true
pkill -f "gateway-0.0.1-SNAPSHOT.jar" || true
sleep 1

echo "Starting order-service -> ${ORDER_JAR}"
nohup java -jar "${ORDER_JAR}" > "${LOG_ORDER}" 2>&1 &
ORDER_PID=$!

echo "Starting user-service -> ${USER_JAR}"
nohup java -jar "${USER_JAR}" > "${LOG_USER}" 2>&1 &
USER_PID=$!

echo "Starting gateway -> ${GATEWAY_JAR}"
nohup java -jar "${GATEWAY_JAR}" > "${LOG_GATEWAY}" 2>&1 &
GATEWAY_PID=$!

sleep 1

echo "Started services:"
echo "  order-service PID=${ORDER_PID}, log=${LOG_ORDER}"
echo "  user-service  PID=${USER_PID}, log=${LOG_USER}"
echo "  gateway       PID=${GATEWAY_PID}, log=${LOG_GATEWAY}"

echo "Tailing logs (press Ctrl-C to stop)..."
# tail all three logs in one combined stream. You can open separate terminals if you prefer.
exec tail -n +1 -F "$LOG_ORDER" "$LOG_USER" "$LOG_GATEWAY"

