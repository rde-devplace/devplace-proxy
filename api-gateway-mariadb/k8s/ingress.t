apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: public-nginx
    nginx.ingress.kubernetes.io/enable-cors: "true"
    nginx.ingress.kubernetes.io/cors-allow-origin: "*"
    nginx.ingress.kubernetes.io/cors-allow-methods: "PUT, GET, POST, DELETE, OPTIONS"
    nginx.ingress.kubernetes.io/cors-allow-headers: "Authorization, DNT, X-CustomHeader, Keep-Alive, User-Agent, X-Requested-With, If-Modified-Since, Cache-Control, Content-Type"
    nginx.ingress.kubernetes.io/cors-allow-credentials: "true"
  name: ${IMAGE_NAME}-ingress
  namespace: ${NAMESPACE}
spec:
  rules:
  - host: kube-proxy-rs.amdp-dev.skamdp.org
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: kube-proxy-rs-service
            port:
              number: 8080
  tls:
    - hosts:
      - kube-prpoxy-rs.amdp-dev.skamdp.org
      secretName: kube-proxy-rs-tls-cert


