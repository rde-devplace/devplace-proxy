apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: ${IMAGE_NAME}-tls-cert
  namespace: kube-pattern
spec:
  secretName: ${IMAGE_NAME}-tls-cert
  secretTemplate:
    annotations:
      kubed.appscode.com/sync: "amdp.io/tls=enabled"
      update: ${HASHCODE}
  renewBefore: 240h
  dnsNames:
  - '${IMAGE_NAME}.amdp-dev.skamdp.org'
  issuerRef:
    group: cert-manager.io
    name: letsencrypt-prod
    kind: ClusterIssuer

