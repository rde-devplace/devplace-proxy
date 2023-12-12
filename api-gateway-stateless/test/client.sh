#!/bin/bash

# proxy URI
PROXY_URL="http://localhost:9090/api/route"

# 내부 변수 설정
PATH_PATTERN="/todo1/**"
#PATH_PATTERN="/mytodo/**" # non rewrite
URI="http://localhost:8080/todo1/**"
# URI="http://localhost:8080/mytodo/**" # non rewrite
METHOD="GET"
HOST=""
HEADER_NAME="X-Request-Type"
HEADER_VALUE="Ajax"
PATH_PATTERN_REWRITE="/todo1/(?<segment>.*)"  # 원래의 경로 패턴
PATH_REPLACEMENT="/mytodo/\${segment}"  # 대상 서비스에 전달될 경로 패턴

# 입력된 번호에 따른 curl 요청 수행 함수
function execute_curl() {
    case $1 in
        1)
            curl -X POST $PROXY_URL \
                -H "Content-Type: application/json" \
                -d "{
                       \"path\": \"$PATH_PATTERN\",
                       \"uri\": \"$URI\"
                     }"
            ;;
        2)
            curl -X POST $PROXY_URL \
                -H "Content-Type: application/json" \
                -d "{
                       \"path\": \"$PATH_PATTERN\",
                       \"uri\": \"$URI\",
                       \"method\": \"$METHOD\"
                     }"
            ;;
        3)
            curl -X POST $PROXY_URL \
                -H "Content-Type: application/json" \
                -d "{
                       \"path\": \"$PATH_PATTERN\",
                       \"uri\": \"$URI\",
                       \"method\": \"$METHOD\",
                       \"host\": \"$HOST\"
                     }"
            ;;
        4)
            curl -X POST $PROXY_URL \
                -H "Content-Type: application/json" \
                -d "{
                       \"path\": \"$PATH_PATTERN\",
                       \"uri\": \"$URI\",
                       \"method\": \"$METHOD\",
                       \"host\": \"$HOST\",
                       \"headerName\": \"$HEADER_NAME\",
                       \"headerValue\": \"$HEADER_VALUE\",
                       \"pathPattern\": \"$PATH_PATTERN_REWRITE\",
                       \"pathReplacement\": \"$PATH_REPLACEMENT\"
                     }"
            ;;
        *)
            echo "잘못된 번호입니다. 1 ~ 4 사이의 숫자를 선택하세요."
            display_guide
            ;;
    esac
}

# 사용자 가이드 출력 함수
function display_guide() {
    echo "원하는 번호를 선택하세요:"
    echo "1: Path만 포함"
    echo "2: Path와 Method 포함"
    echo "3: Path, Method, Host 포함"
    echo "4: Path, Method, Host, Header, RewritePath 정보 포함"  # 설명 업데이트
    read -p "번호 입력: " NUMBER

    if [[ $NUMBER =~ ^[1-4]$ ]]; then
        execute_curl $NUMBER
    else
        echo "올바른 번호를 입력하세요. 프로그램을 종료합니다."
        exit 1
    fi
}

# 사용자 입력을 받아 해당하는 curl 요청 실행
display_guide

