#!/usr/bin/env bash

# ===========================================
# Tomcat 環境變數設定（VM 環境）
# 路徑: $CATALINA_HOME/bin/setenv.sh
# ===========================================

# Jasypt 加密密碼（必須）
# 請先 export JASYPT_ENCRYPTOR_PASSWORD，詳見部署文件
export JAVA_OPTS="$JAVA_OPTS -Djasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD:?請先設定 JASYPT_ENCRYPTOR_PASSWORD}"

# Spring Profile 設定
export SPRING_PROFILES_ACTIVE=vm

# JVM 記憶體和 GC 設定
CATALINA_OPTS="${CATALINA_OPTS} \
 -Xms2048m \
 -Xmx2048m \
 -XX:MetaspaceSize=256m \
 -XX:MaxMetaspaceSize=512m \
 -XX:+UseG1GC \
 -XX:MaxGCPauseMillis=200 \
 -XX:+HeapDumpOnOutOfMemoryError \
 -XX:HeapDumpPath=/var/log/tomcat/heapdump.hprof \
 -XX:+PrintGCDetails \
 -XX:+PrintGCDateStamps \
 -Xloggc:/var/log/tomcat/gc.log"

export CATALINA_OPTS

# ===========================================
# 說明：
# 資料庫和 Redis 連線資訊已直接寫在 application-vm.yml 中
# 使用 Jasypt 加密，透過上方的密碼進行解密
# ===========================================
